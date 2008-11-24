package gc.carbon.test.profile;

import org.testng.annotations.Test;
import org.restlet.data.Status;
import org.restlet.data.Form;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.w3c.dom.Document;
import gc.carbon.test.profile.BaseProfileCategoryTestCase;

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
public class ProfileCategoryGETV1 extends BaseProfileCategoryTestCase {

    private DateTimeFormatter VALID_FROM_FMT = DateTimeFormat.forPattern("yyyyMMdd");
    private DateTimeFormatter START_DATE_FMT = DateTimeFormat.forPattern("yyyyMMdd'T'HHmm");


    public ProfileCategoryGETV1(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        initDB();
        super.setUp();
    }

    private String create(DateTime startDate) throws Exception {
         Form data = new Form();
         data.add("validFrom",startDate.toString(VALID_FROM_FMT));
         return createProfileItem(data);
    }

    @Test
    public void testInValidEndDateRequest() throws Exception {
        getReference().setQuery("endDate=20100401");
        Status status = doGet().getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testInValidDurationRequest() throws Exception {
        getReference().setQuery("duration=PT30M");
        Status status = doGet().getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testInValidStartDateRequest() throws Exception {
        getReference().setQuery("startDate=20100401");
        Status status = doGet().getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testIdenticalAPIResponsesWithV1Data() throws Exception {
        DateTime startDate = new DateTime();

        String uid = create(startDate);
        getReference().setQuery("validFrom=" + VALID_FROM_FMT.print(startDate));

        DomRepresentation rep = doGet().getEntityAsDom();
        rep.write(System.out);

        assertXpathExists("//ProfileItem[@uid='" + uid + "']", rep.getDocument());

        getReference().setQuery("v=2.0&startDate=" + START_DATE_FMT.print(startDate));
        rep = doGet().getEntityAsDom();
        rep.write(System.out);
        assertXpathExists("//ProfileItem[@uid='" + uid + "']", rep.getDocument());

    }
}