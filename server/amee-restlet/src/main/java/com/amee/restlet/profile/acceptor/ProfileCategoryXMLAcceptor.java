package com.amee.restlet.profile.acceptor;

import com.amee.domain.APIUtils;
import com.amee.domain.profile.ProfileItem;
import com.amee.restlet.profile.ProfileCategoryResource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.restlet.data.Form;
import org.restlet.resource.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
@Service
public class ProfileCategoryXMLAcceptor implements IProfileCategoryRepresentationAcceptor {

    private final Log log = LogFactory.getLog(getClass());

    // The default maximum size for batch ProfileItem POSTs.
    private static int MAX_PROFILE_BATCH_SIZE = 50;

    public ProfileCategoryXMLAcceptor() {
        String maxProfileBatchSize = System.getProperty("amee.MAX_PROFILE_BATCH_SIZE");
        if (StringUtils.isNumeric(maxProfileBatchSize)) {
            MAX_PROFILE_BATCH_SIZE = Integer.parseInt(maxProfileBatchSize);   
        }
    }

    @Autowired
    ProfileCategoryFormAcceptor formAcceptor;

    public List<ProfileItem> accept(ProfileCategoryResource resource, Representation entity) {
        List<ProfileItem> profileItems = new ArrayList<ProfileItem>();
        if (entity.isAvailable()) {
            try {
                Element rootElem = APIUtils.getRootElement(entity.getStream());
                if (rootElem.getName().equalsIgnoreCase("ProfileCategory")) {
                    Element profileItemsElem = rootElem.element("ProfileItems");

                    if (profileItemsElem != null) {

                        List elements = profileItemsElem.elements("ProfileItem");

                        // AMEE 2.0 has a maximum allowed size for batch POSTs and PUTs. If this request execeeds that limit
                        // do not process the request and return a 400 status
                        if ((elements.size() > MAX_PROFILE_BATCH_SIZE) && (resource.getAPIVersion().isNotVersionOne())) {
                            resource.badRequest();
                            return profileItems;
                        }

                        // If the POST inputstream contains more than one entity it is considered a batch request.
                        if (elements.size() > 1 && resource.isPost())
                            resource.setIsBatchPost(true);

                        for (Object o1 : elements) {
                            Element profileItemElem = (Element) o1;
                            Form form = new Form();

                            for (Object o2 : profileItemElem.elements()) {
                                Element profileItemValueElem = (Element) o2;
                                form.add(profileItemValueElem.getName(), profileItemValueElem.getText());
                            }

                            // Representations to be returned for batch requests can be specified as a query parameter.
                            form.add("representation", resource.getForm().getFirstValue("representation"));

                            List<ProfileItem> items = formAcceptor.accept(resource, form);
                            if (!items.isEmpty()) {
                                profileItems.addAll(items);
                            } else {
                                log.warn("Profile Item not added");
                                return profileItems;
                            }
                        }
                    }
                } else {
                    log.warn("Profile Category not found");
                }
            } catch (DocumentException e) {
                log.warn("Caught DocumentException: " + e.getMessage(), e);
            } catch (IOException e) {
                log.warn("Caught IOException: " + e.getMessage(), e);
            }
        } else {
            log.warn("XML not available");
        }
        return profileItems;
    }
}
