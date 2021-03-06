package com.amee.restlet.profile.builder.v1;

import com.amee.base.utils.XMLUtils;
import com.amee.domain.data.builder.v1.ItemValueBuilder;
import com.amee.domain.item.BaseItemValue;
import com.amee.domain.profile.builder.v1.ProfileItemBuilder;
import com.amee.restlet.profile.ProfileItemValueResource;
import com.amee.restlet.profile.builder.IProfileItemValueResourceBuilder;
import com.amee.service.item.DataItemServiceImpl;
import com.amee.service.item.ProfileItemServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

@Service("v1ProfileItemValueResourceBuilder")
public class ProfileItemValueResourceBuilder implements IProfileItemValueResourceBuilder {

    @Autowired
    private ProfileItemServiceImpl profileItemService;

    @Autowired
    private DataItemServiceImpl dataItemService;

    @Override
    public Element getElement(ProfileItemValueResource resource, Document document) {
        BaseItemValue itemValue = resource.getProfileItemValue();
        Element element = document.createElement("ProfileItemValueResource");
        element.appendChild(new ItemValueBuilder(itemValue, new ProfileItemBuilder(resource.getProfileItem(), dataItemService, profileItemService)).getElement(document));
        element.appendChild(XMLUtils.getElement(document, "Path", resource.getProfileItemValue().getFullPath()));
        element.appendChild(resource.getProfile().getIdentityElement(document));
        return element;
    }

    @Override
    public Map<String, Object> getTemplateValues(ProfileItemValueResource resource) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("browser", resource.getProfileBrowser());
        values.put("profileItemValue", resource.getProfileItemValue());
        values.put("node", resource.getProfileItemValue());
        values.put("profileItem", resource.getProfileItem());
        values.put("profile", resource.getProfile());
        return values;
    }

    @Override
    public org.apache.abdera.model.Element getAtomElement(ProfileItemValueResource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject getJSONObject(ProfileItemValueResource resource) throws JSONException {
        JSONObject obj = new JSONObject();
        BaseItemValue itemValue = resource.getProfileItemValue();
        obj.put("itemValue", new ItemValueBuilder(itemValue, new ProfileItemBuilder(resource.getProfileItem(), dataItemService, profileItemService)).getJSONObject(true));
        obj.put("path", resource.getProfileItemValue().getFullPath());
        obj.put("profile", resource.getProfile().getIdentityJSONObject());
        return obj;
    }
}
