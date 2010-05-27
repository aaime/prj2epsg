package org.opengeo;

import org.geotools.factory.Hints;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

public class Prj2EPSG extends Application {

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
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        // the home page
        router.attachDefault(Home.class).extractQuery("search", "search", true);
        router.attach("/epsg/{code}", EPSGCode.class);

        return router;
    }
}
