package org.opengeo;

import java.util.Arrays;
import java.util.Collections;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

public class Home extends HTMLResource {

    public Home(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_HTML));

        // see if we have to search for a code
        String search = (String) request.getAttributes().get("search");
        
        dataModel.put("showResults", Boolean.FALSE);
        dataModel.put("search", search != null ? search : "");
        
        if(search != null) {
            dataModel.put("showResults", Boolean.TRUE);
            dataModel.put("codes", Collections.emptyList());
            try {
                CoordinateReferenceSystem crs = CRS.parseWKT(search);
                Integer code = CRS.lookupEpsgCode(crs, true);
                if(code != null) {
                    dataModel.put("message", "Found the following EPSG matches");
                    dataModel.put("codes", Arrays.asList(code.toString()));
                } else {
                    dataModel.put("message", "Could not find a corresponding EPSG code");
                }
            } catch(FactoryException e) {
                dataModel.put("message", "Invalid WKT syntax: " + e.getMessage());
            }
        }
    }

    
}
