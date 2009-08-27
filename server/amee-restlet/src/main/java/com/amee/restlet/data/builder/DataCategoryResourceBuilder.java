package com.amee.restlet.data.builder;

import com.amee.core.APIUtils;
import com.amee.domain.Pager;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.path.PathItem;
import com.amee.domain.path.PathItemGroup;
import com.amee.domain.sheet.Sheet;
import com.amee.restlet.data.DataCategoryResource;
import com.amee.service.data.DataService;
import com.amee.service.definition.DefinitionService;
import com.amee.service.path.PathItemService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Service
public class DataCategoryResourceBuilder {

    @Autowired
    private DataService dataService;

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private PathItemService pathItemService;

    public JSONObject getJSONObject(DataCategoryResource resource) throws JSONException {

        // create JSON object
        JSONObject obj = new JSONObject();
        obj.put("path", resource.getPathItem().getFullPath());

        if (resource.isGet()) {

            // addItemValue DataCategory
            obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));

            // list child Data Categories and child Data Items
            JSONObject children = new JSONObject();

            // addItemValue Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();

            // If the DC is a symlink, use the target PathItem
            PathItem pathItem = resource.getPathItem();
            if (resource.getDataCategory().getAliasedCategory() != null) {
                PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(resource.getEnvironment());
                pathItem = pathItemGroup.findByUId(resource.getDataCategory().getAliasedCategory().getUid());
            }

            for (PathItem pi : pathItem.getChildrenByType("DC")) {

                DataCategory child = dataService.getDataCategoryByUid(pi.getUid());

                // TODO: PERMISSIONS
                //if (child == null || (child.isDeprecated() &&
                //        !resource.getDataBrowser().getDataCategoryActions().isAllowViewDeprecated()))
                //    continue;

                dataCategories.put(pi.getJSONObject());

            }
            children.put("dataCategories", dataCategories);

            // addItemValue Sheet containing Data Items
            Sheet sheet = dataService.getSheet(resource.getDataBrowser());
            if (sheet != null) {
                Pager pager = resource.getPager(resource.getItemsPerPage());
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                children.put("dataItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
            } else {
                children.put("dataItems", new JSONObject());
                children.put("pager", new JSONObject());
            }

            // addItemValue children
            obj.put("children", children);

        } else if (resource.isPostOrPut()) {

            // DataCategories
            if (resource.getDataCategory() != null) {
                obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));
            } else if (resource.getDataCategories() != null) {
                JSONArray dataCategories = new JSONArray();
                obj.put("dataCategories", dataCategories);
                for (DataCategory dc : resource.getDataCategories()) {

                    // TODO: PERMISSIONS
                    // if (dc.isDeprecated() &&
                    //        !resource.getDataBrowser().getDataCategoryActions().isAllowViewDeprecated())
                    //    continue;

                    dataCategories.put(dc.getJSONObject(false));
                }
            }

            // DataItems
            if (resource.getDataItem() != null) {
                obj.put("dataItem", resource.getDataItem().getJSONObject(true));
            } else if (resource.getDataItems() != null) {
                JSONArray dataItems = new JSONArray();
                obj.put("dataItems", dataItems);
                for (DataItem di : resource.getDataItems()) {
                    dataItems.put(di.getJSONObject(false));
                }
            }
        }

        return obj;
    }

    public Element getElement(DataCategoryResource resource, Document document) {

        Element element = document.createElement("DataCategoryResource");
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));

        if (resource.isGet()) {

            // addItemValue DataCategory
            element.appendChild(resource.getDataCategory().getElement(document, true));

            // list child Data Categories and child Data Items
            Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // addItemValue Data Categories
            Element dataCategoriesElement = document.createElement("DataCategories");

            // If the DC is a symlink, use the target PathItem
            PathItem pathItem = resource.getPathItem();
            if (resource.getDataCategory().getAliasedCategory() != null) {
                PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(resource.getEnvironment());
                pathItem = pathItemGroup.findByUId(resource.getDataCategory().getAliasedCategory().getUid());
            }

            for (PathItem pi : pathItem.getChildrenByType("DC")) {

                DataCategory child = dataService.getDataCategoryByUid(pi.getUid());

                // TODO: PERMISSIONS
                // if (child == null || (child.isDeprecated() &&
                //        !resource.getDataBrowser().getDataCategoryActions().isAllowViewDeprecated()))
                //    continue;

                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);
            // list child Data Items via sheet
            Sheet sheet = dataService.getSheet(resource.getDataBrowser());
            if (sheet != null) {
                Pager pager = resource.getPager(resource.getItemsPerPage());
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
            }

        } else if (resource.isPostOrPut()) {

            // DataCategories
            if (resource.getDataCategory() != null) {
                element.appendChild(resource.getDataCategory().getElement(document, false));
            } else if (resource.getDataCategories() != null) {
                Element dataItemsElement = document.createElement("DataCategories");
                element.appendChild(dataItemsElement);
                for (DataCategory dc : resource.getDataCategories()) {

                    // TODO: PERMISSIONS
                    // if (dc.isDeprecated() &&
                    //        !resource.getDataBrowser().getDataCategoryActions().isAllowViewDeprecated())
                    //    continue;

                    dataItemsElement.appendChild(dc.getElement(document, false));
                }
            }

            // DataItems
            if (resource.getDataItem() != null) {
                element.appendChild(resource.getDataItem().getElement(document, false));
            } else if (resource.getDataItems() != null) {
                Element dataItemsElement = document.createElement("DataItems");
                element.appendChild(dataItemsElement);
                for (DataItem di : resource.getDataItems()) {
                    dataItemsElement.appendChild(di.getElement(document, false));
                }
            }
        }

        return element;
    }

    public Map<String, Object> getTemplateValues(DataCategoryResource resource) {
        DataCategory dataCategory = resource.getDataCategory();
        Sheet sheet = dataService.getSheet(resource.getDataBrowser());
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getDataBrowser());
        values.put("dataCategory", dataCategory);
        values.put("itemDefinition", dataCategory.getItemDefinition());
        values.put("user", resource.getUser());
        values.put("itemDefinitions", definitionService.getItemDefinitions(resource.getEnvironment()));
        values.put("node", dataCategory);
        if (sheet != null) {
            Pager pager = resource.getPager(resource.getItemsPerPage());
            sheet = Sheet.getCopy(sheet, pager);
            pager.setCurrentPage(resource.getPage());
            values.put("sheet", sheet);
            values.put("pager", pager);
        }

        // If the DC is a symlink, use the target PathItem
        PathItem pathItem = resource.getPathItem();
        values.put("fullPath", "/data" + pathItem.getFullPath());
        if (resource.getDataCategory().getAliasedCategory() != null) {
            PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(resource.getEnvironment());
            pathItem = pathItemGroup.findByUId(resource.getDataCategory().getAliasedCategory().getUid());
            values.put("pathItem", pathItem);
        }
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement() {
        return null;
    }
}
