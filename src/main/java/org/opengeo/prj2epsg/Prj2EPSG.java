/* Copyright (c) 2010 Openplans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package org.opengeo.prj2epsg;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.epsg.ThreadedHsqlEpsgFactory;
import org.geotools.util.WeakCollectionCleaner;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.http.HttpResponse;

/**
 * The P4j2EPSG application
 * @author aaime
 */
public class Prj2EPSG extends Application {

    static final Logger LOGGER = Logging.getLogger(Prj2EPSG.class);
    
    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        // the home page
        router.attachDefault(Home.class);
        // search page
        router.attach("/search.{type}", Search.class).extractQuery("terms", "terms", true)
                .extractQuery("mode", "mode", true);
        router.attach("/search", Search.class).extractQuery("terms", "terms", true).extractQuery(
                "mode", "mode", true);
        // the single epsg code page
        router.attach("/epsg/{code}.{type}", EPSGCode.class);
        router.attach("/epsg/{code}", EPSGCode.class);
        // the interactive api documentation
        router.attach("/apidocs.html", APIDocs.class);
        // static files
        router.attach("/static", new org.restlet.Directory(getContext(), "war:///static"));
        

        return router;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        
        // basic referencing setups
        System.setProperty("org.geotools.referencing.forceXY", "true");
        Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");
        // setup the referencing tolerance to make it more tolerant to tiny differences
        // between projections (increases the chance of matching a random prj file content
        // to an actual EPSG code
        Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 1e-9);

        // HACK: java.util.prefs are awful. See
        // http://www.allaboutbalance.com/disableprefs. When the site comes
        // back up we should implement their better way of fixing the problem.
        System.setProperty("java.util.prefs.syncInterval", "5000000");

        // initialize the Lucene search index
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        File directory = new File(System.getProperty("java.io.tmpdir", "."), "Geotools");
        if (directory.isDirectory() || directory.mkdir()) {
            directory = new File(directory, "LuceneIndex-" + ThreadedHsqlEpsgFactory.VERSION);
            directory.mkdir();
        }
        if (directory.exists() && directory.isDirectory()) {
            try {
                Directory index = new SimpleFSDirectory(directory);
                File sentinel = new File(directory, "created.txt");

                if (!sentinel.exists()) {
                    LOGGER.info("Creating Lucene keywords search index");

                    // write out the index
                    IndexWriter w = new IndexWriter(index, analyzer, true,
                            IndexWriter.MaxFieldLength.UNLIMITED);
                    Set<String> codes = CRS.getSupportedCodes("EPSG");
                    for (String code : codes) {
                        try {
                            CoordinateReferenceSystem crs = CRS.decode("EPSG:" + code);
                            // turn crs into a document Lucene can index
                            Document doc = new Document();
                            doc.add(new Field("code", code, Field.Store.YES, Field.Index.ANALYZED));
                            doc.add(new Field("authority", crs.getName().getAuthority().toString(),
                                    Field.Store.YES, Field.Index.ANALYZED));
                            doc.add(new Field("name", crs.getName().getCode(), Field.Store.YES,
                                    Field.Index.ANALYZED));
                            if (crs.getRemarks() != null)
                                doc.add(new Field("remarks", crs.getRemarks().toString(),
                                        Field.Store.YES, Field.Index.ANALYZED));
                            doc.add(new Field("wkt", crs.toString(), Field.Store.YES,
                                    Field.Index.ANALYZED));
                            w.addDocument(doc);
                        } catch (Exception e) {
                            // it's ok, some codes we cannot handle
                        }
                    }
                    w.close();

                    sentinel.createNewFile();

                    LOGGER.info("Lucene keywords search index successfully created");
                }
                Search.LUCENE_INDEX = index;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "Could not initialize Lucene search indexes for keyword search");
            }
        }
        
        cleanupThreadLocals();
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        
        cleanupThreadLocals();
        
        try {
            LOGGER.info("Beginning GeoServer cleanup sequence");

            // the dreaded classloader
            ClassLoader webappClassLoader = getClass().getClassLoader();

            // unload all deferred authority factories so that we get rid of the timer tasks in them
            try {
                disposeAuthorityFactories(ReferencingFactoryFinder
                        .getCoordinateOperationAuthorityFactories(null));
            } catch (Throwable e) {
                LOGGER
                        .log(Level.WARNING, "Error occurred trying to dispose authority factories",
                                e);
            }
            try {
                disposeAuthorityFactories(ReferencingFactoryFinder.getCRSAuthorityFactories(null));
            } catch (Throwable e) {
                LOGGER
                        .log(Level.WARNING, "Error occurred trying to dispose authority factories",
                                e);
            }
            try {
                disposeAuthorityFactories(ReferencingFactoryFinder.getCSAuthorityFactories(null));
            } catch (Throwable e) {
                LOGGER
                        .log(Level.WARNING, "Error occurred trying to dispose authority factories",
                                e);
            }

            // kill the threads created by referencing
            WeakCollectionCleaner.DEFAULT.exit();
            DeferredAuthorityFactory.exit();
            CRS.reset("all");
            LOGGER.info("Shut down GT referencing threads ");
            // reset
            ReferencingFactoryFinder.reset();
            // CommonFactoryFinder.reset();
            // DataStoreFinder.reset();
            // DataAccessFinder.reset();
            LOGGER.info("Shut down GT  SPI ");

            // unload all of the jdbc drivers we have loaded. We need to store them and unregister
            // later to avoid concurrent modification exceptions
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            Set<Driver> driversToUnload = new HashSet<Driver>();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                try {
                    // the driver class loader can be null if the driver comes from the JDK, such as
                    // the
                    // sun.jdbc.odbc.JdbcOdbcDriver
                    ClassLoader driverClassLoader = driver.getClass().getClassLoader();
                    if (driverClassLoader != null && webappClassLoader.equals(driverClassLoader)) {
                        driversToUnload.add(driver);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            for (Driver driver : driversToUnload) {
                try {
                    DriverManager.deregisterDriver(driver);
                    LOGGER.info("Unregistered JDBC driver " + driver);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could now unload driver " + driver.getClass(), e);
                }
            }
            
            // GeoTools.exit();

            // flush all javabean introspection caches as this too can keep a webapp classloader
            // from being unloaded
            Introspector.flushCaches();
            LOGGER.info("Cleaned up javabean caches");

            // drop LUCENE index
            Search.LUCENE_INDEX.close();
            Search.LUCENE_INDEX = null;
            
            LOGGER.info("Full application shutdown complete");
        } catch (Throwable t) {
            // if anything goes south during the cleanup procedures I want to know what it is
            t.printStackTrace();
        }
    }

    private void disposeAuthorityFactories(Set<? extends AuthorityFactory> factories)
            throws FactoryException {
        for (AuthorityFactory af : factories) {
            if (af instanceof AbstractAuthorityFactory) {
                LOGGER.info("Disposing referencing factory " + af);
                ((AbstractAuthorityFactory) af).dispose();
            }
        }
    }
    
    @Override
    public void handle(Request request, Response response) {
        try {
            super.handle(request, response);
        } finally {
            cleanupThreadLocals();
        }
    }

    private void cleanupThreadLocals() {
        // GeoTools referencing ones
        CRS.cleanupThreadLocals();
        
        // restlet uses thead locals as well
        Response.setCurrent(null);
        Application.setCurrent(null);
        Context.setCurrent(null);
        HttpResponse.setCurrent(null);
    }

}
