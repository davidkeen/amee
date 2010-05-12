package com.amee.platform.search;

import org.apache.lucene.search.Query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataCategoryFilter implements Serializable {

    private Map<String, Query> queries = new HashMap<String, Query>();

    public DataCategoryFilter() {
        super();
    }

    public Query getUid() {
        return getQueries().get("uid");
    }

    public void setUid(Query uid) {
        getQueries().put("uid", uid);
    }

    public Query getName() {
        return getQueries().get("name");
    }

    public void setName(Query name) {
        getQueries().put("name", name);
    }

    public Query getPath() {
        return getQueries().get("path");
    }

    public void setPath(Query path) {
        getQueries().put("path", path);
    }

    public Query getFullPath() {
        return getQueries().get("fullPath");
    }

    public void setFullPath(Query fullPath) {
        getQueries().put("fullPath", fullPath);
    }

    public Query getWikiName() {
        return getQueries().get("wikiName");
    }

    public void setWikiName(Query wikiName) {
        getQueries().put("wikiName", wikiName);
    }

    public Query getWikiDoc() {
        return getQueries().get("wikiDoc");
    }

    public void setWikiDoc(Query wikiDoc) {
        getQueries().put("wikiDoc", wikiDoc);
    }

    public Query getProvenance() {
        return getQueries().get("provenance");
    }

    public void setProvenance(Query provenance) {
        getQueries().put("provenance", provenance);
    }

    public Query getAuthority() {
        return getQueries().get("authority");
    }

    public void setAuthority(Query authority) {
        getQueries().put("authority", authority);
    }

    public Query getParentUid() {
        return getQueries().get("parentUid");
    }

    public void setParentUid(Query parentUid) {
        getQueries().put("parentUid", parentUid);
    }

    public Query getParentWikiName() {
        return getQueries().get("parentWikiName");
    }

    public void setParentWikiName(Query parentWikiName) {
        getQueries().put("parentWikiName", parentWikiName);
    }

    public Query getItemDefinitionUid() {
        return getQueries().get("itemDefinitionUid");
    }

    public void setItemDefinitionUid(Query itemDefinitionUid) {
        getQueries().put("itemDefinitionUid", itemDefinitionUid);
    }

    public Query getItemDefinitionName() {
        return getQueries().get("itemDefinitionName");
    }

    public void setItemDefinitionName(Query itemDefinitionName) {
        getQueries().put("itemDefinitionName", itemDefinitionName);
    }

    public Map<String, Query> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, Query> queries) {
        this.queries = queries;
    }
}
