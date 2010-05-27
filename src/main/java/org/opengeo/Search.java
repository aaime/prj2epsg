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
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public class Search extends BaseResource {

    public Search(Context context, Request request, Response response) {
        super(context, request, response);

        // see if we have to search for a code
        String search = (String) request.getAttributes().get("terms");
       
        dataModel.put("showResults", Boolean.FALSE);
        dataModel.put("terms", search != null ? search : "");
        
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
    
    @Override
    public Variant getPreferredVariant() {
        if("json".equals(type)) {
            dataModel.remove("showResults");
            dataModel.remove("terms");
            dataModel.remove("message");
            return new Variant(MediaType.APPLICATION_JSON);
        }
        
        return super.getPreferredVariant();
    }

}
