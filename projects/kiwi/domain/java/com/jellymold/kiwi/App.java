package com.jellymold.kiwi;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.PersistentObject;
import com.jellymold.utils.domain.UidGen;
import com.jellymold.utils.domain.DatedObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * An App encapsulates a group of web resources into a logical group under a URI. Apps can be attached
 * to multiple Sites via SiteApps.
 * <p/>
 * Apps define a set of Targets, representing resources, and Actions which can be performed by Users within
 * the App.
 * <p/>
 * When deleting an App we need to ensure all relevant SiteApps are also removed. Actions and Targets should
 * be automatically removed but we need to deal with dependancies of Actions.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "APP")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class App implements DatedObject, Comparable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @Column(name = "NAME", length = 100, nullable = false)
    private String name = "";

    @Column(name = "DESCRIPTION", length = 1000, nullable = false)
    private String description = "";

    @Column(name = "AUTHENTICATION_REQUIRED", nullable = false)
    private Boolean authenticationRequired = false;

    @Column(name = "FILTER_NAMES", length = 1000, nullable = false)
    private String filterNames = "";

    @Column(name = "TARGET_BUILDER", length = 255, nullable = false)
    private String targetBuilder = "";

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("key")
    private Set<Action> actions = new HashSet<Action>();

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("uriPattern")
    private Set<Target> targets = new HashSet<Target>();

    @Column(name = "ALLOW_CLIENT_CACHE", nullable = false)
    private Boolean allowClientCache = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public App() {
        super();
        setUid(UidGen.getUid());
    }

    public App(String name) {
        this();
        setName(name);
    }

    public App(String name, String description) {
        this(name);
        setDescription(description);
    }

    public void add(Action action) {
        action.setApp(this);
        getActions().add(action);
    }

    public void remove(Action action) {
        getActions().remove(action);
    }

    public void add(Target target) {
        target.setApp(this);
        getTargets().add(target);
    }

    public void remove(Target target) {
        getTargets().remove(target);
    }

    public String toString() {
        return "App_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof App)) return false;
        App app = (App) o;
        return getName().equalsIgnoreCase(app.getName());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        App app = (App) o;
        return getName().compareToIgnoreCase(app.getName());
    }

    public int hashCode() {
        return getName().toLowerCase().hashCode();
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("description", getDescription());
        obj.put("filterNames", getFilterNames());
        obj.put("targetBuilder", getTargetBuilder());
        obj.put("allowClientCache", getAllowClientCache());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
        }
        return obj;
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("App");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "FilterNames", getFilterNames()));
        element.appendChild(APIUtils.getElement(document, "TargetBuilder", getTargetBuilder()));
        element.appendChild(APIUtils.getElement(document, "AllowClientCache", "" + getAllowClientCache()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        Element element = APIUtils.getIdentityElement(document, this);
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        return element;
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setDescription(element.elementText("Description"));
        setFilterNames(element.elementText("FilterNames"));
        setTargetBuilder(element.elementText("TargetBuilder"));
        setAllowClientCache(Boolean.parseBoolean(element.elementText("AllowClientCache")));
    }

    @PrePersist
    public void onCreate() {
        setCreated(Calendar.getInstance().getTime());
        setModified(getCreated());
    }

    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = uid;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public Boolean getAuthenticationRequired() {
        return authenticationRequired;
    }

    public Boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(Boolean authenticationRequired) {
        if (authenticationRequired != null) {
            this.authenticationRequired = authenticationRequired;
        }
    }

    public String getFilterNames() {
        return filterNames;
    }

    public void setFilterNames(String filterNames) {
        if (filterNames == null) {
            filterNames = "";
        }
        this.filterNames = filterNames;
    }

    public String getTargetBuilder() {
        return targetBuilder;
    }

    public void setTargetBuilder(String targetBuilder) {
        if (targetBuilder == null) {
            targetBuilder = "";
        }
        this.targetBuilder = targetBuilder;
    }

    public Boolean getAllowClientCache() {
        return allowClientCache;
    }

    public Boolean isAllowClientCache() {
        return allowClientCache;
    }

    public void setAllowClientCache(Boolean allowClientCache) {
        if (allowClientCache != null) {
            this.allowClientCache = allowClientCache;
        }
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        if (actions == null) {
            actions = new HashSet<Action>();
        }
        this.actions = actions;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        if (targets == null) {
            targets = new HashSet<Target>();
        }
        this.targets = targets;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}