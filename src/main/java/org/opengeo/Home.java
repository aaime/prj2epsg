package org.opengeo;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

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
