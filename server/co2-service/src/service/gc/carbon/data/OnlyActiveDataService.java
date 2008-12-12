package gc.carbon.data;

import gc.carbon.domain.profile.StartEndDate;
import gc.carbon.domain.data.DataCategory;
import gc.carbon.domain.data.DataItem;
import gc.carbon.data.DataService;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

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
public class OnlyActiveDataService extends DataService {

    DataService delegatee;

    public OnlyActiveDataService(DataService delegatee) {
        this.delegatee = delegatee;
    }

    public List<DataItem> getDataItems(final DataCategory dataCategory, final StartEndDate startDate, final StartEndDate endDate) {

       List<DataItem> requestedItems;

        final List<DataItem> dataItems = delegatee.getDataItems(dataCategory, startDate, endDate);
        requestedItems = (List) CollectionUtils.select(dataItems, new Predicate() {
            public boolean evaluate(Object o) {
                DataItem di = (DataItem) o;
                for (DataItem dataItem : dataItems) {
                    if (di.getStartDate().before(dataItem.getStartDate()) &&
                            !dataItem.getStartDate().after(startDate.toDate())) {
                        return false;
                    }
                }
                return true;
            }
        });
        return requestedItems;
    }
}