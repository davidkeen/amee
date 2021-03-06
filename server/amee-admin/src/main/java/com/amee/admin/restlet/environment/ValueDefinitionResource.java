package com.amee.admin.restlet.environment;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ValueDefinition;
import com.amee.domain.ValueType;
import com.amee.restlet.environment.DefinitionBrowser;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class ValueDefinitionResource extends AdminResource {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        definitionBrowser.setValueDefinitionUid(request.getAttributes().get("valueDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getValueDefinitionUid() != null);
    }

    @Override
    public List<IAMEEEntityReference> getEntities() {
        List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
        entities.add(getRootDataCategory());
        entities.add(definitionBrowser.getValueDefinition());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_VALUE_DEFINITION;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("valueDefinition", definitionBrowser.getValueDefinition());
        values.put("valueTypes", ValueType.getChoices());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("valueDefinition", definitionBrowser.getValueDefinition().getJSONObject());
        obj.put("valueTypes", ValueType.getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ValueDefinitionResource");
        element.appendChild(definitionBrowser.getValueDefinition().getElement(document));
        element.appendChild(ValueType.getElement(document));
        return element;
    }

    @Override
    public void doStore(Representation entity) {
        log.debug("doStore()");
        ValueDefinition valueDefinition = definitionBrowser.getValueDefinition();
        Form form = getForm();
        Set<String> names = form.getNames();
        if (names.contains("name")) {
            valueDefinition.setName(form.getFirstValue("name"));
        }
        if (names.contains("description")) {
            valueDefinition.setDescription(form.getFirstValue("description"));
        }
        if (names.contains("valueType")) {
            String valueType = form.getFirstValue("valueType");
            valueType = valueType.equalsIgnoreCase("DECIMAL") ? "DOUBLE" : valueType;
            valueDefinition.setValueType(ValueType.valueOf(valueType));
        }
        success();
    }

    @Override
    public void doRemove() {
        log.debug("doRemove");
        ValueDefinition valueDefinition = definitionBrowser.getValueDefinition();
        definitionService.remove(valueDefinition);
        success();
    }
}
