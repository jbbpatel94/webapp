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

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


/**
 * Tests DatabaseMetaData methods.
 *
 * @author Mark Matthews
 * @version $Id: MetadataTest.java,v 1.8.2.10 2005/04/06 14:12:56 mmatthews Exp $
 */
public class MetadataTest extends BaseTestCase {
    /**
     * Creates a new MetadataTest object.
     *
     * @param name DOCUMENT ME!
     */
    public MetadataTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(MetadataTest.class);
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
    public void testForeignKeys() throws SQLException {
        DatabaseMetaData dbmd = conn.getMetaData();
        rs = dbmd.getImportedKeys(null, null, "child");

        while (rs.next()) {
            String pkColumnName = rs.getString("PKCOLUMN_NAME");
            String fkColumnName = rs.getString("FKCOLUMN_NAME");
            assertTrue("Primary Key not returned correctly ('" + pkColumnName
                + "' != 'parent_id')",
                pkColumnName.equalsIgnoreCase("parent_id"));
            assertTrue("Foreign Key not returned correctly ('" + fkColumnName
                + "' != 'parent_id_fk')",
                fkColumnName.equalsIgnoreCase("parent_id_fk"));
        }

        rs.close();
        rs = dbmd.getExportedKeys(null, null, "parent");

        while (rs.next()) {
            String pkColumnName = rs.getString("PKCOLUMN_NAME");
            String fkColumnName = rs.getString("FKCOLUMN_NAME");
            String fkTableName = rs.getString("FKTABLE_NAME");
            assertTrue("Primary Key not returned correctly ('" + pkColumnName
                + "' != 'parent_id')",
                pkColumnName.equalsIgnoreCase("parent_id"));
            assertTrue(
                "Foreign Key table not returned correctly for getExportedKeys ('"
                + fkTableName + "' != 'child')",
                fkTableName.equalsIgnoreCase("child"));
            assertTrue(
                "Foreign Key not returned correctly for getExportedKeys ('"
                + fkColumnName + "' != 'parent_id_fk')",
                fkColumnName.equalsIgnoreCase("parent_id_fk"));
        }

        rs.close();

        rs = dbmd.getCrossReference(null, null, "cpd_foreign_3", null, null,
                "cpd_foreign_4");

        while (rs.next()) {
            String pkColumnName = rs.getString("PKCOLUMN_NAME");
            String pkTableName = rs.getString("PKTABLE_NAME");
            String fkColumnName = rs.getString("FKCOLUMN_NAME");
            String fkTableName = rs.getString("FKTABLE_NAME");
            String deleteAction = cascadeOptionToString(rs.getInt("DELETE_RULE"));
            String updateAction = cascadeOptionToString(rs.getInt("UPDATE_RULE"));

            System.out.println("[D] " + deleteAction);
            System.out.println("[U] " + updateAction);

            System.out.println(pkTableName + "(" + pkColumnName + ") -> "
                + fkTableName + "(" + fkColumnName + ")");
        }

        rs.close();

        rs = dbmd.getImportedKeys(null, null, "fktable2");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void testGetPrimaryKeys() throws SQLException {
        try {
            DatabaseMetaData dbmd = conn.getMetaData();
            rs = dbmd.getPrimaryKeys(conn.getCatalog(), "", "multikey");

            short[] keySeqs = new short[4];
            String[] columnNames = new String[4];
            int i = 0;

            while (rs.next()) {
                rs.getString("TABLE_NAME");
                columnNames[i] = rs.getString("COLUMN_NAME");

                rs.getString("PK_NAME");
                keySeqs[i] = rs.getShort("KEY_SEQ");
                i++;
            }

            if ((keySeqs[0] != 3) && (keySeqs[1] != 2) && (keySeqs[2] != 4)
                    && (keySeqs[4] != 1)) {
                fail("Keys returned in wrong order");
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    /* ignore */
                }
            }
        }
    }

    private static String cascadeOptionToString(int option) {
        switch (option) {
        case DatabaseMetaData.importedKeyCascade:
            return "CASCADE";

        case DatabaseMetaData.importedKeySetNull:
            return "SET NULL";

        case DatabaseMetaData.importedKeyRestrict:
            return "RESTRICT";

        case DatabaseMetaData.importedKeyNoAction:
            return "NO ACTION";
        }

        return "SET DEFAULT";
    }

