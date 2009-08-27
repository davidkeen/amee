/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.domain;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import com.amee.domain.auth.User;
import com.amee.domain.profile.Profile;

@Embeddable
public class AMEEEntityReference implements Serializable {

    public final static int ENTITY_CLASS_MAX_SIZE = 50;

    public final static Map<String, Class> CLASSES = new HashMap<String, Class>();
    {
        CLASSES.put("User", User.class);
        CLASSES.put("Profile", Profile.class);
    }

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @Column(name = "ENTITY_UID", length = AMEEEntity.UID_SIZE, nullable = false)
    private String entityUid = "";

    @Column(name = "ENTITY_CLASS", length = ENTITY_CLASS_MAX_SIZE, nullable = false)
    private String entityClass = "";

    public AMEEEntityReference() {
        super();
    }

    public AMEEEntityReference(AMEEEntity entity) {
        this();
        setEntityId(entity.getId());
        setEntityUid(entity.getUid());
        setEntityClass(entity.getClass().getSimpleName());
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getEntityUid());
        obj.put("class", getEntityClass());
        return obj;
    }

    public Element getElement(Document document, String name) {
        Element element = document.createElement(name);
        element.setAttribute("uid", getEntityUid());
        element.setAttribute("class", getEntityClass());
        return element;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityUid() {
        return entityUid;
    }

    public void setEntityUid(String entityUid) {
        if (entityUid == null) {
            entityUid = "";
        }
        this.entityUid = entityUid;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        if (entityClass == null) {
            entityClass = "";
        }
        this.entityClass = entityClass;
    }
}
