/*
 * Copyright (C) 2024 Longri
 *
 * This file is part of database.
 *
 * database is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * database is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with fxutils. If not, see <https://www.gnu.org/licenses/>.
 */
package de.longri.database;

import de.longri.database.cache.TableReadSource;
import de.longri.database.properties.*;
import de.longri.filetransfer.Local_FileTransferHandle;
import de.longri.serializable.NotImplementedException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MariaDBConnectionTest extends JunitDefaultsTestDB {

    static MariaDBConnectionTest INSTANCE = new MariaDBConnectionTest();

    @BeforeAll
    static void setUp() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException {
        INSTANCE.instanceSetUp();
    }

    @AfterAll
    static void tearDown() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException {
        Thread.sleep(1000);
//        INSTANCE.instanceTearDown();
    }


    MariaDBConnectionTest() {
        super(false, false,"TestDB");
    }


    @Test
    void test() throws GeneralSecurityException, UnsupportedEncodingException, SQLException, ClassNotFoundException {
        assertNotNull(INSTANCE.getConnection());
        INSTANCE.getConnection().createDatabase();
        assertTrue(INSTANCE.getConnection().databaseExist());

    }


    @Test
    void clusterTableDataTest() throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException, NotImplementedException {

        String cachePath = "./TEST/Cache";

        //delete cache before test
        Local_FileTransferHandle cache = new Local_FileTransferHandle(cachePath);
        cache.deleteDirectory();

        assertFalse(cache.exists());

        TableDataTestCache CACHE = new TableDataTestCache(cachePath);

        DatabaseConnection con=INSTANCE.getConnection();

        CACHE.loadCache(con);

        assertEquals(CACHE.getTable("Table1"), CACHE.table1);
        assertEquals(CACHE.getTable("Table2"), CACHE.table2);
        assertEquals(CACHE.getTable("Table3"), CACHE.table3);
        assertEquals(CACHE.getTable("Table4"), CACHE.table4);
        assertEquals(CACHE.getTable("Table5"), CACHE.table5);


        //--------------------------Table 1 test
        assertEquals(3, CACHE.table1.tableData.size());
        TableDataTestCache.Table1_data data = CACHE.table1.tableData.get(0);
        AbstractProperty prop;
        prop = data.get("name");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Item1", prop.getValue());
        prop = data.get("value");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(100, prop.getValue());

        data = CACHE.table1.tableData.get(1);
        prop = data.get("name");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Item2", prop.getValue());
        prop = data.get("value");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(200, prop.getValue());

        data = CACHE.table1.tableData.get(2);
        prop = data.get("name");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Item3", prop.getValue());
        prop = data.get("value");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(300, prop.getValue());

        //--------------------------Table 2 test
        assertEquals(3, CACHE.table2.tableData.size());
        TableDataTestCache.Table2_data data1 = CACHE.table2.tableData.get(0);
        prop = data1.get("description");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Description1", prop.getValue());
        prop = data1.get("is_active");
        assertInstanceOf(NamedBoolProperty.class, prop);
        assertEquals(true, prop.getValue());

        data1 = CACHE.table2.tableData.get(1);
        prop = data1.get("description");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Description2", prop.getValue());
        prop = data1.get("is_active");
        assertInstanceOf(NamedBoolProperty.class, prop);
        assertEquals(false, prop.getValue());

        data1 = CACHE.table2.tableData.get(2);
        prop = data1.get("description");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Description3", prop.getValue());
        prop = data1.get("is_active");
        assertInstanceOf(NamedBoolProperty.class, prop);
        assertEquals(true, prop.getValue());

        //--------------------------Table 3 test
        assertEquals(3, CACHE.table3.tableData.size());
        TableDataTestCache.Table3_data data2 = CACHE.table3.tableData.get(0);
        prop = data2.get("user_id");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(1, prop.getValue());
        prop = data2.get("amount");
        assertInstanceOf(NamedDoubleProperty.class, prop);
        assertEquals(99.99, prop.getValue());

        data2 = CACHE.table3.tableData.get(1);
        prop = data2.get("user_id");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(2, prop.getValue());
        prop = data2.get("amount");
        assertInstanceOf(NamedDoubleProperty.class, prop);
        assertEquals(150.75, prop.getValue());

        data2 = CACHE.table3.tableData.get(2);
        prop = data2.get("user_id");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(3, prop.getValue());
        prop = data2.get("amount");
        assertInstanceOf(NamedDoubleProperty.class, prop);
        assertEquals(250.0, prop.getValue());

        //--------------------------Table 4 test

        LocalDateTime NOW_TRUNCATED = getDockerTimestamp(con.getConnection());

        assertEquals(3, CACHE.table4.tableData.size());
        TableDataTestCache.Table4_data data4 = CACHE.table4.tableData.get(0);
        prop = data4.get("timestamp");
        assertInstanceOf(NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.getValue(), 1);
        prop = data4.get("status");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Pending", prop.getValue());

        data4 = CACHE.table4.tableData.get(1);
        prop = data4.get("timestamp");
        assertInstanceOf(NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.getValue(), 1);
        prop = data4.get("status");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Completed", prop.getValue());

        data4 = CACHE.table4.tableData.get(2);
        prop = data4.get("timestamp");
        assertInstanceOf(NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.getValue(), 1);
        prop = data4.get("status");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Failed", prop.getValue());


        //--------------------------Table 5 test
        assertEquals(3, CACHE.table5.tableData.size());
        TableDataTestCache.Table5_data data5 = CACHE.table5.tableData.get(0);
        prop = data5.get("title");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Title1", prop.getValue());
        prop = data5.get("content");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Content1", prop.getValue());

        data5 = CACHE.table5.tableData.get(1);
        prop = data5.get("title");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Title2", prop.getValue());
        prop = data5.get("content");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Content2", prop.getValue());

        data5 = CACHE.table5.tableData.get(2);
        prop = data5.get("title");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Title3", prop.getValue());
        prop = data5.get("content");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Content3", prop.getValue());

        //check all tables are loaded from DB
        assertEquals(TableReadSource.DB, CACHE.table1.SOURCE);
        assertEquals(TableReadSource.DB, CACHE.table2.SOURCE);
        assertEquals(TableReadSource.DB, CACHE.table3.SOURCE);
        assertEquals(TableReadSource.DB, CACHE.table4.SOURCE);
        assertEquals(TableReadSource.DB, CACHE.table5.SOURCE);

        //  #######     load from Disk    ################################################################
        CACHE.loadCache(INSTANCE.getConnection());

        assertEquals(CACHE.getTable("Table1"), CACHE.table1);
        assertEquals(CACHE.getTable("Table2"), CACHE.table2);
        assertEquals(CACHE.getTable("Table3"), CACHE.table3);
        assertEquals(CACHE.getTable("Table4"), CACHE.table4);
        assertEquals(CACHE.getTable("Table5"), CACHE.table5);


        //--------------------------Table 1 test
        assertEquals(3, CACHE.table1.tableData.size());
        data = CACHE.table1.tableData.get(0);
        prop = data.get("name");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Item1", prop.getValue());
        prop = data.get("value");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(100, prop.getValue());

        data = CACHE.table1.tableData.get(1);
        prop = data.get("name");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Item2", prop.getValue());
        prop = data.get("value");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(200, prop.getValue());

        data = CACHE.table1.tableData.get(2);
        prop = data.get("name");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Item3", prop.getValue());
        prop = data.get("value");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(300, prop.getValue());

        //--------------------------Table 2 test
        assertEquals(3, CACHE.table2.tableData.size());
        data1 = CACHE.table2.tableData.get(0);
        prop = data1.get("description");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Description1", prop.getValue());
        prop = data1.get("is_active");
        assertInstanceOf(NamedBoolProperty.class, prop);
        assertEquals(true, prop.getValue());

        data1 = CACHE.table2.tableData.get(1);
        prop = data1.get("description");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Description2", prop.getValue());
        prop = data1.get("is_active");
        assertInstanceOf(NamedBoolProperty.class, prop);
        assertEquals(false, prop.getValue());

        data1 = CACHE.table2.tableData.get(2);
        prop = data1.get("description");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Description3", prop.getValue());
        prop = data1.get("is_active");
        assertInstanceOf(NamedBoolProperty.class, prop);
        assertEquals(true, prop.getValue());

        //--------------------------Table 3 test
        assertEquals(3, CACHE.table3.tableData.size());
        data2 = CACHE.table3.tableData.get(0);
        prop = data2.get("user_id");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(1, prop.getValue());
        prop = data2.get("amount");
        assertInstanceOf(NamedDoubleProperty.class, prop);
        assertEquals(99.99, prop.getValue());

        data2 = CACHE.table3.tableData.get(1);
        prop = data2.get("user_id");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(2, prop.getValue());
        prop = data2.get("amount");
        assertInstanceOf(NamedDoubleProperty.class, prop);
        assertEquals(150.75, prop.getValue());

        data2 = CACHE.table3.tableData.get(2);
        prop = data2.get("user_id");
        assertInstanceOf(NamedIntegerProperty.class, prop);
        assertEquals(3, prop.getValue());
        prop = data2.get("amount");
        assertInstanceOf(NamedDoubleProperty.class, prop);
        assertEquals(250.0, prop.getValue());

        //--------------------------Table 4 test

        NOW_TRUNCATED = getDockerTimestamp(INSTANCE.getConnection().getConnection());

        assertEquals(3, CACHE.table4.tableData.size());
        data4 = CACHE.table4.tableData.get(0);
        prop = data4.get("timestamp");
        assertInstanceOf(NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.getValue(), 1);
        prop = data4.get("status");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Pending", prop.getValue());

        data4 = CACHE.table4.tableData.get(1);
        prop = data4.get("timestamp");
        assertInstanceOf(NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.getValue(), 1);
        prop = data4.get("status");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Completed", prop.getValue());

        data4 = CACHE.table4.tableData.get(2);
        prop = data4.get("timestamp");
        assertInstanceOf(NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.getValue(), 1);
        prop = data4.get("status");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Failed", prop.getValue());


        //--------------------------Table 5 test
        assertEquals(3, CACHE.table5.tableData.size());
        data5 = CACHE.table5.tableData.get(0);
        prop = data5.get("title");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Title1", prop.getValue());
        prop = data5.get("content");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Content1", prop.getValue());

        data5 = CACHE.table5.tableData.get(1);
        prop = data5.get("title");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Title2", prop.getValue());
        prop = data5.get("content");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Content2", prop.getValue());

        data5 = CACHE.table5.tableData.get(2);
        prop = data5.get("title");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Title3", prop.getValue());
        prop = data5.get("content");
        assertInstanceOf(NamedStringProperty.class, prop);
        assertEquals("Content3", prop.getValue());

        //check all tables are loaded from DB
        assertEquals(TableReadSource.Disk, CACHE.table1.SOURCE);
        assertEquals(TableReadSource.Disk, CACHE.table2.SOURCE);
        assertEquals(TableReadSource.Disk, CACHE.table3.SOURCE);
        assertEquals(TableReadSource.Disk, CACHE.table4.SOURCE);
        assertEquals(TableReadSource.Disk, CACHE.table5.SOURCE);

    }


    void assertLocalDate(LocalDateTime expectedDate, LocalDateTime actualDate, int precisionSeconds) {
        assertEquals(expectedDate.getYear(), actualDate.getYear());
        assertEquals(expectedDate.getMonth(), actualDate.getMonth());
        assertEquals(expectedDate.getDayOfMonth(), actualDate.getDayOfMonth());
        assertEquals(expectedDate.getHour(), actualDate.getHour());
        assertEquals(expectedDate.getMinute(), actualDate.getMinute());
        assertEquals(expectedDate.getSecond(), actualDate.getSecond(), precisionSeconds);
    }

    LocalDateTime getDockerTimestamp(Connection connection) {
        Statement statement = null;
        ResultSet resultSet = null;

        try {

            // Statement erstellen
            statement = connection.createStatement();

            // SQL-Abfrage zur Abfrage der Serverzeit
            String sql = "SELECT NOW()";

            // Abfrage ausführen
            resultSet = statement.executeQuery(sql);

            // Ergebnis verarbeiten
            if (resultSet.next()) {
                String serverTime = resultSet.getString(1);
                System.out.println("Aktuelle Serverzeit: " + serverTime);
                return Abstract_Database.getDateTime(serverTime);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Can't read mariaDB ServerTime");
        } finally {
            // Ressourcen freigeben
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Can't read mariaDB ServerTime");
    }

}