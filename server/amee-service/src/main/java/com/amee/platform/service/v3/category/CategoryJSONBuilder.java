package com.amee.platform.service.v3.category;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemDefinition;
import com.amee.service.data.DataService;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("categoryJSONBuilder")
@Scope("prototype")
public class CategoryJSONBuilder implements ResourceBuilder<JSONObject> {

    private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    private DataService dataService;

    @Transactional(readOnly = true)
    public JSONObject handle(RequestWrapper requestWrapper) {
        try {
            JSONObject representation = new JSONObject();
            String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
            if (categoryIdentifier != null) {
                // TODO: Need to handle WikiName too.
                // TODO: Needs to be Environment sensitive.
                DataCategory dataCategory = dataService.getDataCategoryByUid(categoryIdentifier);
                if (dataCategory != null) {
                    representation.put("category", getCategoryJSONObject(requestWrapper, dataCategory));
                    representation.put("status", "OK");
                } else {
                    representation.put("status", "NOT_FOUND");
                }
            } else {
                representation.put("status", "ERROR");
                representation.put("error", "The categoryIdentifier was missing.");
            }
            representation.put("version", requestWrapper.getVersion().toString());
            return representation;
        } catch (Exception e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected JSONObject getCategoryJSONObject(RequestWrapper requestWrapper, DataCategory dataCategory) throws JSONException {

        JSONObject category = new JSONObject();
        boolean full = requestWrapper.getMatrixParameters().containsKey("full");
        boolean path = requestWrapper.getMatrixParameters().containsKey("path");
        boolean audit = requestWrapper.getMatrixParameters().containsKey("audit");
        boolean authority = requestWrapper.getMatrixParameters().containsKey("authority");
        boolean wikiDoc = requestWrapper.getMatrixParameters().containsKey("wikiDoc");
        boolean provenance = requestWrapper.getMatrixParameters().containsKey("provenance");
        boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");

        // Basic attributes.
        category.put("uid", dataCategory.getUid());
        category.put("name", dataCategory.getName());
        category.put("wikiName", dataCategory.getWikiName());
        if (dataCategory.getDataCategory() != null) {
            category.put("parentWikiName", dataCategory.getDataCategory().getWikiName());
        }

        // Optional attributes.
        if (path || full) {
            category.put("path", dataCategory.getPath());
            category.put("fullPath", "/not/yet/implemented");
        }
        if (audit || full) {
            category.put("status", dataCategory.getStatus().getName());
            category.put("created", FMT.print(dataCategory.getCreated().getTime()));
            category.put("modified", FMT.print(dataCategory.getModified().getTime()));
        }
        if (authority || full) {
            category.put("authority", "Not yet implemented.");
        }
        if (wikiDoc || full) {
            category.put("wikiDoc", "Not yet implemented.");
        }
        if (provenance || full) {
            category.put("provenance", "Not yet implemented.");
        }
        if ((itemDefinition || full) && (dataCategory.getItemDefinition() != null)) {
            ItemDefinition id = dataCategory.getItemDefinition();
            category.put("itemDefinition", new JSONObject().put("uid", id.getUid()).put("name", id.getName()));
        }

        return category;
    }

    public String getMediaType() {
        return "application/json";
    }
}