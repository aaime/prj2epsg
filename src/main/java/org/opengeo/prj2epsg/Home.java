/* Copyright (c) 2010 Openplans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package org.opengeo.prj2epsg;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * The home page, just redirects to "search"
 * @author aaime
 */
public class Home extends Resource {
    
    public Home(Context context, Request request, Response response) {
        String base = request.getOriginalRef().toString();
        String redirect;
        if(!base.endsWith("/")) {
            redirect =  base + "/search";
        } else {
            redirect =  base + "search";
        }
        
        response.redirectPermanent(redirect);
    }
}
