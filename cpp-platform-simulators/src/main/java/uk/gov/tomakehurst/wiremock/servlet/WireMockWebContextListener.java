package uk.gov.tomakehurst.wiremock.servlet;


import uk.gov.tomakehurst.wiremock.scheduler.DeleteFileScheduler;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.NotImplementedContainer;
import com.google.common.base.MoreObjects;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WireMockWebContextListener implements ServletContextListener {
    private static final String APP_CONTEXT_KEY = "WireMockApp";


    public WireMockWebContextListener() {
    }

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        boolean verboseLoggingEnabled = Boolean.parseBoolean(MoreObjects.firstNonNull(context.getInitParameter("verboseLoggingEnabled"), "true"));
        WireMockApp wireMockApp = new WireMockApp(new SimulatorsWarConfiguration(context), new NotImplementedContainer());
        context.setAttribute(APP_CONTEXT_KEY, wireMockApp);
        context.setAttribute(StubRequestHandler.class.getName(), wireMockApp.buildStubRequestHandler());
        context.setAttribute(AdminRequestHandler.class.getName(), wireMockApp.buildAdminRequestHandler());
        context.setAttribute("Notifier", new Slf4jNotifier(verboseLoggingEnabled));

        setUpSchedulerToDeleteOldFiles();
    }

    private void setUpSchedulerToDeleteOldFiles() {
        new DeleteFileScheduler().deleteOldFiles();
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
