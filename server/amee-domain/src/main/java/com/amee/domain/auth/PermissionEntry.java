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
package com.amee.domain.auth;

import com.amee.domain.AMEEStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * PermissionEntry represents an individual permission used within a Permission instance. Each
 * PermissionEntry instance is immutable. There are no setters and the default constructor
 * is private.
 * <p/>
 * PermissionEntry instances are considered equal if all the properties are identical. This allows
 * PermissionEntries to usefully be placed in a Set where.
 * <p/>
 * PermissionEntries are intended to precisely specify something that a principle can do with
 * an entity, such as modify it or not delete it.
 */
public class PermissionEntry implements Serializable {

    /**
     * Constants for the various commonly used permission entry values.
     */
    public final static PermissionEntry OWN = new PermissionEntry("own");
    public final static PermissionEntry VIEW = new PermissionEntry("view");
    public final static PermissionEntry VIEW_DENY = new PermissionEntry("view", false);
    public final static PermissionEntry CREATE = new PermissionEntry("create");
    public final static PermissionEntry MODIFY = new PermissionEntry("modify");
    public final static PermissionEntry DELETE = new PermissionEntry("delete");

    /**
     * The 'value' of a PermissionEntry. Examples are 'view' or 'delete.
     */
    private String value = "";

    /**
     * The status of the entities which this PermissionEntity applies to.
     */
    private AMEEStatus status = AMEEStatus.ACTIVE;

    /**
     * Flag to declare if a PermissionEntry should allow or deny the permission
     * associated with the value property. For example, allow or deny a principle to
     * 'view' an entity.
     */
    private Boolean allow = true;

    /**
     * Private default constructor, enforcing immutability for PermissionEntry instances.
     */
    private PermissionEntry() {
        super();
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value.
     *
     * @param value for new PermissionEntry
     */
    public PermissionEntry(String value) {
        this();
        setValue(value);
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value and allow state.
     *
     * @param value for new PermissionEntry
     * @param allow state to set, true or false
     */
    public PermissionEntry(String value, Boolean allow) {
        this(value);
        setAllow(allow);
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value and allow state.
     *
     * @param value for new PermissionEntry
     * @param allow state to set, true or false
     */
    public PermissionEntry(String value, String allow) {
        this(value, Boolean.valueOf(allow));
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value, allow state and status.
     *
     * @param value  for new PermissionEntry
     * @param allow  state to set, true or false
     * @param status for new PermissionEntity
     */
    public PermissionEntry(String value, Boolean allow, AMEEStatus status) {
        this(value);
        setAllow(allow);
        setStatus(status);
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value, allow state and status.
     *
     * @param value  for new PermissionEntry
     * @param allow  state to set, true or false
     * @param status for new PermissionEntity
     */
    public PermissionEntry(String value, String allow, String status) {
        this(value, allow);
        setStatus(AMEEStatus.valueOf(status));
    }

    public String toString() {
        return "PermissionEntry_" + getValue() + "_" + (isAllow() ? "allow" : "deny") + "_" + getStatus().toString();
    }

    /**
     * Compare a PermissionEntry with the supplied object. Asides from
     * standard object equality, PermissionEntries are considered equal if they
     * have the same property values. If the value is 'own' then only compare the
     * value property.
     *
     * @param o to compare with
     * @return true if supplied object is equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!PermissionEntry.class.isAssignableFrom(o.getClass())) return false;
        PermissionEntry entry = (PermissionEntry) o;
        if (getValue().equals(OWN.getValue()) && entry.getValue().equals(OWN.getValue())) {
            return true;
        } else {
            return getValue().equals(entry.getValue()) &&
                    getAllow().equals(entry.getAllow()) &&
                    getStatus().equals(entry.getStatus());
        }
    }

    /**
     * Returns a hash code based on the value and allow properties.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getValue().hashCode();
        hash = 31 * hash + getAllow().hashCode();
        return hash;
    }

    /**
     * Get the value of a PermissionEntry.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    private void setValue(String value) {
        if (StringUtils.isBlank(value)) throw new IllegalArgumentException("Value is empty.");
        this.value = value.trim().toLowerCase();
    }

    /**
     * Returns true if the allow state of a PermissionEntry is true.
     *
     * @return true if the allow state of a PermissionEntry is true
     */
    public Boolean isAllow() {
        return allow;
    }

    /**
     * Returns true if the allow state of a PermissionEntry is true.
     *
     * @return true if the allow state of a PermissionEntry is true
     */
    public Boolean getAllow() {
        return allow;
    }

    private void setAllow(Boolean allow) {
        this.allow = allow;
    }

    /**
     * Returns the AMEEStatus of a PermissionEntity.
     *
     * @return
     */
    public AMEEStatus getStatus() {
        return status;
    }

    private void setStatus(AMEEStatus status) {
        if (status == null) throw new IllegalArgumentException("Status is null.");
        this.status = status;
    }
}