package gc.carbon;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.environment.EnvironmentService;
import com.jellymold.utils.HeaderUtils;
import com.jellymold.utils.ThreadBeanHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import gc.carbon.profile.ProfileBrowser;

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
public abstract class BaseResource extends com.jellymold.utils.BaseResource implements BeanFactoryAware {

    protected BeanFactory beanFactory;

    public BaseResource() {
        super();
    }

    public BaseResource(org.restlet.Context context, org.restlet.data.Request request, org.restlet.data.Response response) {
        super(context, request, response);
    }

    public int getItemsPerPage() {
        int itemsPerPage = EnvironmentService.getEnvironment().getItemsPerPage();
        String itemsPerPageStr = getRequest().getResourceRef().getQueryAsForm().getFirstValue("itemsPerPage");
        if (itemsPerPageStr == null) {
            itemsPerPageStr = HeaderUtils.getHeaderFirstValue("ItemsPerPage", getRequest());
        }
        if (itemsPerPageStr != null) {
            try {
                itemsPerPage = Integer.valueOf(itemsPerPageStr);
            } catch (NumberFormatException e) {
                // swallow
            }
        }
        return itemsPerPage;
    }

    public ProfileBrowser getProfileBrowser() {
        ProfileBrowser profileBrowser = (ProfileBrowser) ThreadBeanHolder.get("profileBrowser");
        if (profileBrowser == null) {
            profileBrowser = (ProfileBrowser) beanFactory.getBean("profileBrowser");
            ThreadBeanHolder.set("profileBrowser", profileBrowser);
        }
        return profileBrowser;
    }
    
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
