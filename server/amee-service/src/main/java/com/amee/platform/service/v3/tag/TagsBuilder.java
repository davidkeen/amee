package com.amee.platform.service.v3.tag;

import com.amee.base.resource.RequestWrapper;
import com.amee.base.resource.ResourceBuilder;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.tag.Tag;
import com.amee.service.tag.TagService;
import org.jdom.Document;
import org.jdom.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Service
@Scope("prototype")
public class TagsBuilder implements ResourceBuilder {

    private final static Map<String, Class> RENDERERS = new HashMap<String, Class>() {
        {
            put("application/json", TagsJSONRenderer.class);
            put("application/xml", TagsDOMRenderer.class);
        }
    };

    @Autowired
    private TagService tagService;

    @Autowired
    private TagResourceService tagResourceService;

    private TagsRenderer renderer;

    @Transactional(readOnly = true)
    public Object handle(RequestWrapper requestWrapper) {
        renderer = getRenderer(requestWrapper);
        handle(renderer, tagResourceService.getEntity(requestWrapper));
        renderer.ok();
        return renderer.getObject();
    }

    protected void handle(TagsRenderer renderer, IAMEEEntityReference entity) {
        for (Tag tag : tagService.getTags(entity)) {
            renderer.newTag(tag);
        }
    }

    public TagsRenderer getRenderer(RequestWrapper requestWrapper) {
        try {
            for (String acceptedMediaType : requestWrapper.getAcceptedMediaTypes()) {
                if (RENDERERS.containsKey(acceptedMediaType)) {
                    return (TagsRenderer) RENDERERS
                            .get(acceptedMediaType)
                            .getConstructor()
                            .newInstance();
                }
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Caught InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Caught IllegalAccessException: " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Caught NoSuchMethodException: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Caught InvocationTargetException: " + e.getMessage(), e);
        }
        throw new RuntimeException("TODO");
    }

    public String getMediaType() {
        throw new RuntimeException("Boo!");
    }

    public interface TagsRenderer {

        public void ok();

        public void notAuthenticated();

        public void start();

        public void newTag(Tag tag);

        public Object getObject();
    }

    public static class TagsJSONRenderer implements TagsBuilder.TagsRenderer {

        private JSONObject rootObj;
        private JSONArray tagsArr;

        public TagsJSONRenderer() {
            super();
            start();
        }

        public void start() {
            rootObj = new JSONObject();
            tagsArr = new JSONArray();
            put(rootObj, "tags", tagsArr);
        }

        public void newTag(Tag tag) {
            JSONObject tagObj = new JSONObject();
            put(tagObj, "tag", tag.getTag());
            put(tagObj, "count", tag.getCount());
            tagsArr.put(tagObj);
        }

        public void ok() {
            put(rootObj, "status", "OK");
        }

        public void notAuthenticated() {
            put(rootObj, "status", "NOT_AUTHENTICATED");
        }

        protected JSONObject put(JSONObject o, String key, Object value) {
            try {
                return o.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }

        public JSONObject getObject() {
            return rootObj;
        }
    }

    public static class TagsDOMRenderer implements TagsRenderer {

        private Element rootElem;
        private Element tagsElem;

        public TagsDOMRenderer() {
            super();
            start();
        }

        public void start() {
            rootElem = new Element("Representation");
            tagsElem = new Element("Tags");
            rootElem.addContent(tagsElem);
        }

        public void newTag(Tag tag) {
            Element tagElem = new Element("Tag");
            tagElem.addContent(new Element("Tag").setText(tag.getTag()));
            tagElem.addContent(new Element("Count").setText("" + tag.getCount()));
            tagsElem.addContent(tagElem);
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void notAuthenticated() {
            rootElem.addContent(new Element("Status").setText("NOT_AUTHENTICATED"));
        }

        public Document getObject() {
            return new Document(rootElem);
        }
    }
}