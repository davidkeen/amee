/**
* This file is part of AMEE.
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
package gc.carbon.data;

import com.jellymold.sheet.Choices;
import com.jellymold.sheet.ValueType;
import gc.carbon.profile.ProfileFinder;
import gc.carbon.profile.ProfileItem;
import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: 'perMonth' is hard-coding - how should this be made more dynamic?

@Name("calculator")
@Scope(ScopeType.EVENT)
public class Calculator implements Serializable {

    private final static Logger log = Logger.getLogger(Calculator.class);

    @In(create = true)
    DataService dataService;

    @In(create = true)
    DataFinder dataFinder;

    @In(create = true)
    ProfileFinder profileFinder;

    public BigDecimal calculate(ProfileItem profileItem) {
        log.debug("starting calculator");
        Map<String, Object> values;
        BigDecimal amountPerMonth;
        if (!profileItem.isEnd()) {
            ItemDefinition itemDefinition = profileItem.getItemDefinition();
            Algorithm algorithm = getAlgorithm(itemDefinition, "perMonth");
            if (algorithm != null) {
                // setup values list
                values = getValues(profileItem);
                profileFinder.setProfileItem(profileItem);
                values.put("profileFinder", profileFinder);
                values.put("dataFinder", dataFinder);
                // get the new amount via algorithm and values
                amountPerMonth = calculate(algorithm, values);
                if (amountPerMonth != null) {
                    // store carbon to Item
                    profileItem.updateAmountPerMonth(amountPerMonth);
                } else {
                    log.warn("carbon not set");
                    amountPerMonth = ProfileItem.ZERO;
                }
            } else {
                log.warn("Algorithm NOT found");
                amountPerMonth = ProfileItem.ZERO;
            }
        } else {
            amountPerMonth = ProfileItem.ZERO;
            profileItem.updateAmountPerMonth(amountPerMonth);
        }
        return amountPerMonth;
    }

    public BigDecimal calculate(DataItem dataItem, Choices userValueChoices) {
        log.debug("starting calculator");
        Map<String, Object> values;
        BigDecimal amountPerMonth;
        ItemDefinition itemDefinition = dataItem.getItemDefinition();
        Algorithm algorithm = getAlgorithm(itemDefinition, "perMonth");
        if (algorithm != null) {
            // get the new amount via algorithm and values
            values = getValues(dataItem, userValueChoices);
            values.put("profileFinder", profileFinder);
            values.put("dataFinder", dataFinder);
            amountPerMonth = calculate(algorithm, values);
        } else {
            log.warn("Algorithm NOT found");
            amountPerMonth = ProfileItem.ZERO;
        }
        return amountPerMonth;
    }

    protected BigDecimal calculate(Algorithm algorithm, Map<String, Object> values) {

        log.debug("getting value");

        BigDecimal amount = ProfileItem.ZERO;
        String value = null;

        // get our template
        String algorithmContent = algorithm.getContent();

        try {
            // use a Rhino context to evaluate the carbon algorithm
            Context cx = Context.enter();
            Scriptable scope = cx.initStandardObjects();

            for (String key : values.keySet()) {
                scope.put(key, scope, values.get(key));
            }

            Object result = cx.evaluateString(scope, algorithmContent, "", 0, null);
            value = Context.toString(result);
            log.debug("value: " + value);
        } catch (EvaluatorException e) {
            log.warn("caught EvaluatorException: " + e.getMessage());
        } catch (RhinoException e) {
            log.warn("caught RhinoException: " + e.getMessage());

        } finally {
            Context.exit();
        }

        // process result
        if (value != null) {
            try {
                amount = new BigDecimal(value);
                amount = amount.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                if (amount.precision() > ProfileItem.PRECISION) {
                    log.warn("precision is too big: " + amount);
                    // TODO: do something?
                }
            } catch (Exception e) {
                // swallow
                log.warn("caught Exception: " + e);
                // TODO: do something?
            }
            log.debug("got value: " + amount);
        }
        return amount;
    }

    protected Algorithm getAlgorithm(ItemDefinition itemDefinition, String path) {
        for (Algorithm algorithm : itemDefinition.getAlgorithms()) {
            if (algorithm.getName().equalsIgnoreCase(path)) {
                log.debug("found Algorithm");
                return algorithm;
            }
        }
        return null;
    }

    public Map<String, Object> getValues(ProfileItem profileItem) {
        return getValues(profileItem.getDataItem(), profileItem, null);
    }

    public Map<String, Object> getValues(DataItem dataItem, Choices userValueChoices) {
        return getValues(dataItem, null, userValueChoices);
    }

    // TODO: surely this method can be more efficient
    public Map<String, Object> getValues(DataItem dataItem, ProfileItem profileItem, Choices userValueChoices) {
        Map<String, Object> returnValues = new HashMap<String, Object>();
        String key;
        String value;
        Set<ItemValueDefinition> itemValueDefinitions = dataItem.getItemDefinition().getItemValueDefinitions();
        List<ItemValue> dataItemValues = dataItem.getItemValues();
        List<ItemValue> profileItemValues;
        if (profileItem != null) {
            profileItemValues = profileItem.getItemValues();
        } else {
            profileItemValues = null;
        }
        for (ItemValueDefinition itemValueDefinition : itemValueDefinitions) {
            key = itemValueDefinition.getPath();
            // first attempt from ItemValueDefinition
            value = itemValueDefinition.getUsableValue();
            // second attempt from DataItem
            if (dataItemValues != null) {
                for (ItemValue itemValue : dataItemValues) {
                    if (itemValue.getItemValueDefinition().equals(itemValueDefinition)) {
                        if (itemValue.getValue() != null) {
                            value = itemValue.getUsableValue();
                        }
                        break;
                    }
                }
            }
            if (itemValueDefinition.isFromProfile()) {
                if (userValueChoices != null) {
                    // third attempt from userValueChoices
                    if (itemValueDefinition.isFromProfile() && userValueChoices.containsKey(itemValueDefinition.getPath())) {
                        value = userValueChoices.get(itemValueDefinition.getPath()).getValue();
                    }
                } else if (profileItemValues != null) {
                    // third attempt from ProfileItem
                    for (ItemValue itemValue : profileItemValues) {
                        if (itemValue.getItemValueDefinition().equals(itemValueDefinition)) {
                            if (itemValue.getValue() != null) {
                                value = itemValue.getUsableValue();
                            }
                            break;
                        }
                    }
                }
            }
            // now store key & value to map
            if (value != null) {
                if (itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.DECIMAL)) {
                    try {
                        BigDecimal newValue;
                        try {
                            newValue = new BigDecimal(value);
                            newValue = newValue.setScale(ProfileItem.SCALE, ProfileItem.ROUNDING_MODE);
                            if (newValue.precision() > ProfileItem.PRECISION) {
                                log.warn("precision is too big: " + newValue);
                                // TODO: do something?
                            }
                        } catch (Exception e) {
                            // swallow
                            log.warn("caught Exception: " + e);
                            newValue = ProfileItem.ZERO;
                        }
                        returnValues.put(key, newValue);
                    } catch (Exception e) {
                        // swallow
                    }
                } else {
                    returnValues.put(key, value);
                }
            }
        }
        return returnValues;
    }
}