    private void createTestTable() throws SQLException {
        stmt.executeUpdate("DROP TABLE IF EXISTS child");
        stmt.executeUpdate("DROP TABLE IF EXISTS parent");
        stmt.executeUpdate("DROP TABLE IF EXISTS multikey");
        stmt.executeUpdate("DROP TABLE IF EXISTS cpd_foreign_4");
        stmt.executeUpdate("DROP TABLE IF EXISTS cpd_foreign_3");
        stmt.executeUpdate("DROP TABLE IF EXISTS cpd_foreign_2");
        stmt.executeUpdate("DROP TABLE IF EXISTS cpd_foreign_1");
        stmt.executeUpdate("DROP TABLE IF EXISTS fktable2");
        stmt.executeUpdate("DROP TABLE IF EXISTS fktable1");

        stmt.executeUpdate(
            "CREATE TABLE parent(parent_id INT NOT NULL, PRIMARY KEY (parent_id)) TYPE=INNODB");
        stmt.executeUpdate(
            "CREATE TABLE child(child_id INT, parent_id_fk INT, INDEX par_ind (parent_id_fk), "
            + "FOREIGN KEY (parent_id_fk) REFERENCES parent(parent_id)) TYPE=INNODB");
        stmt.executeUpdate(
            "CREATE TABLE multikey(d INT NOT NULL, b INT NOT NULL, a INT NOT NULL, c INT NOT NULL, PRIMARY KEY (d, b, a, c))");

        // Test compound foreign keys
        stmt.executeUpdate("create table cpd_foreign_1("
            + "id int(8) not null auto_increment primary key,"
            + "name varchar(255) not null unique," + "key (id)"
            + ") type=InnoDB");
        stmt.executeUpdate("create table cpd_foreign_2("
            + "id int(8) not null auto_increment primary key," + "key (id),"
            + "name varchar(255)" + ") type=InnoDB");
        stmt.executeUpdate("create table cpd_foreign_3("
            + "cpd_foreign_1_id int(8) not null,"
            + "cpd_foreign_2_id int(8) not null," + "key(cpd_foreign_1_id),"
            + "key(cpd_foreign_2_id),"
            + "primary key (cpd_foreign_1_id, cpd_foreign_2_id),"
            + "foreign key (cpd_foreign_1_id) references cpd_foreign_1(id),"
            + "foreign key (cpd_foreign_2_id) references cpd_foreign_2(id)"
            + ") type=InnoDB");
        stmt.executeUpdate("create table cpd_foreign_4("
            + "cpd_foreign_1_id int(8) not null,"
            + "cpd_foreign_2_id int(8) not null," + "key(cpd_foreign_1_id),"
            + "key(cpd_foreign_2_id),"
            + "primary key (cpd_foreign_1_id, cpd_foreign_2_id),"
            + "foreign key (cpd_foreign_1_id, cpd_foreign_2_id) "
            + "references cpd_foreign_3(cpd_foreign_1_id, cpd_foreign_2_id) "
            + "ON DELETE RESTRICT ON UPDATE CASCADE" + ") type=InnoDB");

        stmt.executeUpdate(
            "create table fktable1 (TYPE_ID int not null, TYPE_DESC varchar(32), primary key(TYPE_ID)) TYPE=InnoDB");
        stmt.executeUpdate(
            "create table fktable2 (KEY_ID int not null, COF_NAME varchar(32), PRICE float, TYPE_ID int, primary key(KEY_ID), "
            + "index(TYPE_ID), foreign key(TYPE_ID) references fktable1(TYPE_ID)) TYPE=InnoDB");
    }
    
    /**
     * Tests detection of read-only fields when the server is 4.1.0 or newer.
     * 
     * @throws Exception if the test fails.
     */
    public void testRSMDIsReadOnly() throws Exception {
    	try {
    		this.rs = this.stmt.executeQuery("SELECT 1");
    		
    		ResultSetMetaData rsmd = this.rs.getMetaData();
    		
    		if (versionMeetsMinimum(4, 1)) {
    			assertTrue(rsmd.isReadOnly(1));
    			
    			try {
    				this.stmt.executeUpdate("DROP TABLE IF EXISTS testRSMDIsReadOnly");
    				this.stmt.executeUpdate("CREATE TABLE testRSMDIsReadOnly (field1 INT)");
    				this.stmt.executeUpdate("INSERT INTO testRSMDIsReadOnly VALUES (1)");
    				
    				this.rs = this.stmt.executeQuery("SELECT 1, field1 + 1, field1 FROM testRSMDIsReadOnly");
    				rsmd = this.rs.getMetaData();
    				
    				assertTrue(rsmd.isReadOnly(1));
    				assertTrue(rsmd.isReadOnly(2));
    				assertTrue(!rsmd.isReadOnly(3));
    			} finally {
    				this.stmt.executeUpdate("DROP TABLE IF EXISTS testRSMDIsReadOnly");
    			}
    		} else {
    			assertTrue(rsmd.isReadOnly(1) == false);
    		}
    	} finally {
    		if (this.rs != null) {
    			this.rs.close();
    		}
    	}
    }
    
    public void testSupportsSelectForUpdate() throws Exception {
    	boolean supportsForUpdate = this.conn.getMetaData().supportsSelectForUpdate();
    	
    	if (this.versionMeetsMinimum(4, 0)) {
    		assertTrue(supportsForUpdate);
    	} else {
    		assertTrue(!supportsForUpdate);
    	}
    }
}
