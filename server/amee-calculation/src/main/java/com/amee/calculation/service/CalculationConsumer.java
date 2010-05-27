package com.amee.calculation.service;

import com.amee.domain.APIVersion;
import com.amee.domain.data.DataItem;
import com.amee.domain.sheet.Choice;
import com.amee.domain.sheet.Choices;
import com.amee.messaging.StringRpcMessageConsumer;
import com.amee.messaging.config.ExchangeConfig;
import com.amee.messaging.config.QueueConfig;
import com.amee.platform.science.Amounts;
import com.amee.platform.science.CO2Amount;
import com.amee.platform.science.ReturnValues;
import com.amee.service.data.DataService;
import com.amee.service.environment.EnvironmentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CalculationConsumer extends StringRpcMessageConsumer {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    @Qualifier("calculationExchange")
    private ExchangeConfig exchangeConfig;

    @Autowired
    @Qualifier("calculationQueue")
    private QueueConfig queueConfig;

    @Transactional(readOnly = true)
    protected String handle(String message) {
        try {
            // Setup JSONObjects.
            JSONObject inbound = new JSONObject(message);
            JSONObject outbound = new JSONObject();
            // Get the DataItem.
            String dataItemUid = inbound.getString("dataItemUid");
            DataItem dataItem = dataService.getDataItem(environmentService.getEnvironmentByName("AMEE"), dataItemUid);
            if (dataItem != null) {
                // Prepare the value choices.
                Choices userValueChoices = dataService.getUserValueChoices(dataItem, APIVersion.TWO);
                userValueChoices.merge(getParameters(inbound));
                // Do the calculation
                ReturnValues amounts = calculationService.calculate(dataItem, userValueChoices, APIVersion.TWO);
                outbound.put("result", amounts);
            } else {
                outbound.put("error", "DataItem not found.");
            }
            return outbound.toString();
        } catch (JSONException e) {
            log.warn("handle() Caught JSONException: " + e.getMessage());
            return "{\"error\": \"Could not parse JSON.\"}";
        }
    }

    protected List<Choice> getParameters(JSONObject inbound) throws JSONException {
        List<Choice> parameters = new ArrayList<Choice>();
        JSONObject inboundParameters = inbound.getJSONObject("parameters");
        Iterator i = inboundParameters.keys();
        while (i.hasNext()) {
            String name = (String) i.next();
            parameters.add(new Choice(name, inboundParameters.getString(name)));
        }
        return parameters;
    }

    @Override
    public ExchangeConfig getExchangeConfig() {
        return exchangeConfig;
    }

    @Override
    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    @Override
    public String getBindingKey() {
        return getQueueConfig().getName();
    }
}