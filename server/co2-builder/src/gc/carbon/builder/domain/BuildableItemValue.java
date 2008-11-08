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
package gc.carbon.builder.domain;

import com.jellymold.utils.domain.APIObject;

import java.util.Date;

import gc.carbon.builder.domain.BuildableItem;
import gc.carbon.builder.domain.BuildableItemValueDefinition;
import gc.carbon.builder.Builder;

public interface BuildableItemValue extends APIObject {
    String getName();

    String getDisplayName();

    String getPath();

    String getDisplayPath();

    String getUid();

    Date getCreated();

    Date getModified();

    String getUnit();

    String getPerUnit();

    String getValue();
    
    BuildableItem getItem();
    
    BuildableItemValueDefinition getItemValueDefinition();

    void setBuilder(Builder builder);
}