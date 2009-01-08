package gc.carbon.domain;

import gc.carbon.domain.profile.ProfileItem;

import javax.measure.unit.SI;
import javax.measure.DecimalMeasure;
import java.math.BigDecimal;

import com.jellymold.utils.domain.APIObject;
import com.jellymold.utils.domain.APIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.json.JSONObject;
import org.json.JSONException;

/*
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
//TODO - Implement APIObject following rework of Unit domain model
public class Unit {

    public static final Unit ONE = new Unit(javax.measure.unit.Unit.ONE);
    protected javax.measure.unit.Unit unit = javax.measure.unit.Unit.ONE;

    public static Unit valueOf(String unit) {
        return new Unit(javax.measure.unit.Unit.valueOf(unit));
    }

    protected Unit(javax.measure.unit.Unit unit) {
        this.unit = unit;
    }

    public Unit with(PerUnit perUnit) {
        return CompoundUnit.valueOf(this, perUnit);
    }

    public BigDecimal convert(BigDecimal decimal, Unit targetUnit) {
        DecimalMeasure dm = DecimalMeasure.valueOf(decimal, toUnit());
        BigDecimal converted = dm.to(targetUnit.toUnit(), ProfileItem.CONTEXT).getValue();
        return converted.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
    }

    public boolean equals(Unit that) {
        return toUnit().equals(that.toUnit());
    }

    public javax.measure.unit.Unit toUnit() {
        return unit;
    }

    public String toString() {
        return toUnit().toString();
    }


    //TODO - Once amount is an IV and not a column in ITEM table this can be removed. It allow unit to added as a child of the amount element
    public void getElement(Element parent, Document document) {
        parent.appendChild(APIUtils.getElement(document, "Unit", toString()));
    }

    //TODO - Once amount is an IV and not a column in ITEM table this can be removed. It allow unit to added as a child of the amount JSONObject 
    public void getJSONObject(JSONObject parent) throws JSONException {
        parent.put("unit", toString());
    }    
}