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
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEStatus;
import com.amee.domain.APIVersion;
import com.amee.domain.auth.Group;
import com.amee.domain.auth.Permission;
import com.amee.domain.auth.PermissionEntry;
import com.amee.domain.auth.User;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.ItemValue;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Service
@Scope("prototype")
public class PermissionService {

    private final Log log = LogFactory.getLog(getClass());

    private static final String CACHE_REGION = "query.permissionService";

    /**
     * Defines which 'principle' classes (keys) can relate to which 'entity' classes (values).
     */
    public final static Map<Class, Set<Class>> PRINCIPLE_ENTITY = new HashMap<Class, Set<Class>>();

    /**
     * Define which principles can relate to which entities.
     */
    {
        // Users <--> Entities
        addPrincipleAndEntity(User.class, Environment.class);
        addPrincipleAndEntity(User.class, Profile.class);
        addPrincipleAndEntity(User.class, DataCategory.class);
        addPrincipleAndEntity(User.class, ProfileItem.class);
        addPrincipleAndEntity(User.class, DataItem.class);
        addPrincipleAndEntity(User.class, ItemValue.class);

        // Groups <--> Entities
        addPrincipleAndEntity(Group.class, Environment.class);
        addPrincipleAndEntity(Group.class, Profile.class);
        addPrincipleAndEntity(Group.class, DataCategory.class);
        addPrincipleAndEntity(Group.class, ProfileItem.class);
        addPrincipleAndEntity(Group.class, DataItem.class);
        addPrincipleAndEntity(Group.class, ItemValue.class);
    }

    /**
     * Constants for the various permission entry values.
     */
    public final static PermissionEntry OWN = new PermissionEntry("own");
    public final static PermissionEntry VIEW = new PermissionEntry("view");
    public final static PermissionEntry VIEW_DEPRECATED = new PermissionEntry("view-deprecated");
    public final static PermissionEntry CREATE = new PermissionEntry("create");
    public final static PermissionEntry MODIFY = new PermissionEntry("modify");
    public final static PermissionEntry DELETE = new PermissionEntry("delete");

    /**
     * Helpful PermissionEntry Sets.
     */
    public final static Set<PermissionEntry> OWN_VIEW = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_CREATE = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_MODIFY = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_DELETE = new HashSet<PermissionEntry>();

