/* Copyright (c) 2010 Openplans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package org.opengeo.prj2epsg;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

/**
 * API documentation with dynamic examples
 * @author aaime
 */
public class APIDocs extends BaseResource {

    public APIDocs(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().remove(new Variant(MediaType.APPLICATION_JSON));
    }

}
