package com.jellymold.kiwi.auth;

import com.jellymold.utils.BaseResource;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class ProtectedResource extends BaseResource implements Serializable {

    public final static String VIEW_PROTECTED = "auth/protected.ftl";

    public ProtectedResource() {
        super();
    }

    public ProtectedResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public String getTemplatePath() {
        return VIEW_PROTECTED;
    }

    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return values;
    }
}