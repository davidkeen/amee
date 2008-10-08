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
package gc.carbon.profile;

import com.jellymold.kiwi.Environment;
import com.jellymold.sheet.Sheet;
import com.jellymold.utils.Pager;
import gc.carbon.profile.representation.ProfileCategoryRepresentation;
import gc.carbon.IRepresentationStrategy;
import gc.carbon.data.*;
import gc.carbon.path.PathItem;
import gc.carbon.path.PathItemService;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.XML;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Name("profileCategoryResource")
@Scope(ScopeType.EVENT)
public class ProfileCategoryResource extends BaseProfileResource implements Serializable {

    private final static Logger log = Logger.getLogger(ProfileCategoryResource.class);

    @In(create = true)
    private EntityManager entityManager;

    @In(create = true)
    private DataService dataService;

    @In(create = true)
    private ProfileService profileService;

    @In(create = true)
    private ProfileBrowser profileBrowser;

    @In(create = true)
    private PathItemService pathItemService;

    @In(create = true)
    private ProfileSheetService profileSheetService;

    @In(create = true)
    private Calculator calculator;

    @In
    private Environment environment;

    @In
    private PathItem pathItem;

    private ProfileItem profileItem;
    private List<ProfileItem> profileItems;

    //TODO - Use IOC container to wire-up with correct concrete implementation
    private IRepresentationStrategy representation = new ProfileCategoryRepresentation();

    public ProfileCategoryResource() {
        super();
    }

