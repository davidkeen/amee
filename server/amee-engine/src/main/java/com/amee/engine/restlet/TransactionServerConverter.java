package com.amee.engine.restlet;

import com.amee.base.transaction.TransactionController;
import com.amee.base.utils.ThreadBeanHolder;
import com.amee.domain.DataItemService;
import com.amee.domain.LocaleService;
import com.amee.domain.MetadataService;
import com.amee.domain.ProfileItemService;
import com.amee.domain.profile.CO2CalculationService;
import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;
import org.restlet.Context;
import org.springframework.context.ApplicationContext;

public class TransactionServerConverter extends HttpServerConverter {

    private ApplicationContext springContext;
    private TransactionController transactionController;

    public TransactionServerConverter(Context context) {
        super(context);
        springContext = (ApplicationContext) context.getAttributes().get("springContext");
        transactionController = (TransactionController) context.getAttributes().get("transactionController");
    }

    @Override
    public HttpRequest toRequest(HttpServerCall httpCall) {
        // Clear the ThreadBeanHolder at the start of each request.
        ThreadBeanHolder.clear();
        // Store commonly used services.
        ThreadBeanHolder.set(DataItemService.class, (DataItemService) springContext.getBean("dataItemServiceImpl"));
        ThreadBeanHolder.set(ProfileItemService.class, (ProfileItemService) springContext.getBean("profileItemServiceImpl"));
        ThreadBeanHolder.set(LocaleService.class, springContext.getBean(LocaleService.class));
        ThreadBeanHolder.set(MetadataService.class, springContext.getBean(MetadataService.class));
        ThreadBeanHolder.set(CO2CalculationService.class, (CO2CalculationService) springContext.getBean("calculationService"));
        // Pass request through.
        return super.toRequest(httpCall);
    }

    @Override
    public void commit(HttpResponse response) {
        // Commit the response.
        super.commit(response);
        // End transaction / entity manager.
        transactionController.afterCommit();
        // Clear the ThreadBeanHolder at the end of each request.
        ThreadBeanHolder.clear();
    }
}