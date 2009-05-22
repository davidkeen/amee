package com.amee.domain.profile;

import com.amee.core.CO2Amount;
import com.amee.core.Decimal;
import com.amee.core.ObjectType;
import com.amee.domain.Builder;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.DataItem;
import com.amee.domain.data.Item;
import com.amee.domain.data.ItemValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.persistence.*;
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

@Configurable(autowire = Autowire.BY_TYPE)
@Entity
@DiscriminatorValue("PI")
public class ProfileItem extends Item {

    @Transient
    private final Log log = LogFactory.getLog(getClass());

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "PROFILE_ID")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "DATA_ITEM_ID")
    private DataItem dataItem;

    @Column(name = "AMOUNT", precision = Decimal.PRECISION, scale = Decimal.SCALE)
    private BigDecimal persistentAmount = BigDecimal.ZERO;

    @Transient
    private BigDecimal amount = null;

    @Transient
    private Builder builder;

    @Transient
    @Resource
    private CO2CalculationService calculationService;

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

    @Override
    public String getPath() {
        return getUid();
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

    /**
     * Get the {@link com.amee.core.CO2Amount CO2Amount} for this ProfileItem.
     * <p/>
     * If the ProfileItem does not support CO2 calculations (i.e. metadata) CO2Amount.ZERO is returned.
     *
     * @return - the {@link com.amee.core.CO2Amount CO2Amount} for this ProfileItem
     */
    public CO2Amount getAmount() {
        // CO2 amounts are lazily determined once per session.
        if (amount == null) {
            log.debug("getAmount() - lazily calculating amount");
            calculationService.calculate(this);
        }
        return new CO2Amount(amount);
    }

    /**
     * Set the amount. If the amount is different to the current persistentAmount then set this too. Will
     * return true if the persistentAmount was changed.
     *
     * @param amount to set
     * @return true if the persistentAmount was changed
     */
    public boolean setAmount(CO2Amount amount) {
        this.amount = amount.getValue();
        // Persist the transient session CO2 amount if it is different from the last persisted amount.
        if (this.amount.compareTo(persistentAmount) != 0) {
            log.debug("setAmount() - amount has changed from " + persistentAmount + " to " + this.amount);
            persistentAmount = this.amount;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public JSONObject getJSONObject(boolean b) throws JSONException {
        return builder.getJSONObject(b);
    }

    public Element getElement(Document document, boolean b) {
        return builder.getElement(document, b);
    }

    @Override
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

    //TODO - TEMP HACK - will remove as soon we decide how to handle return units in V1 correctly.
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
