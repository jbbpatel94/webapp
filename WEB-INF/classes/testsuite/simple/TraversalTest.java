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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Tests result set traversal methods.
 *
 * @author Mark Matthews
 * @version $Id: TraversalTest.java,v 1.6.2.4 2004/08/09 22:15:13 mmatthew Exp $
 */
public class TraversalTest extends BaseTestCase {
    /**
     * Creates a new TraversalTest object.
     *
     * @param name DOCUMENT ME!
     */
    public TraversalTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TraversalTest.class);
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
    public void testTraversal() throws SQLException {
        Statement scrollableStmt = null;

        try {
            scrollableStmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            rs = scrollableStmt.executeQuery(
                    "SELECT * FROM TRAVERSAL ORDER BY pos");

            // Test isFirst()
            if (rs.first()) {
                assertTrue("ResultSet.isFirst() failed", rs.isFirst());
                rs.relative(-1);
                assertTrue("ResultSet.isBeforeFirst() failed",
                    rs.isBeforeFirst());
            }

            // Test isLast()
            if (rs.last()) {
                assertTrue("ResultSet.isLast() failed", rs.isLast());
                rs.relative(1);
                assertTrue("ResultSet.isAfterLast() failed", rs.isAfterLast());
            }

            int count = 0;
            rs.beforeFirst();

            boolean forwardOk = true;

            while (rs.next()) {
                int pos = rs.getInt("POS");

                // test case-sensitive column names
                pos = rs.getInt("pos");
                pos = rs.getInt("Pos");
                pos = rs.getInt("POs");
                pos = rs.getInt("PoS");
                pos = rs.getInt("pOS");
                pos = rs.getInt("pOs");
                pos = rs.getInt("poS");

                if (pos != count) {
                    forwardOk = false;
                }

                assertTrue("ResultSet.getRow() failed.",
                    pos == (rs.getRow() - 1));

                count++;
            }

            assertTrue("Only traversed " + count + " / 100 rows", forwardOk);

            boolean isAfterLast = rs.isAfterLast();
            assertTrue("ResultSet.isAfterLast() failed", isAfterLast);
            rs.afterLast();

            // Scroll backwards
            count = 99;

            boolean reverseOk = true;

            while (rs.previous()) {
                int pos = rs.getInt("pos");

                if (pos != count) {
                    reverseOk = false;
                }

                count--;
            }

            assertTrue("ResultSet.previous() failed", reverseOk);

            boolean isBeforeFirst = rs.isBeforeFirst();
            assertTrue("ResultSet.isBeforeFirst() failed", isBeforeFirst);

            rs.next();

            boolean isFirst = rs.isFirst();
            assertTrue("ResultSet.isFirst() failed", isFirst);

            // Test absolute positioning
            rs.absolute(50);

            int pos = rs.getInt("pos");
            assertTrue("ResultSet.absolute() failed", pos == 49);

            // Test relative positioning
            rs.relative(-1);
            pos = rs.getInt("pos");
            assertTrue("ResultSet.relative(-1) failed", pos == 48);

            // Test bogus absolute index
            boolean onResultSet = rs.absolute(200);
            assertTrue("ResultSet.absolute() to point off result set failed",
                onResultSet == false);
            onResultSet = rs.absolute(100);
            assertTrue("ResultSet.absolute() from off rs to on rs failed",
                onResultSet);

            onResultSet = rs.absolute(-99);
            assertTrue("ResultSet.absolute(-99) failed", onResultSet);
            assertTrue("ResultSet absolute(-99) failed", rs.getInt(1) == 1);
        } finally {
            if (scrollableStmt != null) {
                try {
                    scrollableStmt.close();
                } catch (SQLException sqlEx) {
                    ;
                }
            }
        }
    }

    private void createTestTable() throws SQLException {
        //
        // Catch the error, the table might exist
        //
        try {
            stmt.executeUpdate("DROP TABLE TRAVERSAL");
        } catch (SQLException SQLE) {
            ;
        }

        stmt.executeUpdate(
            "CREATE TABLE TRAVERSAL (pos int PRIMARY KEY, stringdata CHAR(32))");

        for (int i = 0; i < 100; i++) {
            stmt.executeUpdate("INSERT INTO TRAVERSAL VALUES (" + i
                + ", 'StringData')");
        }
    }
}
