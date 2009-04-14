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
package com.amee.domain.profile.builder.v2;

import com.amee.domain.APIUtils;
import com.amee.domain.Builder;
import com.amee.domain.core.CO2AmountUnit;
import com.amee.domain.core.DecimalCompoundUnit;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.builder.v2.ItemValueBuilder;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.profile.StartEndDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProfileItemBuilder implements Builder {

    private ProfileItem item;
    private DecimalCompoundUnit returnUnit = CO2AmountUnit.DEFAULT;

    public ProfileItemBuilder(ProfileItem item, DecimalCompoundUnit returnUnit) {
        this.item = item;
        this.returnUnit = returnUnit;
    }

    public ProfileItemBuilder(ProfileItem item) {
        this.item = item;
    }

    public void buildElement(JSONObject obj, boolean detailed) throws JSONException {
        obj.put("uid", item.getUid());
        obj.put("created", new StartEndDate(item.getCreated()));
        obj.put("modified", new StartEndDate(item.getModified()));

        obj.put("name", item.getName().isEmpty() ? JSONObject.NULL : item.getName());
        JSONArray itemValues = new JSONArray();
        for (ItemValue itemValue : item.getItemValues()) {
            itemValue.setBuilder(new ItemValueBuilder(itemValue));
            itemValues.put(itemValue.getJSONObject(false));
        }
        obj.put("itemValues", itemValues);
        if (detailed) {
            obj.put("environment", item.getEnvironment().getJSONObject(false));
            obj.put("itemDefinition", item.getItemDefinition().getJSONObject(false));
            obj.put("dataCategory", item.getDataCategory().getIdentityJSONObject());
        }
    }

    public void buildElement(Document document, Element element, boolean detailed) {
        element.setAttribute("uid", item.getUid());
        element.setAttribute("created", new StartEndDate(item.getCreated()).toString());
        element.setAttribute("modified", new StartEndDate(item.getModified()).toString());

        element.appendChild(APIUtils.getElement(document, "Name", item.getName()));
        Element itemValuesElem = document.createElement("ItemValues");
        for (ItemValue itemValue : item.getItemValues()) {
            itemValue.setBuilder(new ItemValueBuilder(itemValue));
            itemValuesElem.appendChild(itemValue.getElement(document, false));
        }
        element.appendChild(itemValuesElem);
        if (detailed) {
            element.appendChild(item.getEnvironment().getIdentityElement(document));
            element.appendChild(item.getItemDefinition().getIdentityElement(document));
            element.appendChild(item.getDataCategory().getIdentityElement(document));
        }
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        buildElement(obj, detailed);

        JSONObject amount = new JSONObject();
        amount.put("value", item.getAmount().convert(returnUnit).getValue());
        amount.put("unit", returnUnit);
        obj.put("amount", amount);

        obj.put("startDate", item.getStartDate().toString());
        obj.put("endDate", (item.getEndDate() != null) ? item.getEndDate().toString() : "");
        obj.put("dataItem", item.getDataItem().getIdentityJSONObject());

        // DataItem
        DataItem bDataItem = item.getDataItem();
        JSONObject dataItemObj = bDataItem.getIdentityJSONObject();
        dataItemObj.put("Label", bDataItem.getLabel());
        obj.put("dataItem", dataItemObj);

        if (detailed) {
            obj.put("profile", item.getProfile().getIdentityJSONObject());
        }
        return obj;
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ProfileItem");
        buildElement(document, element, detailed);

        Element amount = document.createElement("Amount");

        amount.setTextContent(item.getAmount().convert(returnUnit).toString());
        amount.setAttribute("unit", returnUnit.toString());
        element.appendChild(amount);
        
        element.appendChild(APIUtils.getElement(document, "StartDate", item.getStartDate().toString()));
        element.appendChild(APIUtils.getElement(document, "EndDate", (item.getEndDate() != null) ? item.getEndDate().toString() : ""));

        // DataItem
        DataItem bDataItem = item.getDataItem();
        Element dataItemElement = bDataItem.getIdentityElement(document);
        dataItemElement.appendChild(APIUtils.getElement(document, "Label", bDataItem.getLabel()));

        element.appendChild(dataItemElement);

        if (detailed) {
            element.appendChild(item.getProfile().getIdentityElement(document));
        }
        return element;
    }

}
