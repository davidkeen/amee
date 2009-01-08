package gc.carbon.profile.builder.v2;

import com.jellymold.utils.Pager;
import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.domain.APIUtils;
import gc.carbon.data.builder.BuildableCategoryResource;
import gc.carbon.data.builder.ResourceBuilder;
import gc.carbon.domain.data.builder.BuildableItemDefinition;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.domain.profile.builder.v2.ProfileItemBuilder;
import gc.carbon.domain.Builder;
import gc.carbon.profile.OnlyActiveProfileService;
import gc.carbon.profile.ProRataProfileService;
import gc.carbon.profile.ProfileService;
import gc.carbon.profile.SelectByProfileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class ProfileCategoryResourceBuilder implements ResourceBuilder {

    private final Log log = LogFactory.getLog(getClass());

    private ProfileService profileService;

    BuildableCategoryResource resource;

    public ProfileCategoryResourceBuilder(BuildableCategoryResource resource) {
        this.resource = resource;
        this.profileService = resource.getProfileService();
    }

    public JSONObject getJSONObject() throws JSONException {

        JSONObject obj = new JSONObject();

        // TODO - Move up to sibling
        // Add APIVersion
        obj.put("apiVersion", resource.getVersion().getJSONObject());

        // add objects
        obj.put("path", resource.getFullPath());
        obj.put("startDate", resource.getStartDate());
        obj.put("endDate", (resource.getEndDate() != null) ? resource.getEndDate().toString() : "");


        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            obj.put("profile", resource.getProfile().getIdentityJSONObject());
        } else {
            obj.put("profile", resource.getProfile().getJSONObject());
        }

        // add Data Category
        obj.put("dataCategory", resource.getDataCategory().getIdentityJSONObject());

        // add Data Categories via pathItem to children
        JSONArray dataCategories = new JSONArray();
        for (APIObject pi : resource.getChildrenByType("DC")) {
            dataCategories.put(pi.getJSONObject());
        }
        obj.put("profileCategories", dataCategories);

        // profile items
        List<? extends BuildableProfileItem> profileItems;
        Pager pager = null;

        if (resource.isGet()) {
            // get profile items
            profileItems = getProfileItems();

            // set-up pager
            pager = resource.getPager();
            profileItems = pageResults(profileItems, pager);
        } else {
            profileItems = resource.getProfileItems();
        }

        if (!profileItems.isEmpty()) {
            JSONArray jsonProfileItems = new JSONArray();
            obj.put("profileItems", jsonProfileItems);
            for (BuildableProfileItem pi : profileItems) {
                pi.setBuilder(new ProfileItemBuilder(pi));
                jsonProfileItems.put(pi.getJSONObject(false));
            }

            // pager
            if (pager != null) {
                obj.put("pager", pager.getJSONObject());
            }

            // add CO2 amount
            obj.put("totalAmount", getTotalAmount(profileItems).toString());

        } else if (resource.isPost() || resource.isPut()) {

            if (!profileItems.isEmpty()) {
                JSONArray profileItemsJSonn = new JSONArray();
                obj.put("profileItems", profileItems);
                for (BuildableProfileItem pi : profileItems) {
                    setBuilder(pi);
                    profileItemsJSonn.put(pi.getJSONObject(false));
                }
            }

        } else {
            obj.put("profileItems", new JSONObject());
            obj.put("pager", new JSONObject());
            obj.put("totalAmount", "0");
        }

        return obj;
    }

    public Element getElement(Document document) {

        // create element
        Element element = document.createElement("ProfileCategoryResource");

        // Add APIVersion
        element.appendChild(resource.getVersion().getElement(document));

        element.appendChild(APIUtils.getElement(document, "Path", resource.getFullPath()));
        element.appendChild(APIUtils.getElement(document, "StartDate", resource.getStartDate().toString()));
        element.appendChild(APIUtils.getElement(document, "EndDate", (resource.getEndDate() != null) ? resource.getEndDate().toString() : ""));

        // add relevant Profile info depending on whether we are at root
        if (resource.hasParent()) {
            element.appendChild(resource.getProfile().getIdentityElement(document));
        } else {
            element.appendChild(resource.getProfile().getElement(document));
        }

        // add DataCategory
        element.appendChild(resource.getDataCategory().getIdentityElement(document));

        // add Data Categories via pathItem to children
        Element dataCategoriesElement = document.createElement("ProfileCategories");
        for (APIObject dc : resource.getChildrenByType("DC")) {
            dataCategoriesElement.appendChild(dc.getElement(document));
        }
        element.appendChild(dataCategoriesElement);

        // profile items
        List<? extends BuildableProfileItem> profileItems;
        Pager pager = null;

        if (resource.isGet()) {
            // get profile items
            profileItems = getProfileItems();

            // set-up pager
            pager = resource.getPager();
            profileItems = pageResults(profileItems, pager);
        } else {
            profileItems = resource.getProfileItems();
        }

        if (!profileItems.isEmpty()) {
            Element profileItemsElement = document.createElement("ProfileItems");
            element.appendChild(profileItemsElement);
            for (BuildableProfileItem pi : profileItems) {
                setBuilder(pi);
                profileItemsElement.appendChild(pi.getElement(document, false));
            }

            // pager
            if (pager != null) {
                element.appendChild(pager.getElement(document));
            }

            // add CO2 amount
            element.appendChild(APIUtils.getElement(document,
                    "TotalAmount",
                    getTotalAmount(profileItems).toString()));

        }
        return element;
    }

    private List<? extends BuildableProfileItem> pageResults(List<? extends BuildableProfileItem> profileItems, Pager pager) {
        // set-up pager
        if (!(profileItems == null || profileItems.isEmpty())) {
            pager.setCurrentPage(resource.getPage());
            pager.setItems(profileItems.size());
            pager.goRequestedPage();

            // limit results
            profileItems = profileItems.subList((int) pager.getStart(), (int) pager.getTo());

            pager.setItemsFound(profileItems.size());
        }
        return profileItems;
    }

    private List<? extends BuildableProfileItem> getProfileItems() {
        BuildableItemDefinition itemDefinition;
        List<? extends BuildableProfileItem> profileItems = new ArrayList<BuildableProfileItem>();

        // must have ItemDefinition
        itemDefinition = resource.getProfileBrowser().getDataCategory().getItemDefinition();
        if (itemDefinition != null) {

            ProfileService decoratedProfileServiceDAO = new OnlyActiveProfileService(profileService);

            if (resource.getProfileBrowser().isProRataRequest()) {
                decoratedProfileServiceDAO = new ProRataProfileService(profileService);
            }

            if (resource.getProfileBrowser().isSelectByRequest()) {
                decoratedProfileServiceDAO = new SelectByProfileService(decoratedProfileServiceDAO, resource.getProfileBrowser().getSelectBy());
            }

            profileItems = decoratedProfileServiceDAO.getProfileItems(resource.getProfileBrowser());
        }
        return profileItems;
    }

    private BigDecimal getTotalAmount(List<? extends BuildableProfileItem> profileItems) {
        BigDecimal totalAmount = ProfileItem.ZERO;
        BigDecimal amount;

        for (BuildableProfileItem profileItem : profileItems) {
            try {
                amount = profileItem.getAmount();
                amount = amount.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                if (amount.precision() > ProfileItem.PRECISION) {
                    log.warn("getTotalAmount() - precision is too big: " + amount);
                    // TODO: do something?
                }
            } catch (Exception e) {
                // swallow
                log.warn("getTotalAmount() - caught Exception: " + e);
                amount = ProfileItem.ZERO;
            }
            totalAmount = totalAmount.add(amount);
        }
        return totalAmount;
    }

    private void setBuilder(BuildableProfileItem pi) {
        if (resource.getProfileBrowser().returnAmountInExternalUnit()) {
            pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getAmountUnit()));
        } else {
            pi.setBuilder(new ProfileItemBuilder(pi));
        }
    }

    public Map<String, Object> getTemplateValues() {

        // profile items
        List<? extends BuildableProfileItem> profileItems;
        Pager pager;

        if (resource.isGet()) {
            // get profile items
            profileItems = getProfileItems();

            // set-up pager
            pager = resource.getPager();
            profileItems = pageResults(profileItems, pager);
        } else {
            profileItems = resource.getProfileItems();
        }

        // init builder to ensure units are represented correctly
        for (BuildableProfileItem pi : profileItems) {
            ProfileItemBuilder builder = new ProfileItemBuilder(pi);
            pi.setConvertedAmount(builder.getAmount(pi));
        }

        APIObject profile = resource.getProfile();
        APIObject dataCategory = resource.getDataCategory();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profile);
        values.put("dataCategory", dataCategory);
        values.put("node", dataCategory);
        values.put("profileItems", profileItems);

        if (!profileItems.isEmpty()) {
            values.put("totalAmount", getTotalAmount(profileItems));
        }
        return values;
    }

}