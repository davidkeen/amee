package gc.carbon.test.profile.v1;

import org.junit.Test;
import org.restlet.data.*;
import org.restlet.resource.DomRepresentation;
import org.w3c.dom.Document;
import gc.carbon.test.profile.BaseProfileItemTest;
import gc.carbon.test.TestClient;
import com.sun.org.apache.xalan.internal.xsltc.DOM;

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
public class ProfileItemPUTTest extends BaseProfileItemTest {

    public ProfileItemPUTTest() {
        super();
    }

    public ProfileItemPUTTest(String name) throws Exception {
        super(name);
        initDB();
    }

    private void doAssertSimilarXML(Form data) throws Exception {
        Response response = doPut(data);
        setControl("put-transport-car-generic.xml");
        assertXMLSimilar(response);
    }

    @Test
    public void testPutWithValidFromAndDistanceKmPerMonth() throws Exception {
        Form data = new Form();
        data.add("validFrom", "20500101");
        data.add("distanceKmPerMonth", "1000");
        doAssertSimilarXML(data);
    }

    @Test
    public void testPutWithStartDate() throws Exception {
        Form data = new Form();
        data.add("startDate", "20100401T0000");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testPutWithEndDate() throws Exception {
        Form data = new Form();
        data.add("endDate", "20100401T0000");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }

    @Test
    public void testPutWithDuration() throws Exception {
        Form data = new Form();
        data.add("duration", "PT30M");
        Status status = doPut(data).getStatus();
        assertEquals("Should be Bad Request",400,status.getCode());
    }


}