/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
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
package com.amee.domain.profile;

import org.springframework.stereotype.Service;

/**
 * Minimal service interface allowing a ProfileItem to calculate it's own CO2 Amount.
 *
 * Note: the interface is required to mitigate circular dependencies between the amee-calculation and amee-domain
 * packages.
 */
@Service
public interface CO2CalculationService {

    /**
     * Calculate the {@link com.amee.domain.core.CO2Amount CO2Amount} of a ProfileItem. The calculated value is
     * set into the passed {@link ProfileItem}.
     *
     * @param profileItem - the {@link ProfileItem} for which to calculate the
     * {@link com.amee.domain.core.CO2Amount CO2Amount}
     */
    void calculate(ProfileItem profileItem);
}
