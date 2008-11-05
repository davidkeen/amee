package gc.carbon.builder.resource.v1;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.APIObject;
import gc.carbon.builder.domain.v1.ProfileItemBuilder;
import gc.carbon.builder.domain.BuildableProfileItem;
import gc.carbon.builder.resource.BuildableResource;
import gc.carbon.builder.resource.ResourceBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
public class ProfileCategoryResourceBuilder extends ResourceBuilder {

    public ProfileCategoryResourceBuilder(BuildableResource resource) {
        super(resource);
    }

    public JSONObject getJSONObject()  throws JSONException {

        JSONObject obj = new JSONObject();

        // add objects
        obj.put("path", resource.getFullPath());
        obj.put("profileDate", resource.getProfileDate());

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            obj.put("profile", resource.getProfile().getJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());

        if (resource.isGet()) {

            // create children JSON
            JSONObject children = new JSONObject();

            // add Data Categories via pathItem to children
            JSONArray dataCategories = new JSONArray();
            for (APIObject pi : resource.getChildrenByType("DC")) {
                dataCategories.put(pi.getJSONObject());
            }
            children.put("dataCategories", dataCategories);

            // add Sheet containing Profile Items & totalAmountPerMonth
/*            Sheet sheet = resource.getProfileSheetService().getSheet(resource.getProfile(), resource.getDataCategory(), resource.getProfileDate());
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                children.put("profileItems", sheet.getJSONObject());
                children.put("pager", pager.getJSONObject());
                obj.put("totalAmountPerMonth", resource.getProfileSheetService().getTotalAmountPerMonth(sheet));
            } else {
                children.put("profileItems", new JSONObject());
                children.put("pager", new JSONObject());
                obj.put("totalAmountPerMonth", "0");
            }*/

            // add chilren
            obj.put("children", children);

        } else if (resource.isPost() || resource.isPut()) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    BuildableProfileItem pi = resource.getProfileItems().get(0);
                    pi.setBuilder(new ProfileItemBuilder(pi));
                    obj.put("profileItem", pi.getJSONObject());
                } else {
                    JSONArray profileItems = new JSONArray();
                    obj.put("profileItems", profileItems);
                    for (BuildableProfileItem pi : resource.getProfileItems()) {
                        pi.setBuilder(new ProfileItemBuilder(pi));
                        profileItems.put(pi.getJSONObject(false));
                    }
                }
            }
        }

        return obj;
    }

    public Element getElement(Document document) {

        // create element
        org.w3c.dom.Element element = document.createElement("ProfileCategoryResource");

        // add objects
        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));

        // add profile date
        //element.appendChild(resource.getDateTimeBrowser().getProfileDate().toXML(document));
                // add profile date
        element.appendChild(APIUtils.getElement(document, "ProfileDate",resource.getProfileDate().toString()));

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            element.appendChild(resource.getProfile().getElement(document));
        } else {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        }

        // add DataCategory and Profile elements
        element.appendChild(resource.getDataCategory().getIdentityElement(document));

        if (resource.isGet()) {

            // list child Profile Categories and child Profile Items
            org.w3c.dom.Element childrenElement = document.createElement("Children");
            element.appendChild(childrenElement);

            // add Profile Categories via pathItem
            org.w3c.dom.Element dataCategoriesElement = document.createElement("ProfileCategories");
            for (APIObject pi : resource.getChildrenByType("DC")) {
                dataCategoriesElement.appendChild(pi.getElement(document));
            }
            childrenElement.appendChild(dataCategoriesElement);

            // get Sheet containing Profile Items
/*            Sheet sheet = resource.getProfileSheetService().getSheet(profile, resource.getDataCategory(), resource.getProfileDate());
            if (sheet != null) {
                Pager pager = resource.getPager();
                sheet = Sheet.getCopy(sheet, pager);
                pager.setCurrentPage(resource.getPage());
                // list child Profile Items via sheet
                childrenElement.appendChild(sheet.getElement(document, false));
                childrenElement.appendChild(pager.getElement(document));
                // add CO2 amount
                element.appendChild(APIUtils.getElement(document, "TotalAmountPerMonth",
                        resource.getProfileSheetService().getTotalAmountPerMonth(sheet).toString()));
            }*/

        } else if (resource.isPost() || resource.isPut()) {

            if (!resource.getProfileItems().isEmpty()) {
                if (resource.getProfileItems().size() == 1) {
                    BuildableProfileItem pi = resource.getProfileItems().get(0);
                    pi.setBuilder(new ProfileItemBuilder(pi));
                    element.appendChild(pi.getElement(document, false));
                } else {
                    org.w3c.dom.Element profileItemsElement = document.createElement("ProfileItems");
                    element.appendChild(profileItemsElement);
                    for (BuildableProfileItem pi : resource.getProfileItems()) {
                        pi.setBuilder(new ProfileItemBuilder(pi));
                        profileItemsElement.appendChild(pi.getElement(document, false));
                    }
                }
            }
        }

        return element;
    }

}
