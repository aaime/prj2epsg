package org.opengeo.prj2epsg;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.json.JsonRepresentation;
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
public class BaseResource extends Resource {
    protected Map<String, Object> dataModel = new LinkedHashMap<String, Object>();

    protected String type;

    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));

        // grab the type if possible
        type = (String) request.getAttributes().get("type");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        MediaType mediaType = variant.getMediaType();
        if (mediaType.equals(MediaType.TEXT_HTML)) {
            Configuration config = new Configuration();
            config.setClassForTemplateLoading(getClass(), "");
            return new TemplateRepresentation(getClass().getSimpleName().toLowerCase() + ".ftl",
                    config, dataModel, MediaType.TEXT_HTML);
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject jo = new JSONObject();
                for (String key : dataModel.keySet()) {
                    Object value = dataModel.get(key);
                    jo.put(key, value);
                }
                return new JsonRepresentation(jo);
            } catch (JSONException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
            }
        }

        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown media type "
                + mediaType);
    }

    @Override
    public Variant getPreferredVariant() {
        if (type == null || "html".equals(type)) {
            return new Variant(MediaType.TEXT_HTML);
        } else if ("json".equals(type)) {
            dataModel.remove("showResults");
            dataModel.remove("terms");
            dataModel.remove("message");
            return new Variant(MediaType.APPLICATION_JSON);
        }

        return null;
    }
}
