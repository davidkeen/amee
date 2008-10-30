package com.jellymold.kiwi.environment.site;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Site;
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
public class SiteResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private Environment environment;

    public SiteResource() {
        super();
    }

    public SiteResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setSiteUid(request.getAttributes().get("siteUid").toString());
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getSite() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_SITE;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("site", environmentBrowser.getSite());
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("environment", environmentBrowser.getEnvironment().getIdentityJSONObject());
        obj.put("site", environmentBrowser.getSite().getJSONObject());
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SiteResource");
        element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
        element.appendChild(environmentBrowser.getSite().getElement(document));
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getSiteActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    // TODO: prevent duplicate server names
    @Override
    public void put(Representation entity) {
        log.debug("put");
        if (environmentBrowser.getSiteActions().isAllowModify()) {
            Form form = getForm();
            Site site = environmentBrowser.getSite();
            // update values
            if (form.getNames().contains("name")) {
                site.setName(form.getFirstValue("name"));
            }
            if (form.getNames().contains("description")) {
                site.setDescription(form.getFirstValue("description"));
            }
            if (form.getNames().contains("serverName")) {
                site.setServerName(form.getFirstValue("serverName"));
            }
            if (form.getNames().contains("serverAddress")) {
                site.setServerAddress(form.getFirstValue("serverAddress"));
            }
            if (form.getNames().contains("serverPort")) {
                site.setServerPort(form.getFirstValue("serverPort"));
            }
            if (form.getNames().contains("serverScheme")) {
                site.setServerScheme(form.getFirstValue("serverScheme"));
            }
            if (form.getNames().contains("authCookieDomain")) {
                site.setAuthCookieDomain(form.getFirstValue("authCookieDomain"));
            }
            if (form.getNames().contains("secureAvailable")) {
                site.setSecureAvailable(Boolean.valueOf(form.getFirstValue("secureAvailable")));
            }
            if (form.getNames().contains("checkRemoteAddress")) {
                site.setCheckRemoteAddress(Boolean.valueOf(form.getFirstValue("checkRemoteAddress")));
            }
            if (form.getNames().contains("maxAuthDuration")) {
                try {
                    site.setMaxAuthDuration(new Long(form.getFirstValue("maxAuthDuration")));
                } catch (NumberFormatException e) {
                    // swallow
                }
            }
            if (form.getNames().contains("maxAuthDuration")) {
                try {
                    site.setMaxAuthDuration(new Long(form.getFirstValue("maxAuthDuration")));
                } catch (NumberFormatException e) {
                    // swallow
                }
            }
            if (form.getNames().contains("maxAuthIdle")) {
                try {
                    site.setMaxAuthIdle(new Long(form.getFirstValue("maxAuthIdle")));
                } catch (Exception e) {
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

    // TODO: Prevent deletion of the current site?!
    @Override
    public void delete() {
        if (environmentBrowser.getSiteActions().isAllowDelete()) {
            siteService.remove(environmentBrowser.getSite());
            success();
        } else {
            notAuthorized();
        }
    }
}