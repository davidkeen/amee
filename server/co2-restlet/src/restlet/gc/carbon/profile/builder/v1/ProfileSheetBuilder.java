package gc.carbon.profile.builder.v1;

import com.jellymold.utils.cache.CacheableFactory;
import com.jellymold.utils.ThreadBeanHolder;
import com.jellymold.utils.ValueType;
import com.jellymold.sheet.Column;
import com.jellymold.sheet.Row;
import com.jellymold.sheet.Sheet;
import com.jellymold.sheet.Cell;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import gc.carbon.profile.ProfileBrowser;
import gc.carbon.profile.ProfileService;
import gc.carbon.domain.data.builder.BuildableItemValue;
import gc.carbon.domain.data.builder.BuildableItemDefinition;
import gc.carbon.domain.data.builder.BuildableItemValueDefinition;
import gc.carbon.domain.profile.builder.BuildableProfileItem;
import gc.carbon.domain.profile.ProfileItem;

public class ProfileSheetBuilder implements CacheableFactory {

        private static final String DAY_DATE = "yyyyMMdd";
        private static DateFormat DAY_DATE_FMT = new SimpleDateFormat(DAY_DATE);

        private ProfileService profileService;

        ProfileSheetBuilder(ProfileService profileService) {
            super();
            this.profileService = profileService;
        }

        public Object create() {

            List<Column> columns;
            Row row;
            Map<String, ? extends BuildableItemValue> itemValuesMap;
            BuildableItemValue itemValue;
            BuildableItemDefinition itemDefinition;
            Sheet sheet = null;
            ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");

            // must have ItemDefinition
            itemDefinition = profileBrowser.getDataCategory().getItemDefinition();
            if (itemDefinition != null) {

                List<ProfileItem> profileItems = profileService.getProfileItems(profileBrowser.getProfile(), profileBrowser.getDataCategory(), profileBrowser.getProfileDate());

                // create sheet and columns
                sheet = new Sheet();
                sheet.setKey(getKey());
                sheet.setLabel("ProfileItems");
                for (BuildableItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                    if (itemValueDefinition.isFromProfile()) {
                        new Column(sheet, itemValueDefinition.getPath(), itemValueDefinition.getName());
                    }
                }

                new Column(sheet, "name");
                new Column(sheet, "amountPerMonth");
                new Column(sheet, "validFrom");
                new Column(sheet, "end");
                new Column(sheet, "path");
                new Column(sheet, "uid", true);
                new Column(sheet, "created", true);
                new Column(sheet, "modified", true);
                new Column(sheet, "dataItemLabel");
                new Column(sheet, "dataItemUid");

                // create rows and cells
                columns = sheet.getColumns();
                for (BuildableProfileItem profileItem : profileItems) {
                    itemValuesMap = profileItem.getItemValuesMap();
                    row = new Row(sheet, profileItem.getUid());
                    row.setLabel("ProfileItem");
                    for (Column column : columns) {
                        itemValue = itemValuesMap.get(column.getName());
                        if (itemValue != null) {
                            new Cell(column, row, itemValue.getValue(), itemValue.getUid(), itemValue.getItemValueDefinition().getValueDefinition().getValueType());
                        } else if ("name".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getName(), ValueType.TEXT);
                        } else if ("amountPerMonth".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getAmount(), ValueType.DECIMAL);
                        } else if ("validFrom".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, DAY_DATE_FMT.format(profileItem.getStartDate()), ValueType.TEXT);
                        } else if ("end".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.isEnd(), ValueType.BOOLEAN);
                        } else if ("path".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getDisplayPath(), ValueType.TEXT);
                        } else if ("uid".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getUid(), ValueType.TEXT);
                        } else if ("created".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getCreated(), ValueType.DATE);
                        } else if ("modified".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getModified(), ValueType.DATE);
                        } else if ("dataItemUid".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getDataItem().getUid(), profileItem.getDataItem().getUid(), ValueType.TEXT);
                        } else if ("dataItemLabel".equalsIgnoreCase(column.getName())) {
                            new Cell(column, row, profileItem.getDataItem().getLabel(), ValueType.TEXT);
                        } else {
                            // add empty cell
                            new Cell(column, row);
                        }
                    }
                }


                // sort columns and rows in sheet
                sheet.addDisplayBy("dataItemLabel");
                sheet.addDisplayBy("amountPerMonth");
                sheet.sortColumns();
                sheet.addSortBy("dataItemLabel");
                sheet.addSortBy("amountPerMonth");
                sheet.sortRows();
            }

            return sheet;
        }

        public String getKey() {
            ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowserForFactory");
            return "ProfileSheet_" + profileBrowser.getProfile().getUid() + "_" + profileBrowser.getDataCategory().getUid() + "_" + profileBrowser.getProfileDate().getTime();
        }

        public String getCacheName() {
            return "ProfileSheets";
        }
    }