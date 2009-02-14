package gc.carbon.domain.profile;

import gc.carbon.domain.*;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.domain.data.Item;
import gc.carbon.domain.data.ItemValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

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

@Entity
@DiscriminatorValue("PI")
public class ProfileItem extends Item {

    public final static int PRECISION = 21;
    public final static int SCALE = 6;
    public final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public final static BigDecimal ZERO = BigDecimal.valueOf(0, SCALE);
    public static final MathContext CONTEXT = new MathContext(ProfileItem.PRECISION, ProfileItem.ROUNDING_MODE);

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "PROFILE_ID")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_ITEM_ID")
    private DataItem dataItem;

    @Column(name = "AMOUNT", precision = PRECISION, scale = SCALE)
    private BigDecimal amount = ZERO;

    @Transient
    private Builder builder;

    @Transient
    private String convertedAmount = "";

    public static final AMEEUnit INTERNAL_AMOUNT_UNIT = new AMEEUnit(SI.KILOGRAM);
    public static final AMEEPerUnit INTERNAL_AMOUNT_PERUNIT = new AMEEPerUnit(NonSI.YEAR);
    public static final AMEEUnit INTERNAL_RETURN_UNIT = AMEECompoundUnit.valueOf(INTERNAL_AMOUNT_UNIT, INTERNAL_AMOUNT_PERUNIT);

    public ProfileItem() {
        super();
    }

    public ProfileItem(Profile profile, DataItem dataItem) {
        super(dataItem.getDataCategory(), dataItem.getItemDefinition());
        setProfile(profile);
        setDataItem(dataItem);
    }

    public ProfileItem(Profile profile, DataCategory dataCategory, DataItem dataItem) {
        super(dataCategory, dataItem.getItemDefinition());
        setProfile(profile);
        setDataItem(dataItem);
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public String toString() {
        return "ProfileItem_" + getUid();
    }

    @Transient
    public ProfileItem getCopy() {
        ProfileItem profileItem = new ProfileItem(getProfile(), getDataCategory(), getDataItem());
        profileItem.setStartDate(getStartDate());
        profileItem.setEndDate(getEndDate());
        profileItem.setAmount(getAmount());
        profileItem.setName(getName());
        profileItem.setCreated(getCreated());
        profileItem.setModified(getModified());
        profileItem.setUid(getUid());
        profileItem.setId(getId());
        return profileItem;
    }

    @Transient
    public String getPath() {
        return getUid();
    }

    @Transient
    public String getDisplayPath() {
        return EngineUtils.getDisplayPath(this);
    }

    @Transient
    public String getDisplayName() {
        return EngineUtils.getDisplayName(this);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        if (dataItem != null) {
            this.dataItem = dataItem;
        }
    }

    public boolean isEnd() {
        return (endDate != null) && (startDate.compareTo(endDate) == 0);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @SuppressWarnings("unchecked")
    public BigDecimal getAmount(AMEEUnit returnUnit) {
        if (!returnUnit.equals(INTERNAL_RETURN_UNIT)) {
            return INTERNAL_RETURN_UNIT.convert(getAmount(), returnUnit);
        } else {
            return getAmount();
        }
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null) {
            amount = ZERO;
        }
        this.amount = amount;
    }

    @Transient
    public void setConvertedAmount(String convertedAmount) {
        if (convertedAmount == null) {
            convertedAmount = "";
        }
        this.convertedAmount = convertedAmount;
    }


    @Transient
    public String getConvertedAmount() {
        return convertedAmount;
    }

    @Transient
    public void updateAmount(BigDecimal newAmount) {
        setAmount(newAmount);
    }

    public JSONObject getJSONObject(boolean b) throws JSONException {
        return builder.getJSONObject(b);
    }

    public Element getElement(Document document, boolean b) {
        return builder.getElement(document, b);
    }

    @Transient
    public String getType() {
        return ObjectType.PI.toString();
    }

    @Transient
    public ObjectType getObjectType() {
        return ObjectType.PI;
    }

    public boolean hasNonZeroPerTimeValues() {
        for (ItemValue iv : getItemValues()) {
            if (iv.hasPerTimeUnit() && iv.isNonZero()) {
                return true;
            }
        }
        return false;
    }

    //TEMP HACK - will remove as soon we decide how to handle return units in V1 correctly.
    public boolean isSingleFlight() {
        for (ItemValue iv : getItemValues()) {
            if ((iv.getName().startsWith("IATA") && iv.getValue().length() > 0) ||
                (iv.getName().startsWith("Lat") && !iv.getValue().equals("-999")) ||
                (iv.getName().startsWith("Lon") && !iv.getValue().equals("-999"))) {
                return true;
            }

        }
        return false;
    }
}
