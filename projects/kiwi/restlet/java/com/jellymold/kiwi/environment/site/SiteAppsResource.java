package com.jellymold.kiwi.environment.site;

import com.jellymold.kiwi.App;
import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.SiteApp;
import com.jellymold.kiwi.app.AppService;
import com.jellymold.kiwi.environment.EnvironmentBrowser;
import com.jellymold.kiwi.environment.EnvironmentConstants;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.Pager;
import org.json.JSONArray;
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

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class SiteAppsResource extends BaseResource {

    @Autowired
    private SiteService siteService;

    @Autowired
    private EnvironmentBrowser environmentBrowser;

    @Autowired
    private AppService appService;

    @Autowired
    private Environment environment;

    private SiteApp newSiteApp;

    public SiteAppsResource() {
        super();
    }

    public SiteAppsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        environmentBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
        environmentBrowser.setSiteUid(request.getAttributes().get("siteUid").toString());
        setPage(request);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (environmentBrowser.getSite() != null);
    }

    @Override
    public String getTemplatePath() {
        return EnvironmentConstants.VIEW_SITE_APPS;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Pager pager = getPager(environment.getItemsPerPage());
        List<SiteApp> siteApps = siteService.getSiteApps(environmentBrowser.getSite(), pager);
        pager.setCurrentPage(getPage());
        Map<String, Object> values = super.getTemplateValues();
        values.put("browser", environmentBrowser);
        values.put("environment", environmentBrowser.getEnvironment());
        values.put("site", environmentBrowser.getSite());
        values.put("siteApps", siteApps);
        values.put("apps", siteService.getApps());
        values.put("pager", pager);
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<SiteApp> siteApps = siteService.getSiteApps(environmentBrowser.getSite(), pager);
            pager.setCurrentPage(getPage());
            obj.put("environment", environmentBrowser.getEnvironment().getIdentityJSONObject());
            obj.put("site", environmentBrowser.getSite().getIdentityJSONObject());
            JSONArray siteAppsArr = new JSONArray();
            for (SiteApp siteApp : siteApps) {
                siteAppsArr.put(siteApp.getJSONObject());
            }
            obj.put("siteApps", siteAppsArr);
            obj.put("pager", pager.getJSONObject());
        } else if (isPost()) {
            obj.put("siteApp", newSiteApp.getJSONObject());
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SiteAppsResource");
        if (isGet()) {
            Pager pager = getPager(environment.getItemsPerPage());
            List<SiteApp> siteApps = siteService.getSiteApps(environmentBrowser.getSite(), pager);
            pager.setCurrentPage(getPage());
            element.appendChild(environmentBrowser.getEnvironment().getIdentityElement(document));
            element.appendChild(environmentBrowser.getSite().getIdentityElement(document));
            Element sitesElement = document.createElement("SiteApps");
            for (SiteApp siteApp : siteApps) {
                sitesElement.appendChild(siteApp.getElement(document));
            }
            element.appendChild(sitesElement);
            element.appendChild(pager.getElement(document));
        } else if (isPost()) {
            element.appendChild(newSiteApp.getElement(document));
        }
        return element;
    }

    @Override
    public void handleGet() {
        log.debug("handleGet");
        if (environmentBrowser.getSiteAppActions().isAllowView()) {
            super.handleGet();
        } else {
            notAuthorized();
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    // TODO: prevent duplicate instances
    @Override
    public void post(Representation entity) {
        log.debug("post");
        if (environmentBrowser.getSiteAppActions().isAllowModify()) {
            Form form = getForm();
            // create new instance if submitted
            String appUid = form.getFirstValue("appUid");
            if (appUid != null) {
                App app = appService.getAppByUid(appUid);
                if (app != null) {
                    Site site = environmentBrowser.getSite();
                    newSiteApp = new SiteApp(app, site);
                    newSiteApp.setUriPattern(form.getFirstValue("uriPattern"));
                    newSiteApp.setSkinPath(form.getFirstValue("skinPath"));
                    newSiteApp.setDefaultApp(Boolean.valueOf(form.getFirstValue("defaultApp")));
                    newSiteApp.setEnabled(Boolean.valueOf(form.getFirstValue("enabled")));
                    site.add(newSiteApp);
                }
            }
            if (newSiteApp != null) {
                if (isStandardWebBrowser()) {
                    success();
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
}
