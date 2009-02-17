package com.jellymold.engine;

import com.jellymold.kiwi.UserPasswordToMD5;
import com.jellymold.kiwi.environment.ScheduledTaskManager;
import com.jellymold.utils.cache.CacheHelper;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.ext.seam.TransactionController;
import org.restlet.service.LogService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Engine implements WrapperListener, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    protected ApplicationContext springContext;
    protected TransactionController transactionController;
    protected Component container;
    protected String serverName = "localhost";

    public Engine() {
        super();
    }

    public Engine(String serverName) {
        this();
        this.serverName = serverName;
    }

    public static void main(String[] args) {
        start(new Engine(), args);
    }

    protected static void start(WrapperListener wrapperListener, String[] args) {
        WrapperManager.start(wrapperListener, args);
    }

    public Integer start(String[] args) {

        parseOptions(args);

        log.debug("Starting Engine...");

        // initialise Spring ApplicationContext
        springContext = new ClassPathXmlApplicationContext(new String[]{
                "applicationContext.xml",
                "applicationContext-container.xml",
                "applicationContext-application-*.xml",
                "applicationContext-skins.xml"});

        // obtain the Restlet container
        container = ((Component) springContext.getBean("ameeContainer"));

        // initialise TransactionController (for controlling Spring)
        transactionController = (TransactionController) springContext.getBean("transactionController");

        // wrap start callback
        transactionController.begin(true);
        onStart();
        transactionController.end();

        // configure Restlet server (ajp, http, etc)
        // TODO: try and do this in Spring XML config
        Server ajpServer = ((Server) springContext.getBean("ameeServer"));
        ajpServer.getContext().getAttributes()
                .put("transactionController", transactionController); // used in TransactionServerConverter

        // configure Restlet logging to log on a single line
        // TODO: try and do this in Spring XML config
        LogService logService = container.getLogService();
        logService.setLogFormat("[IP:{cia}] [M:{m}] [S:{S}] [PATH:{rp}] [UA:{cig}] [REF:{fp}]");
        Logger logger = Logger.getLogger("org.restlet");
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new Formatter() {
            public String format(LogRecord record) {
                return "[org.restlet]" + record.getMessage() + "\n";
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(ch);

        try {
            // get things going
            container.start();
            log.debug("...Engine started.");
        } catch (Exception e) {
            log.fatal("caught Exception: " + e);
            e.printStackTrace();
            return null;
        }

        return null;
    }

    protected void parseOptions(String[] args) {

        CommandLine line = null;
        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        // define serverName option
        Option serverNameOpt = OptionBuilder.withArgName("serverName")
                .hasArg()
                .withDescription("The server name")
                .create("serverName");
        serverNameOpt.setRequired(true);
        options.addOption(serverNameOpt);

        // parse the options
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            new HelpFormatter().printHelp("java com.jellymold.engine.Engine", options);
            System.exit(-1);
        }

        // serverName
        if (line.hasOption(serverNameOpt.getOpt())) {
            serverName = line.getOptionValue(serverNameOpt.getOpt());
        }
    }

    protected void onStart() {
        // TODO: TEMPORARY CODE!!!! Remove once all databases have been migrated. Get rid of UserPasswordToMD5 too.
        if (System.getProperty("amee.userPasswordToMD5") != null) {
            UserPasswordToMD5 userPasswordToMD5 =
                    (UserPasswordToMD5) springContext.getBean("userPasswordToMD5");
            userPasswordToMD5.updateUserPasswordToMD5(false);
        }
        // TODO: scheduled tasks are disabled until this is needed
        // start scheduled tasks
        // ScheduledTaskManager scheduledTaskManager = (ScheduledTaskManager) springContext.getBean("scheduledTaskManager");
        // scheduledTaskManager.setServerName(serverName);
        // scheduledTaskManager.onStart();
    }

    protected void onShutdown() {
        // TODO: scheduled tasks are disabled until this is needed
        // shutdown scheduled tasks
        // ScheduledTaskManager scheduledTaskManager = (ScheduledTaskManager) springContext.getBean("scheduledTaskManager");
        // scheduledTaskManager.onShutdown();
    }

    public int stop(int exitCode) {
        try {
            log.debug("Stopping Engine...");
            // shutdown callback
            onShutdown();
            // shutdown Restlet container
            container.stop();
            // clean up cache
            CacheHelper.getInstance().getCacheManager().shutdown();
            log.debug("...Engine stopped.");
        } catch (Exception e) {
            log.error("Caught Exception: " + e);
        }
        return exitCode;
    }

    public void controlEvent(int event) {
        log.debug("controlEvent");
        // do nothing
    }
}
