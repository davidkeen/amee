package gc.carbon.domain.path;

import com.jellymold.utils.ValueType;
import gc.carbon.domain.data.ItemValue;
import gc.carbon.domain.data.ItemValueDefinition;
import gc.carbon.domain.profile.ProfileItem;
import gc.carbon.domain.Unit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.measure.DecimalMeasure;
import java.math.BigDecimal;

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

@SuppressWarnings("unchecked")
public class InternalValue {

    private final Log log = LogFactory.getLog(getClass());

    private Object value;

    public InternalValue(String value) {
        this.value = value;
    }

    public InternalValue(ItemValueDefinition itemValueDefinition) {
        if (isDecimal(itemValueDefinition)) {
            value = convertStringToDecimal(itemValueDefinition.getUsableValue());
        } else {
            value = itemValueDefinition.getUsableValue();
        }
    }

    public InternalValue(ItemValue itemValue) {
        if (isDecimal(itemValue.getItemValueDefinition())) {
            value = asInternalValue(itemValue);
        } else {
            value = itemValue.getUsableValue();
        }
    }

    public Object getValue() {
        return value;
    }

    private boolean isDecimal(ItemValueDefinition ivd) {
        return ivd.getValueDefinition().getValueType().equals(ValueType.DECIMAL);
    }

    private BigDecimal convertStringToDecimal(String decimalString) {
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(decimalString);
            decimal = decimal.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
            if (decimal.precision() > ProfileItem.PRECISION) {
                log.warn("precision is too big: " + decimal);
            }
        } catch (Exception e) {
            log.warn("caught Exception: " + e);
            decimal = ProfileItem.ZERO;
        }
        return decimal;
    }

    private BigDecimal asInternalValue(ItemValue iv) {

        BigDecimal decimal = convertStringToDecimal(iv.getUsableValue());

        if (!iv.hasUnits() && !iv.hasPerUnits())
            return decimal;

        Unit internalUnit = iv.getItemValueDefinition().getCompoundUnit();
        Unit externalUnit = iv.getCompoundUnit();
        if (!externalUnit.equals(internalUnit)) {
            decimal = externalUnit.convert(decimal, internalUnit);
        }

        return decimal;
    }
}