    /**
     * Populate PermissionEntry Sets.
     */
    {
        OWN_VIEW.add(OWN);
        OWN_VIEW.add(VIEW);
        OWN_CREATE.add(OWN);
        OWN_CREATE.add(CREATE);
        OWN_MODIFY.add(OWN);
        OWN_MODIFY.add(MODIFY);
        OWN_DELETE.add(OWN);
        OWN_DELETE.add(DELETE);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForEntity(AMEEEntity entity) {
        if ((entity == null) || !isValidEntity(entity)) {
            throw new IllegalArgumentException();
        }
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("entityReference.entityId", entity.getId()));
        criteria.add(Restrictions.eq("entityReference.entityClass", entity.getClass()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    public Permission getPermissionForEntity(AMEEEntity entity, PermissionEntry entry) {
        List<Permission> permissions = getPermissionsForEntity(entity);
        for (Permission permission : permissions) {
            if (permission.getEntries().contains(entry)) {
                return permission;
            }
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForPrinciples(Collection<AMEEEntity> principles) {
        if ((principles == null) || principles.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<Permission> permissions = new ArrayList<Permission>();
        for (AMEEEntity principle : principles) {
            permissions.addAll(getPermissionsForPrinciple(principle));
        }
        return permissions;
    }

    public List<Permission> getPermissionsForPrinciple(AMEEEntity principle) {
        return getPermissionsForPrinciple(principle, null);
    }

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForPrinciple(AMEEEntity principle, Class entityClass) {
        if ((principle == null) || !isValidPrinciple(principle)) {
            throw new IllegalArgumentException();
        }
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("principleReference.entityId", principle.getId()));
        criteria.add(Restrictions.eq("principleReference.entityClass", principle.getClass()));
        if (entityClass != null) {
            criteria.add(Restrictions.eq("entityReference.entityClass", entityClass.getSimpleName()));
        }
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Permission> getPermissionsForPrincipleAndEntity(AMEEEntity principle, AMEEEntity entity) {
        if ((principle == null) || (entity == null) || !isValidPrincipleToEntity(principle, entity)) {
            throw new IllegalArgumentException();
        }
        Session session = (Session) entityManager.getDelegate();
        Criteria criteria = session.createCriteria(Permission.class);
        criteria.add(Restrictions.eq("principleReference.entityId", principle.getId()));
        criteria.add(Restrictions.eq("principleReference.entityClass", principle.getClass()));
        criteria.add(Restrictions.eq("entityReference.entityId", entity.getId()));
        criteria.add(Restrictions.eq("entityReference.entityClass", entity.getClass()));
        criteria.add(Restrictions.ne("status", AMEEStatus.TRASH));
        criteria.setCacheable(true);
        criteria.setCacheRegion(CACHE_REGION);
        return criteria.list();
    }

    public boolean hasPermissions(AMEEEntity principle, AMEEEntity entity, String entries) {
        Set<PermissionEntry> entrySet = new HashSet<PermissionEntry>();
        for (String entry : entries.split(",")) {
            entrySet.add(new PermissionEntry(entry));
        }
        return hasPermissions(principle, entity, entrySet);
    }

    public boolean hasPermissions(AMEEEntity principle, AMEEEntity entity, Set<PermissionEntry> entrySet) {
        List<AMEEEntity> principles = new ArrayList<AMEEEntity>();
        principles.add(principle);
        List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
        entities.add(entity);
        return hasPermissions(principles, entities, entrySet);
    }

    public boolean hasPermissions(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entrySet) {
        return hasPermissions(principles, entities, entrySet, false);
    }

    // TODO: Handle deprecated entities.
    public boolean hasPermissions(List<AMEEEntity> principles, List<AMEEEntity> entities, Set<PermissionEntry> entrySet, boolean all) {
        // Permissions for earlier entities can be superceeded by those from later entities.
        for (AMEEEntity entity : entities) {
            // Permissions for earlier principles can be superceeded by those from later principles.
            for (AMEEEntity principle : principles) {
                // Exception for Users who are super-users.
                if (User.class.isAssignableFrom(principle.getClass())) {
                    if (((User) principle).isSuperUser()) {
                        log.debug("hasPermissions() - hell yeh");
                        return true;
                    }
                }
                // Check permissions for this principle and entity combination.
                Collection<Permission> permissions = getPermissionsForPrincipleAndEntity(principle, entity);
                for (Permission permission : permissions) {
                    // Do we need to match all of the entries?
                    if (all) {
                        // Does this Permission contain all of the entries supplied?
                        if (permission.getEntries().containsAll(entrySet)) {
                            log.debug("hasPermissions() - yeh");
                            return true;
                        }
                    } else {
                        // Does this Permission contain at least one of the entries supplied?
                        for (PermissionEntry entry : entrySet) {
                            if (permission.getEntries().contains(entry)) {
                                log.debug("hasPermissions() - yeh");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        log.debug("hasPermissions() - nah");
        return false;
    }

    public void trashPermissionsForEntity(AMEEEntity entity) {
        trashPermissionsForEntity(new AMEEEntityReference(entity));
    }

    public void trashPermissionsForEntity(AMEEEntityReference entity) {
        entityManager.createQuery(
                "UPDATE Permission " +
                        "SET status = :trash, " +
                        "modified = current_timestamp() " +
                        "WHERE entityReference.entityId = :entityId " +
                        "AND entityReference.entityClass = :entityClass " +
                        "AND status != :trash")
                .setParameter("trash", AMEEStatus.TRASH)
                .setParameter("entityId", entity.getEntityId())
                .setParameter("entityClass", entity.getEntityClass())
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    public AMEEEntity getEntity(AMEEEntityReference entityReference) {
        if (entityReference == null) {
            throw new IllegalArgumentException();
        }
        return (AMEEEntity) entityManager.find(
                AMEEEntityReference.CLASSES.get(entityReference.getEntityClass()), entityReference.getEntityId());
    }

    public APIVersion getAPIVersion(AMEEEntity entity) {
        Permission permission = getPermissionForEntity(entity, OWN);
        if (permission == null) {
            throw new RuntimeException("Profile does not have a Permission expresing ownership.");
        }
        AMEEEntity principle = getEntity(permission.getPrincipleReference());
        if ((principle == null) || !User.class.isAssignableFrom(principle.getClass())) {
            throw new RuntimeException("Permission principle not found or was not a User instance.");
        }
        User user = (User) principle;
        return user.getAPIVersion();
    }

    private void addPrincipleAndEntity(Class principleClass, Class entityClass) {
        Set<Class> entityClasses = PRINCIPLE_ENTITY.get(principleClass);
        if (entityClasses == null) {
            entityClasses = new HashSet<Class>();
            PRINCIPLE_ENTITY.put(principleClass, entityClasses);
        }
        entityClasses.add(entityClass);
    }

    public boolean isValidPrinciple(AMEEEntity principle) {
        if (principle == null) throw new IllegalArgumentException();
        return PRINCIPLE_ENTITY.keySet().contains(getRealClass(principle.getClass()));
    }

    public boolean isValidEntity(AMEEEntity entity) {
        if (entity == null) throw new IllegalArgumentException();
        for (Set<Class> entities : PRINCIPLE_ENTITY.values()) {
            if (entities.contains(getRealClass(entity.getClass()))) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidPrincipleToEntity(AMEEEntity principle, AMEEEntity entity) {
        if ((principle == null) || (entity == null)) throw new IllegalArgumentException();
        return isValidPrinciple(principle) &&
                PRINCIPLE_ENTITY.get(getRealClass(principle.getClass())).contains(getRealClass(entity.getClass()));
    }

    public Class getRealClass(Class c) {
        if (User.class.isAssignableFrom(c)) {
            return User.class;
        } else if (Group.class.isAssignableFrom(c)) {
            return Group.class;
        } else if (Environment.class.isAssignableFrom(c)) {
            return Environment.class;
        } else if (Profile.class.isAssignableFrom(c)) {
            return Profile.class;
        } else if (DataCategory.class.isAssignableFrom(c)) {
            return DataCategory.class;
        } else if (DataItem.class.isAssignableFrom(c)) {
            return DataItem.class;
        } else if (ProfileItem.class.isAssignableFrom(c)) {
            return ProfileItem.class;
        } else if (ItemValue.class.isAssignableFrom(c)) {
            return ItemValue.class;
        }
        throw new IllegalArgumentException("Class not supported.");
    }
}
