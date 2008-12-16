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
package gc.carbon.domain.profile.builder.v1;

import com.jellymold.utils.domain.APIUtils;
import gc.carbon.domain.data.builder.BuildableItemValue;
import gc.carbon.domain.data.builder.v1.ItemValueBuilder;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.Builder;
import gc.carbon.domain.Unit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

public class ProfileItemBuilder implements Builder {

    private static final String DAY_DATE = "yyyyMMdd";
    private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);

    private BuildableProfileItem item;
    private Unit returnUnit = ProfileItem.INTERNAL_COMPOUND_AMOUNT_UNIT;

    public ProfileItemBuilder(BuildableProfileItem item, Unit returnUnit) {
        this.item = item;
        this.returnUnit = returnUnit;
    }    public ProfileItemBuilder(BuildableProfileItem item) {
        this.item = item;
    }

    public void buildElement(JSONObject obj, boolean detailed) throws JSONException {
        obj.put("uid", item.getUid());
        obj.put("name", item.getDisplayName());
        JSONArray itemValues = new JSONArray();
        for (BuildableItemValue itemValue : item.getItemValues()) {
            itemValue.setBuilder(new ItemValueBuilder(itemValue));
            itemValues.put(itemValue.getJSONObject(false));
        }
        obj.put("itemValues", itemValues);
        if (detailed) {
            obj.put("created", item.getCreated());
            obj.put("modified", item.getModified());
            obj.put("environment", item.getEnvironment().getIdentityJSONObject());
            obj.put("itemDefinition", item.getItemDefinition().getIdentityJSONObject());
            obj.put("dataCategory", item.getDataCategory().getIdentityJSONObject());
        }
    }

    public void buildElement(Document document, Element element, boolean detailed) {
        element.setAttribute("uid", item.getUid());
        element.appendChild(APIUtils.getElement(document, "Name", item.getDisplayName()));
        Element itemValuesElem = document.createElement("ItemValues");
        for (BuildableItemValue itemValue : item.getItemValues()) {
            itemValue.setBuilder(new ItemValueBuilder(itemValue));
            itemValuesElem.appendChild(itemValue.getElement(document, false));
        }
        element.appendChild(itemValuesElem);
        if (detailed) {
            element.setAttribute("created", item.getCreated().toString());
            element.setAttribute("modified", item.getModified().toString());
            element.appendChild(item.getEnvironment().getIdentityElement(document));
            element.appendChild(item.getItemDefinition().getIdentityElement(document));
            element.appendChild(item.getDataCategory().getIdentityElement(document));
        }
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        buildElement(obj, detailed);
        obj.put("amountPerMonth", getAmount(item));
        obj.put("validFrom", DAY_DATE_FMT.format(item.getStartDate()));
        obj.put("end", Boolean.toString(item.isEnd()));
        obj.put("dataItem", item.getDataItem().getIdentityJSONObject());
        if (detailed) {
            obj.put("profile", item.getProfile().getIdentityJSONObject());
        }
        return obj;
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("ProfileItem");
        buildElement(document, element, detailed);
        element.appendChild(APIUtils.getElement(document, "AmountPerMonth", getAmount(item)));
        element.appendChild(APIUtils.getElement(document, "ValidFrom", DAY_DATE_FMT.format(item.getStartDate())));
        element.appendChild(APIUtils.getElement(document, "End", Boolean.toString(item.isEnd())));
        element.appendChild(item.getDataItem().getIdentityElement(document));
        if (detailed) {
            element.appendChild(item.getProfile().getIdentityElement(document));
        }
        return element;
    }

    private String getAmount(BuildableProfileItem item) {
        BigDecimal amount = item.getAmount();
        if (!returnUnit.equals(ProfileItem.INTERNAL_COMPOUND_AMOUNT_UNIT)) {
            amount = ProfileItem.INTERNAL_COMPOUND_AMOUNT_UNIT.convert(amount, returnUnit);
        }
        return amount.toString();
    }
}