    public ProfileCategoryResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        profileBrowser.setDataCategoryUid(request.getAttributes().get("categoryUid").toString());
        profileBrowser.setProfileDate(request.getResourceRef().getQueryAsForm());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (profileBrowser.getDataCategory() != null);
    }

    @Override
    public String getTemplatePath() {
        return ProfileConstants.VIEW_PROFILE_CATEGORY;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Profile profile = profileBrowser.getProfile();
        DataCategory dataCategory = profileBrowser.getDataCategory();
        Sheet sheet = profileSheetService.getSheet(profile, dataCategory, profileBrowser.getProfileDate());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", profileBrowser);
        values.put("profile", profile);
        values.put("dataCategory", dataCategory);
        values.put("node", dataCategory);
        values.put("sheet", sheet);
        if (sheet != null) {
            values.put("totalAmountPerMonth", profileSheetService.getTotalAmountPerMonth(sheet));
        }
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        return representation.getJSONObject(this);
    }

    @Override
    public Element getElement(Document document) {
        return representation.getElement(this, document);
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (profileBrowser.getEnvironmentActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void post(Representation entity) {
        log.debug("post");
        postOrPut(entity);
    }

    @Override
    public void put(Representation entity) {
        log.debug("put");
        postOrPut(entity);
    }

    protected void postOrPut(Representation entity) {
        log.debug("postOrPut");
        if ((getRequest().getMethod().equals(Method.POST) && (profileBrowser.getProfileItemActions().isAllowCreate())) ||
                (getRequest().getMethod().equals(Method.PUT) && (profileBrowser.getProfileItemActions().isAllowModify()))) {
            profileItems = new ArrayList<ProfileItem>();
            MediaType mediaType = entity.getMediaType();
            if (MediaType.APPLICATION_XML.includes(mediaType)) {
                acceptXML(entity);
            } else if (MediaType.APPLICATION_JSON.includes(mediaType)) {
                acceptJSON(entity);
            } else {
                profileItem = acceptForm(getForm());
            }
            if ((profileItem != null) || !profileItems.isEmpty()) {
                // clear caches
                pathItemService.removePathItemGroup(profileBrowser.getProfile());
                profileSheetService.removeSheets(profileBrowser.getProfile());
                if (isStandardWebBrowser()) {
                    success(profileBrowser.getFullPath());
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }

    protected void acceptJSON(Representation entity) {
        log.debug("acceptJSON");
        ProfileItem profileItem;
        Form form;
        String key;
        JSONObject rootJSON;
        JSONArray profileItemsJSON;
        JSONObject profileItemJSON;
        try {
            rootJSON = new JSONObject(entity.getText());
            if (rootJSON.has("profileItems")) {
                profileItemsJSON = rootJSON.getJSONArray("profileItems");
                for (int i = 0; i < profileItemsJSON.length(); i++) {
                    profileItemJSON = profileItemsJSON.getJSONObject(i);
                    form = new Form();
                    for (Iterator iterator = profileItemJSON.keys(); iterator.hasNext();) {
                        key = (String) iterator.next();
                        form.add(key, profileItemJSON.getString(key));
                    }
                    profileItem = acceptForm(form);
                    if (profileItem != null) {
                        profileItems.add(profileItem);
                    } else {
                        log.warn("Profile Item not added/modified");
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            log.warn("Caught JSONException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Caught JSONException: " + e.getMessage(), e);
        }
    }

    protected void acceptXML(Representation entity) {
        log.debug("acceptXML");
        ProfileItem profileItem;
        Form form;
        org.dom4j.Element rootElem;
        org.dom4j.Element profileItemsElem;
        org.dom4j.Element profileItemElem;
        org.dom4j.Element profileItemValueElem;
        try {
            rootElem = XML.getRootElement(entity.getStream());
            if (rootElem.getName().equalsIgnoreCase("ProfileCategory")) {
                profileItemsElem = rootElem.element("ProfileItems");
                if (profileItemsElem != null) {
                    for (Object o1 : profileItemsElem.elements("ProfileItem")) {
                        profileItemElem = (org.dom4j.Element) o1;
                        form = new Form();
                        for (Object o2 : profileItemElem.elements()) {
                            profileItemValueElem = (org.dom4j.Element) o2;
                            form.add(profileItemValueElem.getName(), profileItemValueElem.getText());
                        }
                        profileItem = acceptForm(form);
                        if (profileItem != null) {
                            profileItems.add(profileItem);
                        } else {
                            log.warn("Profile Item not added");
                            return;
                        }
                    }
                }
            } else {
                log.warn("Profile Category not found");
            }
        } catch (DocumentException e) {
            log.warn("Caught DocumentException: " + e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Caught IOException: " + e.getMessage(), e);
        }
    }

    protected ProfileItem acceptForm(Form form) {
        DataCategory dataCategory;
        DataItem dataItem;
        ProfileItem profileItem = null;
        String uid;
        dataCategory = profileBrowser.getDataCategory();
        if (getRequest().getMethod().equals(Method.POST)) {
            // new ProfileItem
            uid = form.getFirstValue("dataItemUid");
            if (uid != null) {
                // the root DataCategory has an empty path
                if (dataCategory.getPath().length() == 0) {
                    // allow any DataItem for any DataCategory
                    dataItem = dataService.getDataItem(environment, uid);
                } else {
                    // only allow DataItems for specific DataCategory (not root)
                    dataItem = dataService.getDataItem(dataCategory, uid);
                }
                if (dataItem != null) {
                    // create new ProfileItem
                    profileItem = new ProfileItem(profileBrowser.getProfile(), dataItem);
                    profileItem = acceptProfileItem(form, profileItem);
                } else {
                    log.warn("Data Item not found");
                    profileItem = null;
                }
            } else {
                log.warn("dataItemUid not supplied");
                profileItem = null;
            }
        } else if (getRequest().getMethod().equals(Method.PUT)) {
            // update ProfileItem
            uid = form.getFirstValue("profileItemUid");
            if (uid != null) {
                // find existing Profile Item
                // the root DataCategory has an empty path
                if (dataCategory.getPath().length() == 0) {
                    // allow any ProfileItem for any DataCategory
                    profileItem = profileService.getProfileItem(profileBrowser.getProfile().getUid(), uid);
                } else {
                    // only allow ProfileItems for specific DataCategory (not root)
                    profileItem = profileService.getProfileItem(profileBrowser.getProfile().getUid(), dataCategory.getUid(), uid);
                }
                if (profileItem != null) {
                    // update existing Profile Item
                    profileItem = acceptProfileItem(form, profileItem);
                } else {
                    log.warn("Profile Item not found");
                    profileItem = null;
                }
            } else {
                log.warn("profileItemUid not supplied");
                profileItem = null;
            }
        }
        return profileItem;
    }

    protected ProfileItem acceptProfileItem(Form form, ProfileItem profileItem) {

        
        // alias deprecated params
        String startDate = form.getFirstValue("validFrom",form.getFirstValue("startDate"));

        // determine date for new ProfileItem
        profileItem.setStartDate(startDate);

        // determine name for new ProfileItem
        profileItem.setName(form.getFirstValue("name"));

        // determine if new ProfileItem is an end marker
        profileItem.setEnd(form.getFirstValue("end"));

        // see if ProfileItem already exists
        if (!profileService.isEquivilentProfileItemExists(profileItem)) {
            // save newProfileItem and do calculations
            entityManager.persist(profileItem);
            profileService.checkProfileItem(profileItem);
            // update item values if supplied
            Map<String, ItemValue> itemValues = profileItem.getItemValuesMap();
            for (String name : form.getNames()) {
                ItemValue itemValue = itemValues.get(name);
                if (itemValue != null) {
                    itemValue.setValue(form.getFirstValue(name));
                }
            }
            calculator.calculate(profileItem);
        } else {
            log.warn("Profile Item already exists");
            profileItem = null;
        }
        return profileItem;
    }

    @Override
    public boolean allowDelete() {
        // only allow delete for profile (a request to /profiles/{profileUid})
        return (pathItem.getPath().length() == 0);
    }

    @Override
    public void delete() {
        log.debug("delete");
        if (profileBrowser.getProfileActions().isAllowDelete()) {
            Profile profile = profileBrowser.getProfile();
            pathItemService.removePathItemGroup(profile);
            profileSheetService.removeSheets(profile);
            profileService.remove(profile);
            success("/profiles");
        } else {
            notAuthorized();
        }
    }

    public List<ProfileItem> getProfileItems() {
        return profileItems;
    }

    public ProfileItem getProfileItem() {
        return profileItem;
    }

    public ProfileSheetService getProfileSheetService() {
        return profileSheetService;
    }

    public ProfileBrowser getProfileBrowser() {
        return profileBrowser;
    }

    public PathItem getPathItem() {
        return pathItem;
    }

    public Pager getPager() {
        return getPager(profileBrowser.getItemsPerPage(getRequest()));
    }
}