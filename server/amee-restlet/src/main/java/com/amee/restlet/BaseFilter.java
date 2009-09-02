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
package com.amee.restlet;

import com.amee.domain.auth.User;
import com.amee.domain.environment.Environment;
import com.amee.domain.site.ISite;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;

public class BaseFilter extends Filter {

    public BaseFilter() {
        super();
    }

    public BaseFilter(Context context) {
        super(context);
    }

    public BaseFilter(Context context, Restlet next) {
        super(context, next);
    }

    public Environment getActiveEnvironment() {
        return (Environment) Request.getCurrent().getAttributes().get("activeEnvironment");
    }

    public ISite getActiveSite() {
        return (ISite) Request.getCurrent().getAttributes().get("activeSite");
    }

    public User getActiveUser() {
        return (User) Request.getCurrent().getAttributes().get("activeUser");
    }
}
