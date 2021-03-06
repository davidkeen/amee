package com.amee.restlet.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.amee.base.utils.ThreadBeanHolder;
import com.amee.base.utils.XMLUtils;
import com.amee.calculation.service.CalculationService;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.data.builder.DataItemBuilder;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.item.data.BaseDataItemValue;
import com.amee.domain.item.data.DataItem;
import com.amee.domain.item.data.DataItemNumberValueHistory;
import com.amee.domain.item.data.DataItemTextValueHistory;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.platform.science.Amount;
import com.amee.platform.science.AmountPerUnit;
import com.amee.platform.science.AmountUnit;
import com.amee.platform.science.CO2AmountUnit;
import com.amee.platform.science.Note;
import com.amee.platform.science.ReturnValue;
import com.amee.platform.science.ReturnValues;
import com.amee.platform.science.StartEndDate;
import com.amee.restlet.AMEEResource;
import com.amee.restlet.RequestContext;
import com.amee.service.data.DataBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.data.DataService;
import com.amee.service.data.DataSheetService;
import com.amee.service.invalidation.InvalidationService;
import com.amee.service.item.DataItemServiceImpl;
import com.amee.service.profile.ProfileService;

//TODO - move to builder model

@Component
@Scope("prototype")
public class DataItemResource extends AMEEResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DataService dataService;

    @Autowired
    private DataItemServiceImpl dataItemService;

    @Autowired
    private DataSheetService dataSheetService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private DataBrowser dataBrowser;

    private DataCategory dataCategory;
    private DataItem dataItem;
    private String unit;
    private String perUnit;
    private List<Choice> parameters;
    @Autowired
    protected ProfileService profileService;

    @Autowired
    private InvalidationService invalidationService;

    @Override
    public void initialise(Context context, Request request, Response response) {

        super.initialise(context, request, response);

        // Obtain DataCategory.
        dataCategory = dataService.getDataCategoryByUid(request.getAttributes().get("categoryUid").toString());
        dataBrowser.setDataCategory(dataCategory);
        (ThreadBeanHolder.get(RequestContext.class)).setDataCategory(dataCategory);

        // Obtain DataItem.
        dataItem = dataItemService.getDataItemByIdentifier(dataCategory, request.getAttributes().get("itemPath").toString());
        (ThreadBeanHolder.get(RequestContext.class)).setDataItem(dataItem);

        // Must have a DataItem to do anything here.
        if (dataItem != null) {

            // We'll pre-process query parameters here to keep the Data API calculation parameters sane.
            Form query = request.getResourceRef().getQueryAsForm();
            unit = query.getFirstValue("returnUnit");
            perUnit = query.getFirstValue("returnPerUnit");

            // The resource may receive a startDate parameter that sets the current date in an
            // historical sequence of ItemValues.
            if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
                dataItem.setEffectiveStartDate(new StartEndDate(query.getFirstValue("startDate")));
            }

            // Query parameter names, minus startDate, returnUnit and returnPerUnit.
            Set<String> names = query.getNames();
            names.remove("startDate");
            names.remove("returnUnit");
            names.remove("returnPerUnit");

            // Pull out any values submitted for Data API calculations.
            parameters = new ArrayList<Choice>();
            for (String key : names) {
                parameters.add(new Choice(key, query.getValues(key)));
            }
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
            (dataCategory != null) &&
            (dataItem != null) &&
            dataItem.getDataCategory().equals(dataCategory) &&
            !dataItem.isTrash();
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        return dataItem.getHierarchy();
    }

    @Override
    public String getTemplatePath() {
        return getAPIVersion() + "/" + DataConstants.VIEW_DATA_ITEM;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Choices userValueChoices = dataSheetService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        Amount amount = calculationService.calculate(dataItem, userValueChoices, getAPIVersion()).defaultValueAsAmount();
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new AmountUnit(SI.KILOGRAM), new AmountPerUnit(NonSI.MONTH));
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", dataBrowser);
        values.put("dataItem", dataItem);
        values.put("node", dataItem);
        values.put("userValueChoices", userValueChoices);
        values.put("amountPerMonth", amount.convert(kgPerMonth).getValue());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        Choices userValueChoices = dataSheetService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        ReturnValues returnAmounts = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        Amount amount = returnAmounts.defaultValueAsAmount();
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new AmountUnit(SI.KILOGRAM), new AmountPerUnit(NonSI.MONTH));
        JSONObject obj = new JSONObject();
        obj.put("dataItem", new DataItemBuilder(dataItem, dataItemService).getJSONObject(true, false));
        obj.put("path", dataItem.getFullPath());
        obj.put("userValueChoices", userValueChoices.getJSONObject());

        double doubleValue = amount.convert(kgPerMonth).getValue();
        if (Double.isInfinite(doubleValue)) {
            obj.put("amountPerMonth", "Infinity");
        } else if (Double.isNaN(doubleValue)) {
            obj.put("amountPerMonth", "NaN");
        } else {
            obj.put("amountPerMonth", doubleValue);
        }

        if (getAPIVersion().isNotVersionOne()) {
            CO2AmountUnit returnUnit = new CO2AmountUnit(unit, perUnit);
            JSONObject amountObj = new JSONObject();

            doubleValue = amount.convert(returnUnit).getValue();
            if (Double.isInfinite(doubleValue)) {
                amountObj.put("value", "Infinity");
            } else if (Double.isNaN(doubleValue)) {
                amountObj.put("value", "NaN");
            } else {
                amountObj.put("value", doubleValue);
            }

            amountObj.put("unit", returnUnit);
            obj.put("amount", amountObj);

            // Multiple return values
            JSONObject amounts = new JSONObject();

            // Create an array of amount objects
            JSONArray amountArray = new JSONArray();
            for (Map.Entry<String, ReturnValue> entry : returnAmounts.getReturnValues().entrySet()) {
                String type = entry.getKey();
                ReturnValue returnValue = entry.getValue();

                // Create an Amount object
                JSONObject multiAmountObj = new JSONObject();
                multiAmountObj.put("type", type);
                multiAmountObj.put("unit", returnValue != null ? returnValue.getUnit() : "");
                multiAmountObj.put("perUnit", returnValue != null ? returnValue.getPerUnit() : "");
                if (type.equals(returnAmounts.getDefaultType())) {
                    multiAmountObj.put("default", "true");
                }
                if (returnValue == null) {
                    multiAmountObj.put("value", JSONObject.NULL);
                } else if (Double.isInfinite(returnValue.getValue())) {
                    multiAmountObj.put("value", "Infinity");
                } else if (Double.isNaN(returnValue.getValue())) {
                    multiAmountObj.put("value", "NaN");
                } else {
                    multiAmountObj.put("value", returnValue.getValue());
                }

                // Add the object to the amounts array
                amountArray.put(multiAmountObj);
            }

            // Add the amount array to the amounts object.
            amounts.put("amount", amountArray);

            // Create an array of note objects
            JSONArray noteArray = new JSONArray();
            for (Note note : returnAmounts.getNotes()) {
                JSONObject noteObj = new JSONObject();
                noteObj.put("type", note.getType());
                noteObj.put("value", note.getValue());
                // TODO: this looks wrong.
                amounts.put("note", noteObj);

                // Add the note object to the notes array
                noteArray.put(noteObj);
            }

            // Add the notes array to the amounts object.
            if (noteArray.length() > 0) {
                amounts.put("note", noteArray);
            }

            obj.put("amounts", amounts);
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Choices userValueChoices = dataSheetService.getUserValueChoices(dataItem, getAPIVersion());
        userValueChoices.merge(parameters);
        ReturnValues returnAmounts = calculationService.calculate(dataItem, userValueChoices, getAPIVersion());
        Amount amount = returnAmounts.defaultValueAsAmount();
        CO2AmountUnit kgPerMonth = new CO2AmountUnit(new AmountUnit(SI.KILOGRAM), new AmountPerUnit(NonSI.MONTH));
        Element element = document.createElement("DataItemResource");
        element.appendChild(new DataItemBuilder(dataItem, dataItemService).getElement(document, true, false));
        element.appendChild(XMLUtils.getElement(document, "Path", dataItem.getFullPath()));
        element.appendChild(userValueChoices.getElement(document));
        element.appendChild(XMLUtils.getElement(document, "AmountPerMonth", amount.convert(kgPerMonth).toString()));
        if (getAPIVersion().isNotVersionOne()) {
            CO2AmountUnit returnUnit = new CO2AmountUnit(unit, perUnit);
            Element amountElem = document.createElement("Amount");
            amountElem.setTextContent(amount.convert(returnUnit).toString());
            amountElem.setAttribute("unit", returnUnit.toString());
            element.appendChild(amountElem);

            // Multiple return values
            Element amounts = document.createElement("Amounts");
            for (Map.Entry<String, ReturnValue> entry : returnAmounts.getReturnValues().entrySet()) {
                String type = entry.getKey();
                ReturnValue returnValue = entry.getValue();

                Element multiAmount = document.createElement("Amount");
                multiAmount.setAttribute("type", type);
                multiAmount.setAttribute("unit", returnValue != null ? returnValue.getUnit() : "");
                multiAmount.setAttribute("perUnit", returnValue != null ? returnValue.getPerUnit() : "");
                if (type.equals(returnAmounts.getDefaultType())) {
                    multiAmount.setAttribute("default", "true");
                }
                multiAmount.setTextContent(returnValue != null ? returnValue.getValue() + "" : "");
                amounts.appendChild(multiAmount);
            }
            for (Note note : returnAmounts.getNotes()) {
                Element noteElm = document.createElement("Note");
                noteElm.setAttribute("type", note.getType());
                noteElm.setTextContent(note.getValue());
                amounts.appendChild(noteElm);
            }
            element.appendChild(amounts);
        }
        return element;
    }

    /**
     * Create ItemValues based on POSTed parameters.
     * 
     * @param entity representation
     */
    @Override
    public void doAccept(Representation entity) {

        log.debug("doAccept()");

        Set<String> names = getForm().getNames();

        // Obtain the startDate.
        StartEndDate startDate = new StartEndDate(getForm().getFirstValue("startDate"));
        names.remove("startDate");

        // Update named ItemValues.
        for (String name : names) {

            // Fetch the itemValueDefinition.
            ItemValueDefinition itemValueDefinition = dataItem.getItemDefinition().getItemValueDefinition(name);
            if (itemValueDefinition == null) {
                // The submitted ItemValueDefinition must be in the owning ItemDefinition.
                log.warn("acceptRepresentation() badRequest - Trying to create a DIV with an IVD not belonging to the DI ID.");
                badRequest();
                return;
            }

            // Cannot create new ItemValues for ItemValueDefinitions which are used in the DrillDown for
            // the owning ItemDefinition.
            if (dataItem.getItemDefinition().isDrillDownValue(itemValueDefinition)) {
                log.warn("acceptRepresentation() badRequest - Trying to create a DIV that is a DrillDown value.");
                badRequest();
                return;
            }

            // The new ItemValue must be unique on itemValueDefinitionUid + startDate.
            if (!dataItemService.isItemValueUnique(dataItem, itemValueDefinition, startDate)) {
                log.warn("acceptRepresentation() badRequest - Trying to create a DIV with the same IVD and startDate as an existing DIV.");
                badRequest();
                return;
            }

            // TODO: PL-6577 - Is this check needed, given the check above? Can they be merged?
            if (startDate.getTime() == 0) {
                // Normal
                // We should never get here. checkDataItem should always create the first item value (startDate = EPOCH)
                log.warn("acceptRepresentation() badRequest - Trying to create another DIV with the startDate as the EPOCH.");
                badRequest();
            } else {
                // History
                BaseDataItemValue newDataItemValue;
                if (itemValueDefinition.isDouble()) {
                    newDataItemValue = new DataItemNumberValueHistory(itemValueDefinition, dataItem, Double.parseDouble(getForm().getFirstValue(name)), startDate);
                } else {
                    newDataItemValue = new DataItemTextValueHistory(itemValueDefinition, dataItem, getForm().getFirstValue(name), startDate);
                }
                dataItemService.persist(newDataItemValue);
                // Mark the DataItem as modified.
                dataItem.onModify();
            }
        }

        // Clear caches.
        dataItemService.clearItemValues();
        invalidationService.invalidate(dataItem.getDataCategory());

        // Return successful creation of new DataItemValue.
        successfulPost(dataItem.getUid());
    }

    /**
     * Update the DataItem and contained ItemValues based on PUT parameters. ItemValues can be identified
     * by their ItemValueDefinition path or their specific UID.
     * <p/>
     * When updating ItemValues using the ItemValueDefinition path the appropriate instance will be selected based on the query string startDate parameter. This
     * is only relevant for non drill-down ItemValues, as only one instance of these is allowed.
     * 
     * @param entity representation
     */
    @Override
    public void doStore(Representation entity) {

        log.debug("doStore()");

        boolean modified = false;
        Form form = getForm();
        Set<String> names = form.getNames();

        // Update 'name' value.
        if (names.contains("name")) {
            dataItem.setName(form.getFirstValue("name"));
            names.remove("name");
            modified = true;
        }

        // Update 'path' value.
        if (names.contains("path")) {
            dataItem.setPath(form.getFirstValue("path"));
            names.remove("path");
            modified = true;
        }

        // Update named ItemValues.
        for (String name : form.getNames()) {
            BaseItemValue itemValue = dataItemService.getItemValue(dataItem, name);
            if (itemValue != null) {
                itemValue.setValue(form.getFirstValue(name));
                modified = true;
            }
        }

        // Mark the DataItem as modified.
        if (modified) {
            dataItem.onModify();
        }

        // Clear caches.
        dataItemService.clearItemValues();
        invalidationService.invalidate(dataItem.getDataCategory());

        // Return successful update of DataItem.
        successfulPut("/data/" + dataItem.getFullPath());
    }

    @Override
    public void doRemove() {
        log.debug("doRemove()");
        invalidationService.invalidate(dataItem.getDataCategory());
        dataItemService.remove(dataItem);
        successfulDelete("/data/" + dataItem.getDataCategory().getFullPath());
    }

    public String getFullPath() {
        return "/data" + dataItem.getFullPath();
    }
}