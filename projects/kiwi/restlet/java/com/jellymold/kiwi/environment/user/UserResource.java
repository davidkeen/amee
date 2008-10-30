package com.jellymold.kiwi.environment.user;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.User;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.BaseResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Component
@Scope("prototype")
public class UserResource extends BaseResource {

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private SiteService siteService;

    @Autowired
    private Environment environment;

    public UserResource() {
        super();
    }

    public UserResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setUserUid(request.getAttributes().get("userUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getEnvironment() != null) && (environmentBrowser.getUser() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_USER;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("user", environmentBrowser.getUser());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getJSONObject());
        obj.put("user", environmentBrowser.getUser().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("UserResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getUser().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getUserActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicate username/password and enforce unique username per environment?
    // TODO: password validation
    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (environmentBrowser.getUserActions().isAllowModify()) {
            Form form = getForm();
            User user = environmentBrowser.getUser();
            boolean ok = true;
            // update values
            if (form.getNames().contains("name")) {
                user.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("username")) {
                if (form.getFirstValue("username").equalsIgnoreCase(user.getUsername())
                        || siteService.getUserByUsername(environmentBrowser.getEnvironment(), form.getFirstValue("username")) == null) {
                    user.setUsername(form.getFirstValue("username"));
                } else {
                    ok = false;
                }
            }

            if (ok) {
                if (form.getNames().contains("status")) {
                    user.setStatus(form.getFirstValue("status"));
                }
                if (form.getNames().contains("type")) {
                    user.setType(form.getFirstValue("type"));
                }
                if (form.getNames().contains("password")) {
                    String password = form.getFirstValue("password");
                    if ((password != null) && password.length() > 0) {
                        user.setPassword(password);
                    }
                }
                if (form.getNames().contains("email")) {
                    user.setEmail(form.getFirstValue("email"));
                }
                success();
            } else {
                badRequest();
            }
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    // TODO: do not allow delete affecting logged-in user...
    @Override
    public void delete() {
        if (environmentBrowser.getUserActions().isAllowDelete()) {
            siteService.remove(environmentBrowser.getUser());
            success();
        } else {
            notAuthorized();
        }
    }
}