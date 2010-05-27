package org.opengeo;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import freemarker.template.Configuration;

/**
 * Base class for resources using an HTML represenatation. A Freemarker template with the same name
 * of the resource will be used and will be given the contents of the {@link #dataModel} field as
 * data model.
 * 
 * @author Andrea Aime - OpenGeo
 * 
 */
public class HTMLResource extends Resource {
    protected Map<String, Object> dataModel = new HashMap<String, Object>();

    public HTMLResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(getClass(), "");
        return new TemplateRepresentation(getClass().getSimpleName().toLowerCase() + ".ftl",
                config, dataModel, MediaType.TEXT_HTML);
    }
}
