package org.opengeo.prj2epsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public class Search extends BaseResource {

    static Directory LUCENE_INDEX;

    public enum SearchMode {
        wkt, keywords
    };

    public Search(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        // see if we have to search for a code
        String terms = (String) request.getAttributes().get("terms");
        String modeKey = (String) request.getAttributes().get("mode");
        SearchMode mode = SearchMode.wkt;
        if (modeKey != null) {
            mode = SearchMode.valueOf(modeKey);
        }

        dataModel.put("showResults", Boolean.FALSE);
        dataModel.put("terms", terms != null ? terms : "");
        dataModel.put("selection", buildSelectionMap(mode));

        if (terms != null) {
            dataModel.put("showResults", Boolean.TRUE);
            dataModel.put("codes", Collections.emptyList());
            if (mode == SearchMode.wkt) {
                lookupFromWkt(terms);
            } else if (mode == SearchMode.keywords) {
                lookupFromLucene(terms);
            }
        }
    }

    private Map<String, String> buildSelectionMap(SearchMode mode) {
        Map<String, String> selection = new HashMap<String, String>();
        for(SearchMode sm : SearchMode.values()) {
           if(sm == mode) { 
               selection.put(sm.name(), "selected=\"selected\"");
           } else {
               selection.put(sm.name(), "");
           }
        }
        return selection;
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
            dataModel.put("message", "Found the following EPSG matches (sorted by relevance, " + hits.length + " out of " + collector.getTotalHits() + ")");
            dataModel.put("codes", codes);
        } catch (Exception e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }

    }

    private void lookupFromWkt(String terms) {
        try {
            CoordinateReferenceSystem crs = CRS.parseWKT(terms);
            Integer code = CRS.lookupEpsgCode(crs, true);
            if (code != null) {
                dataModel.put("message", "Found the following EPSG matches");
                dataModel.put("codes", Arrays.asList(asCRSMap(String.valueOf(code), crs)));
            } else {
                dataModel.put("message", "Could not find a corresponding EPSG code");
            }
        } catch (FactoryException e) {
            dataModel.put("message", "Invalid WKT syntax: " + e.getMessage());
        }
    }

    Map<String, String> asCRSMap(String code, CoordinateReferenceSystem crs) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("code", code);
        map.put("name", crs.getName().getCode());
        return map;
    }

    @Override
    public Variant getPreferredVariant() {
        if ("json".equals(type)) {
            dataModel.remove("showResults");
            dataModel.remove("terms");
            dataModel.remove("message");
            dataModel.remove("selection");
            return new Variant(MediaType.APPLICATION_JSON);
        }

        return super.getPreferredVariant();
    }

}
