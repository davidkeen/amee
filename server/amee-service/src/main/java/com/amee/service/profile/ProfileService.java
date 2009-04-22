package com.amee.service.profile;

import com.amee.domain.*;
import com.amee.domain.cache.CacheableFactory;
import com.amee.domain.data.DataCategory;
import com.amee.domain.data.ItemValue;
import com.amee.domain.data.ItemValueDefinition;
import com.amee.domain.environment.Environment;
import com.amee.domain.profile.Profile;
import com.amee.domain.profile.ProfileItem;
import com.amee.domain.sheet.Sheet;
import com.amee.service.BaseService;
import com.amee.service.transaction.TransactionController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Primary service interface for Profile Resources.
 * <p/>
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
@Service
public class ProfileService extends BaseService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private ProfileServiceDAO dao;

    @Autowired
    private ProfileSheetService profileSheetService;

    @Autowired
    private AMEEStatistics ameeStatistics;

    // Profiles

    /**
     * Fetches a Profile based on the supplied path. If the path is a valid UID format then the
     * Profile with this UID is returned. If a profile with the UID is not found or the path is
     * not a valid UID format then a Profile with the matching path is searched for and returned.
     *
     * @param environment that requested Profile belongs to
     * @param path to search for. Can be either a UID or a path alias.
     * @return the matching Profile
     */
    public Profile getProfile(Environment environment, String path) {
        Profile profile = null;
        if (!StringUtils.isBlank(path)) {
            if (UidGen.isValid(path)) {
                profile = getProfileByUid(environment, path);
            }
            if (profile == null) {
                profile = getProfileByPath(environment, path);
            }
        }
        return profile;
    }

    public Profile getProfileByUid(Environment environment, String uid) {
        Profile profile = dao.getProfileByUid(uid);
        checkEnvironmentObject(environment, profile);
        return profile;
    }

    public Profile getProfileByPath(Environment environment, String path) {
        return dao.getProfileByPath(environment, path);
    }

    public List<Profile> getProfiles(Environment environment, Pager pager) {
        return dao.getProfiles(environment, pager);
    }

    public void persist(Profile p) {
        dao.persist(p);
    }

    public void remove(Profile p) {
        dao.remove(p);
    }

    public void clearCaches(Profile profile) {
        log.debug("clearCaches()");
        profileSheetService.removeSheets(profile);
    }

    // ProfileItems

    public ProfileItem getProfileItem(String uid) {
        return checkProfileItem(dao.getProfileItem(uid));
    }

    public List<ProfileItem> getProfileItems(Profile p) {
        return dao.getProfileItems(p);
    }

    public List<ProfileItem> getProfileItems(Profile p, DataCategory dc, Date date) {
        return checkProfileItems(dao.getProfileItems(p, dc, date));
    }

    public List<ProfileItem> getProfileItems(
            Profile profile,
            DataCategory dataCategory,
            StartEndDate startDate,
            StartEndDate endDate) {
        return checkProfileItems(dao.getProfileItems(profile, dataCategory, startDate, endDate));
    }

    public List<ProfileItem> checkProfileItems(List<ProfileItem> profileItems) {
        for (ProfileItem profileItem : profileItems) {
            checkProfileItem(profileItem);
        }
        return profileItems;
    }

    /**
     * Add to the {@link com.amee.domain.profile.ProfileItem} any {@link com.amee.domain.data.ItemValue}s it is missing.
     * This will be the case on first persist (this method acting as a reification function), and between GETs if any
     * new {@link com.amee.domain.data.ItemValueDefinition}s have been added to the underlying
     * {@link com.amee.domain.data.ItemDefinition}.
     * <p/>
     * Any updates to the {@link com.amee.domain.profile.ProfileItem} will be persisted to the database.
     *
     * @param profileItem to check
     * @return the supplied ProfileItem or null
     */
    public ProfileItem checkProfileItem(ProfileItem profileItem) {

        if (profileItem == null) {
            return null;
        }

        APIVersion apiVersion = profileItem.getProfile().getAPIVersion();
        Set<ItemValueDefinition> existingItemValueDefinitions = profileItem.getItemValueDefinitions();
        Set<ItemValueDefinition> missingItemValueDefinitions = new HashSet<ItemValueDefinition>();

        // find ItemValueDefinitions not currently implemented in this Item
        for (ItemValueDefinition ivd : profileItem.getItemDefinition().getItemValueDefinitions()) {
            if (ivd.isFromProfile() && ivd.getAPIVersions().contains(apiVersion)) {
                if (!existingItemValueDefinitions.contains(ivd)) {
                    missingItemValueDefinitions.add(ivd);
                }
            }
        }

        // Do we need to add any ItemValueDefinitions?
        if (missingItemValueDefinitions.size() > 0) {

            // Ensure a transaction has been opened. The implementation of open-session-in-view we are using
            // does not open transactions for GETs. This method is called for certain GETs.
            transactionController.begin(true);

            // create missing ItemValues
            for (ItemValueDefinition ivd : missingItemValueDefinitions) {
                // start default value with value from ItemValueDefinition
                String defaultValue = ivd.getValue();
                // next give DataItem a chance to set the default value, if appropriate
                if (ivd.isFromData()) {
                    Map<String, ItemValue> dataItemValues = profileItem.getDataItem().getItemValuesMap();
                    ItemValue dataItemValue = dataItemValues.get(ivd.getPath());
                    if ((dataItemValue != null) && (dataItemValue.getValue().length() > 0)) {
                        defaultValue = dataItemValue.getValue();
                    }
                }
                // create missing ItemValue
                new ItemValue(ivd, profileItem, defaultValue);
                ameeStatistics.createProfileItemValue();
            }
        }

        return profileItem;
    }

    public boolean isUnique(ProfileItem pi) {
        return !dao.equivilentProfileItemExists(pi);
    }

    public void persist(ProfileItem pi) {
        dao.persist(pi);
        checkProfileItem(pi);
    }

    public void remove(ProfileItem pi) {
        dao.remove(pi);
    }

    // Profile DataCategories

    public Collection<Long> getProfileDataCategoryIds(Profile profile) {
        return dao.getProfileDataCategoryIds(profile);
    }

    // Sheets

    public Sheet getSheet(CacheableFactory sheetFactory) {
        return profileSheetService.getSheet(sheetFactory);
    }
}