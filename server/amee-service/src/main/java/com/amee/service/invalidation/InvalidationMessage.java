package com.amee.service.invalidation;

import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ObjectType;
import com.amee.service.messaging.Message;
import com.amee.service.messaging.MessagingException;
import org.json.JSONException;
import org.json.JSONObject;

public class InvalidationMessage extends Message {

    private String serverName;
    private String instanceName;
    private ObjectType objectType;
    private String entityUid;

    public InvalidationMessage(Object source) {
        super(source);
        this.setServerName(System.getProperty("server.name"));
        this.setInstanceName(System.getProperty("instance.name"));
    }

    public InvalidationMessage(Object source, IAMEEEntityReference entity) {
        this(source);
        this.setObjectType(entity.getObjectType());
        this.setEntityUid(entity.getEntityUid());
    }

    public InvalidationMessage(Object source, String message) {
        super(source, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || !InvalidationMessage.class.isAssignableFrom(o.getClass())) return false;
        InvalidationMessage message = (InvalidationMessage) o;
        return getEntityUid().equals(message.getEntityUid()) &&
                getObjectType().equals(message.getObjectType());
    }

    @Override
    public String getMessage() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("sn", getServerName());
            obj.put("in", getInstanceName());
            obj.put("ot", getObjectType().getName());
            obj.put("uid", getEntityUid());
        } catch (JSONException e) {
            throw new MessagingException("Caught JSONException: " + e.getMessage(), e);
        }
        return obj.toString();
    }

    @Override
    public void setMessage(String message) {
        if (message == null) {
            throw new MessagingException("InvalidationEvent message was null.");
        }
        try {
            JSONObject obj = new JSONObject(message);
            setServerName(obj.getString("sn"));
            setInstanceName(obj.getString("in"));
            setObjectType(ObjectType.valueOf(obj.getString("ot")));
            setEntityUid(obj.getString("uid"));
        } catch (IllegalArgumentException e) {
            throw new MessagingException("InvalidationEvent message has ObjectType cannot be parsed.");
        } catch (JSONException e) {
            throw new MessagingException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    public boolean isFromSameInstance() {
        return getServerName().equalsIgnoreCase(System.getProperty("server.name")) &&
                getInstanceName().equalsIgnoreCase(System.getProperty("instance.name"));
    }

    public boolean isFromOtherInstance() {
        return !isFromSameInstance();
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getEntityUid() {
        return entityUid;
    }

    public void setEntityUid(String entityUid) {
        this.entityUid = entityUid;
    }
}