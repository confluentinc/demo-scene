package io.confluent.kpay.rest_iq;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroRestService {
    private Logger log = LoggerFactory.getLogger(MicroRestService.class);

    private Server jettyServer;

    /**
     * Start an embedded Jetty Server on the given port
     * @param hostAndPortString
     * @throws Exception if jetty can't startProcessors
     */
    public void start(Object instance, final String hostAndPortString) {
        log.info("Starting RestEndpoint on:" + hostAndPortString + " Instance:" + instance);

        String[] hostAndPort = hostAndPortString.split(":");

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server();
        jettyServer.setHandler(context);

        final ResourceConfig rc = new ResourceConfig();
        rc.register(instance);
        rc.register(JacksonFeature.class);

        log = LoggerFactory.getLogger(instance.getClass());

        final ServletContainer sc = new ServletContainer(rc);
        final ServletHolder holder = new ServletHolder(sc);
        context.addServlet(holder, "/*");

        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setHost(hostAndPort[0]);
        connector.setPort(Integer.parseInt(hostAndPort[1]));
        jettyServer.addConnector(connector);

        try {
            context.start();
            jettyServer.start();
        } catch (final Exception exception) {
            log.error("Unavailable: " + hostAndPortString + " Instance:" + instance);
            exception.printStackTrace();
            throw new RuntimeException("Failed to startProcessors Jetty", exception);
        }
    }

    /**
     * Stop the Jetty Server
     * @throws Exception if jetty can't stop
     */
    public void stop()  {
        if (jettyServer != null) {
            try {
                jettyServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

}
