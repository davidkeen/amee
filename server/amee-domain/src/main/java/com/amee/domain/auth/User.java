package com.amee.domain.auth;

import com.amee.domain.APIUtils;
import com.amee.domain.APIVersion;
import com.amee.domain.DatedObject;
import com.amee.domain.UidGen;
import com.amee.domain.auth.crypto.Crypto;
import com.amee.domain.auth.crypto.CryptoException;
import com.amee.domain.environment.Environment;
import com.amee.domain.environment.EnvironmentObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A User represents a single person or entity who has authenticated access to an Environment.
 * <p/>
 * Users can be members of Groups via GroupUser.
 * <p/>
 * A User belongs to an Environment.
 * <p/>
 * When deleting a User we need to ensure all relevant GroupUsers are also removed.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "USER")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User implements EnvironmentObject, DatedObject, Comparable, Serializable {

    @Transient
    private final Log log = LogFactory.getLog(getClass());

    public final static int USERNAME_SIZE = 20;
    public final static int PASSWORD_SIZE = 40;
    public final static int PASSWORD_CLEAR_SIZE = 40;
    public final static int NAME_SIZE = 100;
    public final static int EMAIL_SIZE = 255;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Column(name = "STATUS")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "USER_TYPE")
    private UserType type = UserType.STANDARD;

    @Column(name = "USERNAME", length = USERNAME_SIZE, nullable = false)
    @Index(name = "USERNAME_IND")
    private String username = "";

    @Column(name = "PASSWORD", length = PASSWORD_SIZE, nullable = false)
    private String password = "";

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    private String name = "";

    @Column(name = "EMAIL", length = EMAIL_SIZE, nullable = false)
    @Index(name = "EMAIL_IND")
    private String email = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "API_VERSION_ID")
    private APIVersion apiVersion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    @Transient
    private List<String> groupNames = new ArrayList<String>();

    public User() {
        super();
        setUid(UidGen.getUid());
    }

    public User(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public User(Environment environment, String username, String password, String name) {
        this(environment);
        setUsername(username);
        setPasswordInClear(password);
        setName(name);
    }

    public String toString() {
        return "User: " + getUsername();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getUsername().equalsIgnoreCase(user.getUsername());
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        User user = (User) o;
        return getUsername().compareToIgnoreCase(user.getUsername());
    }

    public int hashCode() {
        return getUsername().toLowerCase().hashCode();
    }

    @Transient
    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    @Transient
    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("status", getStatus().getName());
        obj.put("type", getType().getName());
        obj.put("groupNames", new JSONArray(getGroupNames()));
        obj.put("apiVersion", getAPIVersion());
        if (detailed) {
            obj.put("username", getUsername());
            obj.put("name", getName());
            obj.put("email", getEmail());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
        }
        return obj;
    }

    @Transient
    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = APIUtils.getIdentityJSONObject(this);
        obj.put("username", getUsername());
        return obj;
    }

    @Transient
    public Element getElement(Document document) {
        return getElement(document, true);
    }

    @Transient
    public Element getElement(Document document, boolean detailed) {
        return getElement(document, "User", detailed);
    }

    @Transient
    public Element getElement(Document document, String name, boolean detailed) {
        Element groups;
        Element element = document.createElement(name);
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Status", getStatus().getName()));
        element.appendChild(APIUtils.getElement(document, "Type", getType().getName()));
        groups = document.createElement("GroupNames");
        for (String groupName : getGroupNames()) {
            groups.appendChild(APIUtils.getElement(document, "GroupName", groupName));
        }
        element.appendChild(groups);
        element.appendChild(APIUtils.getElement(document, "ApiVersion", getAPIVersion().toString()));
        if (detailed) {
            element.appendChild(APIUtils.getElement(document, "Name", getName()));
            element.appendChild(APIUtils.getElement(document, "Username", getUsername()));
            element.appendChild(APIUtils.getElement(document, "Email", getEmail()));
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document) {
        Element element = APIUtils.getIdentityElement(document, this);
        element.appendChild(APIUtils.getElement(document, "Username", getUsername()));
        return element;
    }

    @Transient
    public Element getIdentityElement(Document document, String name) {
        Element element = APIUtils.getIdentityElement(document, name, this);
        element.appendChild(APIUtils.getElement(document, "Username", getUsername()));
        return element;
    }

    @Transient
    public void populate(org.dom4j.Element element) {
        setUid(element.attributeValue("uid"));
        setUsername(element.elementText("Username"));
        setPasswordInClear(element.elementText("Password"));
        setName(element.elementText("Name"));
        setEmail(element.elementText("Email"));
        setStatus(element.elementText("Status"));
        setType(element.elementText("Type"));
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

    public UserStatus getStatus() {
        return status;
    }

    @Transient
    public int getStatusCode() {
        return status.ordinal();
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setStatus(String name) {
        if (name != null) {
            try {
                setStatus(UserStatus.valueOf(name));
            } catch (IllegalArgumentException e) {
                // swallow
            }
        }
    }

    public UserType getType() {
        return type;
    }

    @Transient
    public boolean isGuestUser() {
        return type.equals(UserType.GUEST);
    }

    @Transient
    public boolean isAnonymousUser() {
        return type.equals(UserType.ANONYMOUS);
    }

    @Transient
    public boolean isStandardUser() {
        return type.equals(UserType.STANDARD);
    }

    @Transient
    public boolean isSuperUser() {
        return type.equals(UserType.SUPER);
    }

    @Transient
    public int getTypeCode() {
        return type.ordinal();
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public void setType(String name) {
        if (name != null) {
            try {
                setType(UserType.valueOf(name));
            } catch (IllegalArgumentException e) {
                // swallow
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null) {
            username = "";
        }
        this.username = username.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null) {
            email = "";
        }
        this.email = email.trim();
    }

    public String getPassword() {
        return password;
    }

    @Transient
    public void setPasswordInClear(String password) {
        try {
            setPassword(Crypto.getAsMD5AndBase64(password));
        } catch (CryptoException e) {
            log.error("Caught CryptoException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setPassword(String password) {
        checkPassword(password, PASSWORD_SIZE);
        this.password = password.trim();
    }

    private static void checkPassword(String password, int size) {
        if ((password == null) || password.isEmpty() || (password.length() > size)) {
            throw new IllegalArgumentException(
                    "Password must not be empty and must be <= " + size + " characters long.");
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

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        if (groupNames != null) {
            this.groupNames = groupNames;
        }
    }

    public APIVersion getAPIVersion() {
        return apiVersion;
    }

    public void setAPIVersion(APIVersion apiVersion) {
        if (apiVersion != null) {
            this.apiVersion = apiVersion;
        }
    }
}