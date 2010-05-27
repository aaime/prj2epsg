package org.opengeo;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

public class Home extends Resource {
    
    public Home(Context context, Request request, Response response) {
        response.redirectPermanent(new Reference(request.getRootRef(),  "search"));
    }
}
