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
package com.amee.restlet.environment;

import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEStatus;
import com.amee.domain.ValueDefinition;
import com.amee.domain.data.ItemDefinition;
import com.amee.domain.data.ItemDefinitionLocaleName;
import com.amee.domain.data.LocaleName;
import com.amee.restlet.AuthorizeResource;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class ItemDefinitionResource extends AuthorizeResource implements Serializable {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private DefinitionService definitionService;

    @Autowired
    private DefinitionBrowser definitionBrowser;

    @Override
    public void initialise(Context context, Request request, Response response) {
        super.initialise(context, request, response);
        definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (definitionBrowser.getItemDefinitionUid() != null);
    }

    @Override
    public List<AMEEEntity> getEntities() {
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(getActiveEnvironment());
        entities.add(definitionBrowser.getEnvironment());
        entities.add(definitionBrowser.getItemDefinition());
        return entities;
    }

    @Override
    public String getTemplatePath() {
        return DataConstants.VIEW_ITEM_DEFINITION;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions(definitionBrowser.getEnvironment());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", definitionBrowser);
        values.put("environment", definitionBrowser.getEnvironment());
        values.put("itemDefinition", definitionBrowser.getItemDefinition());
        values.put("valueDefinitions", valueDefinitions.isEmpty() ? null : valueDefinitions);
        values.put("availableLocales", LocaleName.AVAILABLE_LOCALES.keySet());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("itemDefinition", definitionBrowser.getItemDefinition().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("ItemDefinitionResource");
        element.appendChild(definitionBrowser.getItemDefinition().getElement(document));
        return element;
    }

    @Override
    public void doStore(Representation entity) {

        log.debug("doStore()");

        ItemDefinition itemDefinition = definitionBrowser.getItemDefinition();
        Form form = getForm();

        // Parse any submitted locale names
        for (String name : form.getNames()) {
            if (name.startsWith("name_")) {

                String locale = name.substring(name.indexOf("_") + 1);
                String localeNameStr = form.getFirstValue(name);

                if (StringUtils.isBlank(localeNameStr) || !LocaleName.AVAILABLE_LOCALES.containsKey(locale)) {
                    badRequest(APIFault.INVALID_PARAMETERS);
                    return;
                }

                if (itemDefinition.getLocaleNames().containsKey(locale)) {
                    LocaleName localeName = itemDefinition.getLocaleNames().get(locale);
                    localeName.setName(localeNameStr);
                    if (form.getNames().contains("remove_name_" + locale)) {
                        localeName.setStatus(AMEEStatus.TRASH);
                    }
                } else {
                    LocaleName localeName =
                            new ItemDefinitionLocaleName(itemDefinition, LocaleName.AVAILABLE_LOCALES.get(locale), localeNameStr);
                    itemDefinition.addLocaleName(localeName);
                }
            }
        }

        Set<String> names = form.getNames();
        if (names.contains("name")) {
            itemDefinition.setName(form.getFirstValue("name"));
        }
        if (form.getNames().contains("skipRecalculation")) {
            itemDefinition.setSkipRecalculation(Boolean.valueOf(form.getFirstValue("skipRecalculation")));
        }
        if (names.contains("drillDown")) {
            itemDefinition.setDrillDown(form.getFirstValue("drillDown"));
        }

        success();
    }

    @Override
    public void doRemove() {
        log.debug("doRemove");
        definitionService.remove(definitionBrowser.getItemDefinition());
        success();
    }
}