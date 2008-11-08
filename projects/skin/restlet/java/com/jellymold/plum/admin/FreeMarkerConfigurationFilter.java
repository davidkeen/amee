package com.jellymold.plum.admin;

import com.jellymold.plum.FreeMarkerConfigurationService;
import freemarker.template.Configuration;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

public class FreeMarkerConfigurationFilter extends Filter {

    private final Log log = LogFactory.getLog(getClass());

    public FreeMarkerConfigurationFilter(Application application) {
        super(application.getContext(), application);
    }

    protected int beforeHandle(Request request, Response response) {
        log.debug("before handle");
        Configuration configuration;
        ApplicationContext springContext = (ApplicationContext) request.getAttributes().get("springContext");
        FreeMarkerConfigurationService freeMarkerConfigurationService =
                (FreeMarkerConfigurationService) springContext.getBean("freeMarkerConfigurationService");
        configuration = freeMarkerConfigurationService.getConfiguration();
        if (configuration != null) {
            request.getAttributes().put("freeMarkerConfiguration", configuration);
            // TODO: Springify
            // Contexts.getEventContext().set("freeMarkerConfiguration", configuration);
            return CONTINUE;
        }
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return STOP;
    }

    protected void afterHandle(Request request, Response response) {
        log.debug("after handle");
    }
}