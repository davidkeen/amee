package com.jellymold.kiwi.auth;

import com.jellymold.kiwi.Site;
import com.jellymold.kiwi.User;
import com.jellymold.utils.BaseResource;
import com.jellymold.utils.domain.APIUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class SignInResource extends BaseResource implements Serializable {

    public final static String VIEW_SIGN_IN = "auth/signIn.ftl";

    @Autowired
    private AuthService authService;

    @Autowired
    Site site;

    // TODO: Springify
    // @Autowired(required = false)
    User user;

    public SignInResource() {
        super();
    }

    public SignInResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public String getTemplatePath() {
        return VIEW_SIGN_IN;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        if (user != null) {
            obj.put("user", user.getJSONObject(false));
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SignInResource");
        element.appendChild(APIUtils.getElement(document, "Next", AuthUtils.getNextUrl(getRequest(), getForm())));
        if (user != null) {
            element.appendChild(user.getElement(document, false));
        }
        return element;
    }

    @Override
    public void handleGet() {
        Request request = getRequest();
        Response response = getResponse();
        if (site.isSecureAvailable() && !request.getResourceRef().getSchemeProtocol().equals(Protocol.HTTPS)) {
            // bounce to HTTPS
            response.setRedirectRef("https://" +
                    request.getResourceRef().getHostDomain() +
                    "/auth/signIn?next=" + AuthUtils.getNextUrl(request));
            response.setStatus(Status.REDIRECTION_FOUND);
        } else {
            super.handleGet();
        }
    }

    public boolean allowPost() {
        return true;
    }

    public void post(Representation entity) {
        put(entity);
    }

    public boolean allowPut() {
        return true;
    }

    public void put(Representation entity) {
        Form form = getForm();
        User sampleUser;
        String authToken;
        if (form.getNames().contains("username")) {
            // deal with sign in
            String nextUrl = AuthUtils.getNextUrl(getRequest(), getForm());
            sampleUser = new User();
            sampleUser.setUsername(form.getFirstValue("username"));
            sampleUser.setPassword(form.getFirstValue("password"));
            authToken = authService.authenticate(sampleUser, getRequest().getClientInfo().getAddress());
            if (authToken != null) {
                // signed in
                AuthUtils.addAuthCookie(getResponse(), authToken);
                AuthUtils.addAuthHeader(getResponse(), authToken);
                user = authService.getUser();
                // different response for API calls
                if (isStandardWebBrowser()) {
                    // go to 'next' page - which IS a protected resource
                    if (!nextUrl.endsWith("/auth/signIn")) {
                        success(nextUrl);
                    } else {
                        success("/auth");
                    }
                } else {
                    // return a response for API calls
                    super.handleGet();
                }
            } else {
                // not signed in
                AuthUtils.discardAuthCookie(getResponse());
                // show auth page again
                success("/auth/signIn?next=" + nextUrl);
            }
        } else {
            badRequest();
        }
    }
}