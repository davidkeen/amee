package com.amee.domain;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.measure.unit.Unit;

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
public class AMEECompoundUnit extends AMEEUnit {

    private AMEEPerUnit perUnit;

    protected AMEECompoundUnit(AMEEUnit unit, AMEEPerUnit perUnit) {
        super(unit.toUnit());
        this.perUnit = perUnit;
    }

    public static AMEECompoundUnit valueOf(AMEEUnit unit, AMEEPerUnit perUnit) {
        return new AMEECompoundUnit(unit, perUnit);
    }

    public Unit toUnit() {
        return unit.divide(perUnit.toUnit());
    }

    public boolean hasDifferentPerUnit(AMEEPerUnit perUnit) {
        return !this.perUnit.equals(perUnit);
    }

    public AMEEPerUnit getPerUnit() {
        return perUnit;
    }
    
    public void getElement(Element parent, Document document) {
        parent.appendChild(APIUtils.getElement(document, "Unit", unit.toString()));
        parent.appendChild(APIUtils.getElement(document, "PerUnit", perUnit.toString()));
    }

    public void getJSONObject(JSONObject parent) throws JSONException {
        parent.put("unit", unit.toString());
        parent.put("perUnit", perUnit.toString());
    }
}