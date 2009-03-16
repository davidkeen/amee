package com.amee.domain.auth;

import com.amee.domain.APIUtils;
import com.amee.domain.DatedObject;
import com.amee.domain.UidGen;
import com.amee.domain.environment.Environment;
import com.amee.domain.environment.EnvironmentObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A Role encapsulates a set of Actions. Roles are granted to Users within a Group via GroupUsers.
 * The intention is to allow individual Users to be assigned varying Roles in different Groups
 * and for those Users to have access to the various Actions assigned to the Roles.
 * <p/>
 * Roles belong to a Environment.
 * <p/>
 * When deleting a Role we need to ensure the Role is removed from relevant GroupUsers.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "ROLE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Role implements EnvironmentObject, DatedObject, Comparable, Serializable {

    public final static int NAME_SIZE = 100;
    public final static int DESCRIPTION_SIZE = 1000;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    @Index(name = "NAME_IND")
    private String name = "";

    @Column(name = "DESCRIPTION", length = DESCRIPTION_SIZE, nullable = false)
    private String description = "";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ROLE_ACTION",
            joinColumns = {@JoinColumn(name = "ROLE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "ACTION_ID")}
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderBy("name")
    private Set<Action> actions = new HashSet<Action>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    public Role() {
        super();
        setUid(UidGen.getUid());
    }

    public Role(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public void add(Action action) {
        getActions().add(action);
    }

    public void remove(Action action) {
        getActions().remove(action);
    }

    public String toString() {
        return "Role_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return getName().equalsIgnoreCase(role.getName());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Role role = (Role) o;
        return getName().compareToIgnoreCase(role.getName());
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
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            JSONArray actionsArr = new JSONArray();
            for (Action action : getActions()) {
                actionsArr.put(action.getJSONObject());
            }
            obj.put("actions", actionsArr);
        }
        return obj;
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = APIUtils.getIdentityJSONObject(this);
        obj.put("name", getName());
        return obj; 
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("Role");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            Element actionsElement = document.createElement("Actions");
            for (Action action : getActions()) {
                actionsElement.appendChild(action.getIdentityElement(document));
            }
            element.appendChild(actionsElement);
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
    }

    @Transient
    public boolean hasAction(String action) {
        if (getActions() == null) return false;
        for (Action a : getActions()) {
            if (a.getKey().compareToIgnoreCase(action) == 0) {
                return true;
            }
        }
        return false;
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        if (environment != null) {
            this.environment = environment;
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

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        if (actions == null) {
            actions = new HashSet<Action>();
        }
        this.actions = actions;
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