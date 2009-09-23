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
package com.amee.service.auth;

import com.amee.domain.AMEEEntity;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ObjectType;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PermissionService {

    /**
     * Defines which 'principals' (keys) can relate to which 'entities' (values).
     */
    public final static Map<ObjectType, Set<ObjectType>> PRINCIPAL_ENTITY = new HashMap<ObjectType, Set<ObjectType>>();

    /**
     * Define which principals can relate to which entities.
     */
    {
        // Users <--> Entities
        addPrincipalAndEntity(ObjectType.USR, ObjectType.ENV);
        addPrincipalAndEntity(ObjectType.USR, ObjectType.PR);
        addPrincipalAndEntity(ObjectType.USR, ObjectType.DC);
        addPrincipalAndEntity(ObjectType.USR, ObjectType.PI);
        addPrincipalAndEntity(ObjectType.USR, ObjectType.DI);
        addPrincipalAndEntity(ObjectType.USR, ObjectType.IV);

        // Groups <--> Entities
        addPrincipalAndEntity(ObjectType.GRP, ObjectType.ENV);
        addPrincipalAndEntity(ObjectType.GRP, ObjectType.PR);
        addPrincipalAndEntity(ObjectType.GRP, ObjectType.DC);
        addPrincipalAndEntity(ObjectType.GRP, ObjectType.PI);
        addPrincipalAndEntity(ObjectType.GRP, ObjectType.DI);
        addPrincipalAndEntity(ObjectType.GRP, ObjectType.IV);
    }

    @Autowired
    private PermissionServiceDAO dao;

    public List<Permission> getPermissionsForEntity(IAMEEEntityReference entity) {
        if ((entity == null) || !isValidEntity(entity)) {
            throw new IllegalArgumentException();
        }
        return dao.getPermissionsForEntity(entity);
    }

    public Permission getPermissionForEntity(IAMEEEntityReference entity, PermissionEntry entry) {
        List<Permission> permissions = getPermissionsForEntity(entity);
        for (Permission permission : permissions) {
            if (permission.getEntries().contains(entry)) {
                return permission;
            }
        }
        return null;
    }

    public List<Permission> getPermissionsForPrincipals(Collection<IAMEEEntityReference> principals) {
        if ((principals == null) || principals.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<Permission> permissions = new ArrayList<Permission>();
        for (IAMEEEntityReference principal : principals) {
            permissions.addAll(getPermissionsForPrincipal(principal));
        }
        return permissions;
    }

    public List<Permission> getPermissionsForPrincipal(IAMEEEntityReference principal) {
        if ((principal == null) || !isValidPrincipal(principal)) {
            throw new IllegalArgumentException();
        }
        return dao.getPermissionsForPrincipal(principal, null);
    }

    /**
     * Fetch a List of all available Permissions matching the supplied principal and entity.
     *
     * @param principal       to match on
     * @param entityReference to match on
     * @return list of matching permissions
     */
    public List<Permission> getPermissionsForPrincipalAndEntity(AMEEEntity principal, IAMEEEntityReference entityReference) {
        if ((principal == null) || (entityReference == null) || !isValidPrincipalToEntity(principal, entityReference)) {
            throw new IllegalArgumentException();
        }
        AMEEEntity entity = getEntity(entityReference);
        List<Permission> permissions = new ArrayList<Permission>();
        permissions.addAll(entity.getPermissionsForPrincipalAndEntity(principal, entity));
        permissions.addAll(dao.getPermissionsForPrincipalAndEntity(principal, entity));
        return permissions;
    }

    public void trashPermissionsForEntity(IAMEEEntityReference entity) {
        dao.trashPermissionsForEntity(entity);
    }

    private void addPrincipalAndEntity(ObjectType principal, ObjectType entity) {
        Set<ObjectType> entities = PRINCIPAL_ENTITY.get(principal);
        if (entities == null) {
            entities = new HashSet<ObjectType>();
            PRINCIPAL_ENTITY.put(principal, entities);
        }
        entities.add(entity);
    }

    public boolean isValidPrincipal(IAMEEEntityReference principal) {
        if (principal == null) throw new IllegalArgumentException();
        return PRINCIPAL_ENTITY.keySet().contains(principal.getObjectType());
    }

    public boolean isValidEntity(IAMEEEntityReference entity) {
        if (entity == null) throw new IllegalArgumentException();
        for (Set<ObjectType> entities : PRINCIPAL_ENTITY.values()) {
            if (entities.contains(entity.getObjectType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidPrincipalToEntity(IAMEEEntityReference principal, IAMEEEntityReference entity) {
        if ((principal == null) || (entity == null)) throw new IllegalArgumentException();
        return isValidPrincipal(principal) &&
                PRINCIPAL_ENTITY.get(principal.getObjectType()).contains(entity.getObjectType());
    }

    /**
     * Fetch the entity referenced by the IAMEEEntityReference from the database. Copies the
     * AccessSpecification from the entityReference to the entity.
     *
     * @param entityReference to fetch
     * @return fetched entity
     */
    public AMEEEntity getEntity(IAMEEEntityReference entityReference) {
        AMEEEntity entity = entityReference.getEntity();
        if (entity == null) {
            entity = dao.getEntity(entityReference);
            if (entity == null) {
                throw new RuntimeException("An entity was not found for the entityReference.");
            }
            if (entity.getAccessSpecification() == null) {
                entity.setAccessSpecification(entityReference.getAccessSpecification());
            }
            entityReference.setEntity(entity);
        }
        return entity;
    }
}
