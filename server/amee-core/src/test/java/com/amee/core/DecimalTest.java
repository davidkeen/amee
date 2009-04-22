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
package com.amee.core;

import static junit.framework.Assert.assertTrue;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DecimalTest {

    DateTime now;
    DataSeries lhs;
    DataSeries rhs;
    DataPoint rhp;

    @Before
    public void init() {

        now = new DateTime();

        // Test adding two series
        List<DataPoint> a = new ArrayList<DataPoint>();
        a.add(new DataPoint(now.plusDays(1), new Decimal("1")));
        a.add(new DataPoint(now.plusDays(2), new Decimal("2")));
        a.add(new DataPoint(now.plusDays(3), new Decimal("3")));
        lhs = new DataSeries(a);

        List<DataPoint> b = new ArrayList<DataPoint>();
        b.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        b.add(new DataPoint(now.plusDays(2), new Decimal("3")));
        b.add(new DataPoint(now.plusDays(3), new Decimal("4")));
        rhs = new DataSeries(b);

        rhp = new DataPoint(now.plusDays(1), new Decimal("4"));
    }

    @Test
    public void add() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> sum = new ArrayList<DataPoint>();
        sum.add(new DataPoint(now.plusDays(1), new Decimal("3")));
        sum.add(new DataPoint(now.plusDays(2), new Decimal("5")));
        sum.add(new DataPoint(now.plusDays(3), new Decimal("7")));
        actual = new DataSeries(sum);
        test = lhs.add(rhs);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));


        sum = new ArrayList<DataPoint>();
        sum.add(new DataPoint(now.plusDays(1), new Decimal("5")));
        sum.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        sum.add(new DataPoint(now.plusDays(3), new Decimal("7")));
        actual = new DataSeries(sum);
        test = lhs.add(rhp);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));
    }

    @Test
    public void subtract() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("-1")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("-1")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("-1")));
        actual = new DataSeries(diff);
        test = lhs.subtract(rhs);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("-3")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("-2")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("-1")));
        actual = new DataSeries(diff);
        test = lhs.subtract(rhp);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));
    }

    @Test
    public void divide() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("0.5")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("2").divide(new Decimal("3"))));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("0.75")));
        actual = new DataSeries(diff);
        test = lhs.divide(rhs);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("0.25")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("0.5")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("0.75")));
        actual = new DataSeries(diff);
        test = lhs.divide(rhp);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));
    }

    @Test
    public void multiply() {

        DataSeries test;
        DataSeries actual;

        List<DataPoint> diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("2")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("6")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("12")));
        actual = new DataSeries(diff);
        test = lhs.multiply(rhs);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));


        diff = new ArrayList<DataPoint>();
        diff.add(new DataPoint(now.plusDays(1), new Decimal("4")));
        diff.add(new DataPoint(now.plusDays(2), new Decimal("8")));
        diff.add(new DataPoint(now.plusDays(3), new Decimal("16")));
        actual = new DataSeries(diff);
        test = lhs.multiply(rhp);
        assertTrue("Integrate should produce the correct value",test.integrate().equals(actual.integrate()));
    }

}