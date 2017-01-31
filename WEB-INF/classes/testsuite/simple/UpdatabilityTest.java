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

import com.mysql.jdbc.NotUpdatable;

import testsuite.BaseTestCase;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Tests for updatable result sets
 *
 * @author Mark Matthews
 * @version $Id: UpdatabilityTest.java,v 1.6.2.9 2004/08/09 22:15:13 mmatthew Exp $
 */
public class UpdatabilityTest extends BaseTestCase {
    /**
     * Creates a new UpdatabilityTest object.
     *
     * @param name DOCUMENT ME!
     */
    public UpdatabilityTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(UpdatabilityTest.class);
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
     * If using MySQL-4.1, tests if aliased tables work as updatable result
     * sets.
     *
     * @throws Exception if an error occurs
     */
    public void testAliasedTables() throws Exception {
        DatabaseMetaData dbmd = this.conn.getMetaData();

        if ((dbmd.getDatabaseMajorVersion() >= 4)
                && (dbmd.getDatabaseMinorVersion() >= 1)) {
            Statement scrollableStmt = null;

            try {
                scrollableStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                rs = scrollableStmt.executeQuery(
                        "SELECT pos1 AS p1, pos2 AS P2, char_field AS cf FROM UPDATABLE AS UPD LIMIT 1");
                rs.next();
                rs.close();
                rs = null;

                scrollableStmt.close();
                scrollableStmt = null;
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        ; // ignore
                    }

                    rs = null;
                }

                if (scrollableStmt != null) {
                    try {
                        scrollableStmt.close();
                    } catch (SQLException sqlEx) {
                        ; // ignore
                    }

                    scrollableStmt = null;
                }
            }
        }
    }

    /**
     * Tests that the driver does not let you update result sets that come from
     * tables that don't have primary keys
     *
     * @throws SQLException if an error occurs
     */
    public void testBogusTable() throws SQLException {
        stmt.executeUpdate("DROP TABLE IF EXISTS BOGUS_UPDATABLE");
        stmt.executeUpdate("CREATE TABLE BOGUS_UPDATABLE (field1 int)");

        Statement scrollableStmt = null;

        try {
            scrollableStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            rs = scrollableStmt.executeQuery("SELECT * FROM BOGUS_UPDATABLE");

            try {
                rs.moveToInsertRow();
                fail(
                    "ResultSet.moveToInsertRow() should not succeed on non-updatable table");
            } catch (NotUpdatable noUpdate) {
                // ignore
            }
        } finally {
            if (scrollableStmt != null) {
                try {
                    scrollableStmt.close();
                } catch (SQLException sqlEx) {
                    ;
                }
            }

            stmt.executeUpdate("DROP TABLE IF EXISTS BOGUS_UPDATABLE");
        }
    }

    /**
     * Tests that the driver does not let you update result sets that come from
     * queries that haven't selected all primary keys
     *
     * @throws SQLException if an error occurs
     */
    public void testMultiKeyTable() throws SQLException {
        stmt.executeUpdate("DROP TABLE IF EXISTS MULTI_UPDATABLE");
        stmt.executeUpdate(
            "CREATE TABLE MULTI_UPDATABLE (field1 int NOT NULL, field2 int NOT NULL, PRIMARY KEY (field1, field2))");

        Statement scrollableStmt = null;

        try {
            scrollableStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            rs = scrollableStmt.executeQuery(
                    "SELECT field1 FROM MULTI_UPDATABLE");

            try {
                rs.moveToInsertRow();
                fail(
                    "ResultSet.moveToInsertRow() should not succeed on query that does not select all primary keys");
            } catch (NotUpdatable noUpdate) {
                // ignore
            }
        } finally {
            if (scrollableStmt != null) {
                try {
                    scrollableStmt.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }
            }

            stmt.executeUpdate("DROP TABLE IF EXISTS MULTI_UPDATABLE");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void testUpdatability() throws SQLException {
        Statement scrollableStmt = null;

        try {
            scrollableStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            rs = scrollableStmt.executeQuery(
                    "SELECT * FROM UPDATABLE ORDER BY pos1");

            rs.getMetaData().getColumnCount();

            while (rs.next()) {
                int rowPos = rs.getInt(1);
                rs.updateString(3, "New Data" + (100 - rowPos));
                rs.updateRow();
            }

            //
            // Insert a new row
            //
            rs.moveToInsertRow();

            // Test default values retrieval
            assertTrue((rs.getInt(1) == 0) && (rs.getInt(2) == 0)
                && (rs.getString(3) == null));
            rs.updateInt(1, 400);
            rs.updateInt(2, 400);
            rs.updateString(3, "New Data" + (100 - 400));

            // Test updated data visibility.
            assertTrue((rs.getInt(1) == 400) && (rs.getInt(2) == 400)
                && rs.getString(3).equals("New Data" + (100 - 400)));
            rs.insertRow();

            // Test moveToCurrentRow
            int rememberedPosition = rs.getRow();
            rs.moveToInsertRow();
            rs.moveToCurrentRow();
            assertTrue("ResultSet.moveToCurrentRow() failed",
                rs.getRow() == rememberedPosition);
            rs.close();
            rs = scrollableStmt.executeQuery(
                    "SELECT * FROM UPDATABLE ORDER BY pos1");

            boolean dataGood = true;

            while (rs.next()) {
                int rowPos = rs.getInt(1);

                if (!rs.getString(3).equals("New Data" + (100 - rowPos))) {
                    dataGood = false;
                }
            }

            assertTrue("Updates failed", dataGood);

            // move back, and change the primary key
            // This should work
            int newPrimaryKeyId = 99999;
            rs.absolute(1);
            rs.updateInt(1, newPrimaryKeyId);
            rs.updateRow();

            int savedPrimaryKeyId = rs.getInt(1);
            assertTrue("Updated primary key does not match",
                (newPrimaryKeyId == savedPrimaryKeyId));

            // Check cancelRowUpdates()
            rs.absolute(1);

            int primaryKey = rs.getInt(1);
            int originalValue = rs.getInt(2);
            rs.updateInt(2, -3);
            rs.cancelRowUpdates();

            int newValue = rs.getInt(2);
            assertTrue("ResultSet.cancelRowUpdates() failed",
                newValue == originalValue);

            // Now check refreshRow()
            // Check cancelRowUpdates()
            rs.absolute(1);
            primaryKey = rs.getInt(1);
            scrollableStmt.executeUpdate(
                "UPDATE UPDATABLE SET char_field='foo' WHERE pos1="
                + primaryKey);
            rs.refreshRow();
            assertTrue("ResultSet.refreshRow failed",
                rs.getString("char_field").equals("foo"));

            // Now check deleteRow()
            rs.last();

            int oldLastRow = rs.getRow();
            rs.deleteRow();
            rs.last();
            assertTrue("ResultSet.deleteRow() failed",
                rs.getRow() == (oldLastRow - 1));
            rs.close();

            /*
               FIXME: Move to regression

                scrollableStmt.executeUpdate("DROP TABLE IF EXISTS test");
                scrollableStmt.executeUpdate("CREATE TABLE test (ident INTEGER PRIMARY KEY, name TINYTEXT, expiry DATETIME default null)");
                scrollableStmt.executeUpdate("INSERT INTO test SET ident=1, name='original'");

                           //Select to get a resultset to work on
                           ResultSet rs = stmt.executeQuery("SELECT ident, name, expiry FROM test");

                           //Check that the expiry field was null before we did our update
                           rs.first();

                           java.sql.Date before = rs.getDate("expiry");

                           if (rs.wasNull()) {
                               System.out.println("Expiry was correctly SQL null before update");
                           }

                           //Update a different field
                           rs.updateString("name", "Updated");
                           rs.updateRow();

                           //Test to see if field has been altered
                           java.sql.Date after = rs.getDate(3);

                           if (rs.wasNull())
                               System.out.println("Bug disproved - expiry SQL null after update");
                           else
                               System.out.println("Bug proved - expiry corrupted to '" +
                                                  after + "'");
             */
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
            stmt.executeUpdate("DROP TABLE UPDATABLE");
        } catch (SQLException SQLE) {
            ;
        }

        stmt.executeUpdate(
            "CREATE TABLE UPDATABLE (pos1 int not null, pos2 int not null, char_field VARCHAR(32), PRIMARY KEY (pos1, pos2))");

        for (int i = 0; i < 100; i++) {
            stmt.executeUpdate("INSERT INTO UPDATABLE VALUES (" + i + ", " + i
                + ",'StringData" + i + "')");
        }
    }
}
