package com.amee.restlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.resource.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ComponentResource extends Resource {

    protected final Log log = LogFactory.getLog(getClass());

    // TODO: move these to a util class of their own
    // these are disabled because they require item links
    // "rss_0.9", "rss_0.91", "rss_0.92", "rss_0.93", "rss_0.94", "rss_1.0",
    public static final String[] FEED_TYPES_ARR = {"rss_2.0", "atom_0.3", "atom_1.0"};
    public static final List<String> FEED_TYPES = new ArrayList<String>(Arrays.asList(FEED_TYPES_ARR));
    public static final String DEFAULT_FEED_TYPE = "rss_2.0";

    public boolean isValid() {
        return true;
    }
}