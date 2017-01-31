/*
 Copyright (C) 2002-2004 MySQL AB

 This program is free software; you can redistribute it and/or modify
 it under the terms of version 2 of the GNU General Public License as
 published by the Free Software Foundation.
 

 There are special exceptions to the terms and conditions of the GPL 
 as it is applied to this software. View the full text of the 
 exception exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
 software distribution.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package testsuite.simple;

import testsuite.BaseTestCase;

import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;


/**
 * DOCUMENT ME!
 *
 * @author Mark Matthews
 * @version $Id: DateTest.java,v 1.4.2.4 2004/08/09 22:15:13 mmatthew Exp $
 */
public class DateTest extends BaseTestCase {
    /**
     * Creates a new DateTest object.
     *
     * @param name DOCUMENT ME!
     */
    public DateTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DateTest.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp() throws Exception {
        super.setUp();
        createTestTable();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void testTimestamp() throws SQLException {
        pstmt = conn.prepareStatement(
                "INSERT INTO DATETEST(tstamp, dt, dtime, tm) VALUES (?, ?, ?, ?)");

        //TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 6);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.HOUR, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.getTime();
        System.out.println(cal);

        DateFormat df = SimpleDateFormat.getInstance();

        //df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Timestamp nowTstamp = new Timestamp(cal.getTime().getTime());
        java.sql.Date nowDate = new java.sql.Date(cal.getTime().getTime());
        Timestamp nowDatetime = new Timestamp(cal.getTime().getTime());
        java.sql.Time nowTime = new java.sql.Time(cal.getTime().getTime());
        System.out.println("** Times with given calendar (before storing) **\n");
        System.out.println("TIMESTAMP:\t" + nowTstamp.getTime() + " -> "
            + df.format(nowTstamp));
        System.out.println("DATE:\t\t" + nowDate.getTime() + " -> "
            + df.format(nowDate));
        System.out.println("DATETIME:\t" + nowDatetime.getTime() + " -> "
            + df.format(nowDatetime));
        System.out.println("TIME:\t\t" + nowTime.getTime() + " -> "
            + df.format(nowTime));
        System.out.println("\n");
        pstmt.setTimestamp(1, nowTstamp);
        pstmt.setDate(2, nowDate);
        pstmt.setTimestamp(3, nowDatetime);
        pstmt.setTime(4, nowTime);
        pstmt.execute();

        pstmt.getUpdateCount();
        pstmt.clearParameters();
        rs = stmt.executeQuery("SELECT * from DATETEST");

        java.sql.Date thenDate = null;

        while (rs.next()) {
            Timestamp thenTstamp = rs.getTimestamp(1);
            thenDate = rs.getDate(2);

            java.sql.Timestamp thenDatetime = rs.getTimestamp(3);
            java.sql.Time thenTime = rs.getTime(4);
            System.out.println(
                "** Times with given calendar (retrieved from database) **\n");
            System.out.println("TIMESTAMP:\t" + thenTstamp.getTime() + " -> "
                + df.format(thenTstamp));
            System.out.println("DATE:\t\t" + thenDate.getTime() + " -> "
                + df.format(thenDate));
            System.out.println("DATETIME:\t" + thenDatetime.getTime() + " -> "
                + df.format(thenDatetime));
            System.out.println("TIME:\t\t" + thenTime.getTime() + " -> "
                + df.format(thenTime));
            System.out.println("\n");
        }

        rs.close();
        rs = null;
    }

    private void createTestTable() throws SQLException {
        //
        // Catch the error, the table might exist
        //
        try {
            stmt.executeUpdate("DROP TABLE DATETEST");
        } catch (SQLException SQLE) {
            ;
        }

        stmt.executeUpdate(
            "CREATE TABLE DATETEST (tstamp TIMESTAMP, dt DATE, dtime DATETIME, tm TIME)");
    }
}
