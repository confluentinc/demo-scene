package io.confluent.kpay.util;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroRestService {
    private Logger log = LoggerFactory.getLogger(MicroRestService.class);

    private Server jettyServer;

    /**
     * Start an embedded Jetty Server on the given port
     * @param port    port to run the Server on
     * @throws Exception if jetty can't start
     */
    public void start(Object instance, final String host, final int port) throws Exception {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server();
        jettyServer.setHandler(context);

        final ResourceConfig rc = new ResourceConfig();
        rc.register(instance);
        log = LoggerFactory.getLogger(instance.getClass());

        final ServletContainer sc = new ServletContainer(rc);
        final ServletHolder holder = new ServletHolder(sc);
        context.addServlet(holder, "/*");

        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setHost(host);
        connector.setPort(port);
        jettyServer.addConnector(connector);

        context.start();

        try {
            jettyServer.start();
        } catch (final java.net.SocketException exception) {
            log.error("Unavailable: " + host + ":" + port);
            throw new Exception(exception.toString());
        }
    }

    /**
     * Stop the Jetty Server
     * @throws Exception if jetty can't stop
     */
    public void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

}
