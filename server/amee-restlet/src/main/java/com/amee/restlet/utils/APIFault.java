package com.amee.restlet.utils;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
public enum APIFault {

    NONE,
    INVALID_PARAMETERS,
    INVALID_API_PARAMETERS,
    INVALID_CONTENT,
    MISSING_PARAMETERS,
    INVALID_DATE_FORMAT,
    INVALID_DATE_RANGE,
    INVALID_PRORATA_REQUEST,
    DUPLICATE_ITEM,
    INVALID_UNIT,
    EMPTY_LIST,
    ENTITY_NOT_FOUND,
    MAX_BATCH_SIZE_EXCEEDED,
    DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE,
    INVALID_RESOURCE_MODIFICATION;

    private String[] strings = {
            "",
            "A request was received with one or more invalid parameters.",
            "A request was received with one or more parameters not supported by the version of the API.",
            "A request was received with invalid content.",
            "A request was received with one or more missing parameters.",
            "A request was received with one or more datetime parameters having an invalid format.",
            "A request was received with an invalid date range.",
            "A prorata request was received without a recognised bounded date range.",
            "A POST or PUT request was received which would have resulted in a duplicate resource being created.",
            "A request was received with an invalid unit.",
            "An empty list was received or produced.",
            "An entity was not found for the given identifier.",
            "Max batch size was exceeded.",
            "The DELETE operation must leave at least one ITEM_VALUE per ITEM_VALUE_DEFINTION.",
            "A PUT was received attempting to make a prohibited modification."};

    public String getString() {
        return strings[this.ordinal()];
    }

    public String getCode() {
        return Integer.toString(this.ordinal());
    }

    public String toString() {
        return getString();
    }
}
