package org.opengeo;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class Start {
    static final Logger LOG = Logger.getLogger("Start");

    public static void main(String[] args) {
        Server jettyServer = null;

        try {
            jettyServer = new Server();

            SocketConnector conn = new SocketConnector();
            String portVariable = System.getProperty("jetty.port");
            conn.setPort(8080);
            jettyServer.setConnectors(new Connector[] { conn });

            WebAppContext wah = new WebAppContext();
            wah.setContextPath("/prj2epsg");
            wah.setWar("src/main/webapp");
            jettyServer.setHandler(wah);
            wah.setTempDirectory(new File("target/work"));

            jettyServer.start();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not start the Jetty server: " + e.getMessage(), e);

            if (jettyServer != null) {
                try {
                    jettyServer.stop();
                } catch (Exception e1) {
                    LOG.log(Level.SEVERE,
                            "Unable to stop the " + "Jetty server:" + e1.getMessage(), e1);
                }
            }
        }
    }
}
