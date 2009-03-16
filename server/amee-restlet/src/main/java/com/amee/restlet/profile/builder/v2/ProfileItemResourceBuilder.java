package com.amee.restlet.profile.builder.v2;

import com.amee.domain.APIUtils;
import com.amee.domain.data.CO2AmountUnit;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.builder.v2.ProfileItemBuilder;
import com.amee.restlet.profile.ProfileItemResource;
import com.amee.restlet.profile.builder.IProfileItemResourceBuilder;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.IRIElement;
import org.apache.abdera.model.Text;
import org.json.JSONException;
import org.json.JSONObject;
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
@Service("v2ProfileItemResourceBuilder")
public class ProfileItemResourceBuilder implements IProfileItemResourceBuilder {

    public JSONObject getJSONObject(ProfileItemResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        ProfileItem profileItem = resource.getProfileItem();
        setProfileItemBuilder(resource, profileItem);
        obj.put("profileItem", profileItem.getJSONObject(true));
        obj.put("path", resource.getPathItem().getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }


    public Element getElement(ProfileItemResource resource, Document document) {
        ProfileItem profileItem = resource.getProfileItem();
        setProfileItemBuilder(resource, profileItem);
        Element element = document.createElement("ProfileItemResource");
        element.appendChild(profileItem.getElement(document, true));
        element.appendChild(APIUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }

    public Map<String, Object> getTemplateValues(ProfileItemResource resource) {
        ProfileItem profileItem = resource.getProfileItem();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profile", profileItem.getProfile());
        values.put("profileItem", profileItem);
        values.put("node", profileItem);
        return values;
    }

    public org.apache.abdera.model.Element getAtomElement(ProfileItemResource resource) {

        AtomFeed atomFeed = AtomFeed.getInstance();

        ProfileItem profileItem = resource.getProfileItem();

        CO2AmountUnit returnUnit = resource.getProfileBrowser().getCo2AmountUnit();
        String amount = profileItem.getAmount().convert(returnUnit).toString();

        Entry entry = atomFeed.newEntry();
        entry.setBaseUri(resource.getRequest().getAttributes().get("previousHierachicalPart").toString());

        Text title = atomFeed.newTitle(entry);
        title.setText(profileItem.getDisplayName());
        Text subtitle = atomFeed.newSubtitle(entry);
        subtitle.setText(atomFeed.format(profileItem.getStartDate()) + ((profileItem.getEndDate() != null) ? " - " + atomFeed.format(profileItem.getEndDate()) : ""));

        atomFeed.addLinks(entry, "");

        IRIElement eid = atomFeed.newID(entry);
        eid.setText("urn:item:" + profileItem.getUid());

        entry.setPublished(profileItem.getStartDate());
        entry.setUpdated(profileItem.getStartDate());

        atomFeed.addDataItem(entry, profileItem.getDataItem());

        HCalendar content = new HCalendar();

        content.addSummary(amount + " " + returnUnit.toString());

        content.addStartDate(profileItem.getStartDate());
        if (profileItem.getEndDate() != null) {
            content.addEndDate(profileItem.getEndDate());
        }
        entry.setContentAsHtml(content.toString());

        atomFeed.addStartDate(entry, profileItem.getStartDate().toString());
        if (profileItem.getEndDate() != null) {
            atomFeed.addEndDate(entry, profileItem.getEndDate().toString());
        }

        atomFeed.addAmount(entry, amount, returnUnit.toString());

        atomFeed.addItemValuesWithLinks(entry, profileItem.getItemValues(), "");

        Category cat = atomFeed.newItemCategory(entry);
        cat.setTerm(profileItem.getDataItem().getUid());
        cat.setLabel(profileItem.getDataItem().getItemDefinition().getName());

        return entry;

    }
    
    private void setProfileItemBuilder(ProfileItemResource resource, ProfileItem pi) {
        if (resource.getProfileBrowser().requestedCO2InExternalUnit()) {
            pi.setBuilder(new ProfileItemBuilder(pi, resource.getProfileBrowser().getCo2AmountUnit()));
        } else {
            pi.setBuilder(new ProfileItemBuilder(pi));
        }
    }
}