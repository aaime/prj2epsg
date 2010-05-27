/* Copyright (c) 2010 Openplans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package org.opengeo.prj2epsg;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Represents a single EPSG code
 * @author aaime
 */
public class EPSGCode extends BaseResource {

    public EPSGCode(Context context, Request request, Response response) {
        super(context, request, response);

        try {
            String code = "EPSG:" + request.getAttributes().get("code");
            CoordinateReferenceSystem crs = CRS.decode(code);
            if(crs == null) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                dataModel.put("code", code);
                dataModel.put("name", crs.getName().getCode());
                dataModel.put("scope", toString(crs.getScope()));
                dataModel.put("remarks", toString(crs.getRemarks()));
                if(crs.getDomainOfValidity() != null) {
                    dataModel.put("area", toString(crs.getDomainOfValidity().getDescription()));
                } else {
                    dataModel.put("area", "-");
                }
                dataModel.put("wkt", crs.toString());
            }
        } catch (FactoryException e) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    String toString(InternationalString is) {
        if(is == null) {
            return "-";
        } else  {
            return is.toString();
        }
    }
}
