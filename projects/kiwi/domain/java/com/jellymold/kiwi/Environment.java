package com.jellymold.kiwi;

import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.DatedObject;
import com.jellymold.utils.domain.UidGen;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "ENVIRONMENT")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Environment implements DatedObject, Comparable, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = 12)
    private String uid = "";

    @Column(name = "NAME", length = 255, nullable = false)
    private String name = "";

    @Column(name = "PATH", length = 255, nullable = false)
    private String path = "";

    @Column(name = "DESCRIPTION", length = 1000, nullable = false)
    private String description = "";

    @Column(name = "OWNER", length = 255, nullable = false)
    private String owner = "";

    @Column(name = "ITEMS_PER_PAGE", nullable = false)
    private Integer itemsPerPage = 10;

    @Column(name = "ITEMS_PER_FEED", nullable = false)
    private Integer itemsPerFeed = 10;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = Calendar.getInstance().getTime();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = Calendar.getInstance().getTime();

    @Version
    @Column(name = "VERSION")
    private Long version;

    public Environment() {
        super();
        setUid(UidGen.getUid());
    }

    public Environment(String name) {
        this();
        setName(name);
    }

    public String toString() {
        return "Environment_" + getUid();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Environment)) return false;
        Environment environment = (Environment) o;
        return getUid().equals(environment.getUid());
    }

    public int compareTo(Object o) {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Environment environment = (Environment) o;
        return getUid().compareTo(environment.getUid());
    }

    public int hashCode() {
        return getUid().hashCode();
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
        obj.put("path", getPath());
        obj.put("description", getDescription());
        obj.put("owner", getOwner());
        obj.put("itemsPerPage", getItemsPerPage());
        obj.put("itemsPerFeed", getItemsPerFeed());
        if (detailed) {
            obj.put("created", getCreated().toString());
            obj.put("modified", getModified().toString());
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
        Element element = document.createElement("Environment");
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Path", getPath()));
        element.appendChild(APIUtils.getElement(document, "Description", getDescription()));
        element.appendChild(APIUtils.getElement(document, "Owner", getOwner()));
        element.appendChild(APIUtils.getElement(document, "ItemsPerPage", getItemsPerPage().toString()));
        element.appendChild(APIUtils.getElement(document, "ItemsPerFeed", getItemsPerFeed().toString()));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setName(element.elementText("Name"));
        setPath(element.elementText("Path"));
        setDescription(element.elementText("Description"));
        setOwner(element.elementText("Owner"));
        setItemsPerPage(element.elementText("ItemsPerPage"));
        setItemsPerFeed(element.elementText("ItemsPerFeed"));
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            path = "";
        }
        this.path = path;
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

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        if (itemsPerPage != null) {
            this.itemsPerPage = itemsPerPage;
        }
    }

    public void setItemsPerPage(String itemsPerPage) {
        try {
            setItemsPerPage(Integer.parseInt(itemsPerPage));
        } catch (NumberFormatException e) {
            // swallow
        }
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        if (owner == null) {
            owner = "";
        }
        this.owner = owner;
    }

    public Integer getItemsPerFeed() {
        return itemsPerFeed;
    }

    public void setItemsPerFeed(Integer itemsPerFeed) {
        this.itemsPerFeed = itemsPerFeed;
    }

    public void setItemsPerFeed(String itemsPerFeed) {
        try {
            setItemsPerFeed(Integer.parseInt(itemsPerFeed));
        } catch (NumberFormatException e) {
            // swallow
        }
    }
}