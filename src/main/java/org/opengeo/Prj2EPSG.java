package org.opengeo;

import java.io.File;
import java.io.IOException;
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
import org.geotools.referencing.factory.epsg.ThreadedHsqlEpsgFactory;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

public class Prj2EPSG extends Application {

    static final Logger LOGGER = Logging.getLogger(Prj2EPSG.class);

    public Prj2EPSG() {
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
                
                if(!sentinel.exists()) {
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
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        // the home page
        router.attachDefault(Home.class);
        router.attach("/search.{type}", Search.class).extractQuery("terms", "terms", true).extractQuery("mode", "mode", true);
        router.attach("/search", Search.class).extractQuery("terms", "terms", true).extractQuery("mode", "mode", true);
        router.attach("/epsg/{code}.{type}", EPSGCode.class);
        router.attach("/epsg/{code}", EPSGCode.class);

        return router;
    }
}
