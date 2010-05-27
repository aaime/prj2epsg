/* Copyright (c) 2010 Openplans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package org.opengeo.prj2epsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.geotools.referencing.CRS;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.Projection;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

/**
 * The search/results resource
 * @author aaime
 */
public class Search extends BaseResource {

    static final int PRJ_MAX_SIZE_KB = 4;
    
    static Directory LUCENE_INDEX;

    public enum SearchMode {
        wkt, keywords, mixed
    };

    public Search(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
        
        String terms = null;
        String modeKey = null;
        SearchMode mode = SearchMode.mixed;

        // parse the possible different forms, GET, POST and POST with file upload
        if(Method.GET.equals(request.getMethod())) {
            // see if we have to search for a code
            terms = (String) request.getAttributes().get("terms");
            modeKey = (String) request.getAttributes().get("mode");
        } else if(Method.POST.equals(request.getMethod())) {
            if(request.getEntity().getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
                Form form = request.getEntityAsForm();
                terms = (String) form.getFirstValue("terms");
                modeKey = (String) form.getFirstValue("mode");
            } else if(request.getEntity().getMediaType().getName().startsWith(MediaType.MULTIPART_FORM_DATA.getName())) {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(PRJ_MAX_SIZE_KB * 1024);

                try {
                    RestletFileUpload upload = new RestletFileUpload(factory);
                    String fileContents = null;
                    for (FileItem item : upload.parseRequest(getRequest())) {
                        if ("mode".equals(item.getFieldName())) {
                            modeKey = item.getString();
                        } else if ("prjfile".equals(item.getFieldName())) {
                            if (item.getSize() > 64 * 1024) {
                                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                                        "Maximum size for a .prj file is set to " + PRJ_MAX_SIZE_KB + "KB");
                            }
                            fileContents = item.getString();
                        } else if("terms".equals(item.getFieldName())) {
                            terms = item.getString();
                        }
                    }
                    if(fileContents != null && fileContents.length() > 0) {
                        terms = fileContents;
                    }
                } catch (FileUploadException e) {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
                }
            } else {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        
        
        if (modeKey != null) {
            mode = SearchMode.valueOf(modeKey);
        }

        dataModel.put("html_showResults", Boolean.FALSE);
        dataModel.put("html_terms", terms != null ? terms : "");
        dataModel.put("exact", Boolean.FALSE);
        
        if (terms != null) {
            dataModel.put("html_showResults", Boolean.TRUE);
            dataModel.put("codes", Collections.emptyList());
            if(mode == SearchMode.mixed) {
                lookupMixed(terms);
            } else if (mode == SearchMode.wkt) {
                lookupFromWkt(terms);
            } else if (mode == SearchMode.keywords) {
                lookupFromLucene(terms);
            }
        }
    }
    
    @Override
    public boolean allowPost() {
        return true;
    }
    
    @Override
    public void handlePost() {
        super.handleGet();
    }
    
    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        // nothing to do here
    }
    
    private void lookupFromLucene(String terms) throws ResourceException {
        try {
            // search the results
            Query q = new QueryParser(Version.LUCENE_30, "wkt", new StandardAnalyzer(
                    Version.LUCENE_30)).parse(terms);
            int hitsPerPage = 20;
            IndexSearcher searcher = new IndexSearcher(LUCENE_INDEX, true);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // accumulate them
            List<Map<String, String>> codes = new ArrayList<Map<String, String>>();
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                String code = d.get("code");
                codes.add(asCRSMap(code, CRS.decode("EPSG:" + code)));
            }
            dataModel.put("totalHits", collector.getTotalHits());
            dataModel.put("codes", codes);
        } catch (Exception e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }

    }
    
    private void lookupMixed(String terms) throws ResourceException {
        try {
            CoordinateReferenceSystem crs = CRS.parseWKT(terms);
            Integer code = CRS.lookupEpsgCode(crs, true);
            if (code != null) {
                dataModel.put("exact", Boolean.TRUE);
                dataModel.put("codes", Arrays.asList(asCRSMap(String.valueOf(code), crs)));
            } else {
                // we can parse but we don't get any result -> distill a set of
                // serch terms from the CRS and use Lucene search
                String distilledTerms = extractTermsFromCRS(crs);
                lookupFromLucene(distilledTerms);
            }
        } catch (FactoryException e) {
            // not wkt? let's try treat it as keywords then
            lookupFromLucene(terms);
        }
    }

    /**
     * Truns the CRS into a set of terms suitable for a keyword search
     * @param crs
     * @return
     */
    private String extractTermsFromCRS(CoordinateReferenceSystem crs) {
        StringBuilder sb = new StringBuilder();
        extractTermsFromIdentifiedObject(crs, sb);
        return sb.toString();
    }
    
    private void extractTermsFromIdentifiedObject(IdentifiedObject id, StringBuilder sb) {
        sb.append(id.getName().getCode()).append(" ");
        if(id instanceof CoordinateReferenceSystem) {
            CoordinateReferenceSystem crs = (CoordinateReferenceSystem) id;
            extractTermsFromIdentifiedObject(crs.getCoordinateSystem(), sb);
            if(crs instanceof ProjectedCRS) {
                ProjectedCRS pcrs = (ProjectedCRS) crs;
                extractTermsFromIdentifiedObject(pcrs.getBaseCRS(), sb);
                extractTermsFromIdentifiedObject(pcrs.getConversionFromBase(), sb);
            } else if(crs instanceof CompoundCRS) {
                CompoundCRS ccrs = (CompoundCRS) crs;
                for(CoordinateReferenceSystem child : ccrs.getCoordinateReferenceSystems()) {
                    extractTermsFromIdentifiedObject(child, sb);
                }
            }
        } else if(id instanceof Projection) {
            Projection p = (Projection) id;
            extractTermsFromIdentifiedObject(p.getMethod(), sb);
            ParameterValueGroup params = p.getParameterValues();
            for(GeneralParameterValue gpv : params.values()) {
                extractTermsFromIdentifiedObject(gpv.getDescriptor(), sb);
                if(gpv instanceof ParameterValue) {
                    Object value = ((ParameterValue) gpv).getValue();
                    if(value != null) {
                        sb.append(value).append(" ");
                    } 
                }
            }
        }
    }

    private void lookupFromWkt(String terms) {
        try {
            CoordinateReferenceSystem crs = CRS.parseWKT(terms);
            Integer code = CRS.lookupEpsgCode(crs, true);
            if (code != null) {
                dataModel.put("exact", Boolean.TRUE);
                dataModel.put("codes", Arrays.asList(asCRSMap(String.valueOf(code), crs)));
            } 
        } catch (FactoryException e) {
            dataModel.put("errors", "Invalid WKT syntax: " + e.getMessage());
        }
    }

    Map<String, String> asCRSMap(String code, CoordinateReferenceSystem crs) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("code", code);
        map.put("name", crs.getName().getCode());
        map.put("url", getRequest().getRootRef().toString() + "/" + "epsg/" + code);
        return map;
    }
   

    
}
