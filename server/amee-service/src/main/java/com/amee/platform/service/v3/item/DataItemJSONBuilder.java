package com.amee.platform.service.v3.item;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import com.amee.service.path.PathItemService;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class DataItemJSONBuilder implements ResourceBuilder<JSONObject> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private PathItemService pathItemService;

    @Transactional(readOnly = true)
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject o = new JSONObject();
            // Get DataCategory identifier.
            String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (categoryIdentifier != null) {
                // Get DataCategory.
                DataCategory dataCategory = dataService.getDataCategoryByIdentifier(
                        environmentService.getEnvironmentByName("AMEE"), categoryIdentifier);
                if (dataCategory != null) {
                    // Get DataItem identifier.
                    String dataItemIdentifier = requestWrapper.getAttributes().get("itemIdentifier");
                    if (dataItemIdentifier != null) {
                        // Get DataItem.
                        DataItem dataItem = dataService.getDataItemByUid(dataCategory, dataItemIdentifier);
                        if (dataItem != null) {
                            // Get DataItem Element.
                            o.put("item", getDataItemJSONObject(requestWrapper, dataItem));
                            o.put("status", "OK");
                        } else {
                            o.put("status", "NOT_FOUND");
                        }
                    } else {
                        o.put("status", "ERROR");
                        o.put("error", "The itemIdentifier was missing.");
                    }
                } else {
                    o.put("status", "NOT_FOUND");
                }
            } else {
                o.put("status", "ERROR");
                o.put("error", "The categoryIdentifier was missing.");
            }
            return o;
        } catch (Exception e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected JSONObject getDataItemJSONObject(RequestWrapper requestWrapper, DataItem dataItem) throws JSONException {

        JSONObject itemObj = new JSONObject();
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean name = requestWrapper.getMatrixParameters().containsKey("name");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        itemObj.put("uid", dataItem.getUid());

        // Optional attributes.
        if (name || full) {
            itemObj.put("name", dataItem.getName());
            itemObj.put("categoryWikiName", dataItem.getDataCategory().getWikiName());
        }
        if (path || full) {
            // Get PathItem.
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
            PathItem pathItem = pathItemGroup.findByUId(dataItem.getDataCategory().getUid());
            // Add Paths.
            itemObj.put("path", dataItem.getPath());
            if (pathItem != null) {
                itemObj.put("fullPath", pathItem.getFullPath() + "/" + dataItem.getDisplayPath());
            }
        }
        if (audit || full) {
            itemObj.put("status", dataItem.getStatus().getName());
            itemObj.put("created", FMT.print(dataItem.getCreated().getTime()));
            itemObj.put("modified", FMT.print(dataItem.getModified().getTime()));
        }
        if (wikiDoc || full) {
            itemObj.put("wikiDoc", dataItem.getWikiDoc());
        }
        if (provenance || full) {
            itemObj.put("provenance", dataItem.getProvenance());
        }
        if ((itemDefinition || full) && (dataItem.getItemDefinition() != null)) {
            ItemDefinition id = dataItem.getItemDefinition();
            itemObj.put("itemDefinition", new JSONObject().put("uid", id.getUid()).put("name", id.getName()));
        }

        return itemObj;
    }

    public String getMediaType() {
        return "application/json";
    }
}