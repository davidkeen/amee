package com.amee.admin.restlet.auth;

import com.amee.restlet.BaseResource;
import com.amee.restlet.auth.AuthUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class ProtectedResource extends BaseResource implements Serializable {

    public final static String VIEW_PROTECTED = "auth/protected.ftl";

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAvailable(isValid());
    }

    @Override
    public String getTemplatePath() {
        return VIEW_PROTECTED;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return values;
    }
}