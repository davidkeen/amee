package com.jellymold.kiwi.environment;

import com.jellymold.kiwi.Environment;
import com.jellymold.utils.BaseResource;
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

import java.util.Map;

@Component
@Scope("prototype")
public class EnvironmentResource extends BaseResource {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    public EnvironmentResource() {
        super();
    }

    public EnvironmentResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_ENVIRONMENT;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("EnvironmentResource");
        element.appendChild(environmentBrowser.getEnvironment().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getEnvironmentActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) {
        log.debug("storeRepresentation");
        if (environmentBrowser.getEnvironmentActions().isAllowModify()) {
            Form form = getForm();
            Environment environment = environmentBrowser.getEnvironment();
            // update values
            if (form.getNames().contains("name")) {
                environment.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("path")) {
                environment.setPath(form.getFirstValue("path"));
            }
            if (form.getNames().contains("description")) {
                environment.setDescription(form.getFirstValue("description"));
            }
            if (form.getNames().contains("itemsPerPage")) {
                try {
                    environment.setItemsPerPage(new Integer(form.getFirstValue("itemsPerPage")));
                } catch (NumberFormatException e) {
                    // swallow
                }
            }
            success();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    // TODO: Prevent deletion of the current environment?!
    @Override
    public void removeRepresentations() {
        if (environmentBrowser.getEnvironmentActions().isAllowDelete()) {
            environmentService.remove(environmentBrowser.getEnvironment());
            success();
        } else {
            notAuthorized();
        }
    }
}