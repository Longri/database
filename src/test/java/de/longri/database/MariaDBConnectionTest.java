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

import de.longri.database.table_data.AbstractTable;
import de.longri.serializable.NotImplementedException;
import de.longri.utils.NamedObjectProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        INSTANCE.instanceTearDown();
    }


    MariaDBConnectionTest() {
        super("TestDB");
    }


    @Test
    void test() throws GeneralSecurityException, UnsupportedEncodingException, SQLException, ClassNotFoundException {
        assertNotNull(INSTANCE.getConnection());
        INSTANCE.getConnection().createDatabase();
        assertTrue(INSTANCE.getConnection().databaseExist());

    }


    @Test
    void clusterTableDataTest() throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException, NotImplementedException {

        TableDataTestCache CACHE = new TableDataTestCache("./TEST/Cache");
        CACHE.loadCache(INSTANCE.getConnection());

        assertEquals(CACHE.getTable("Table1"), CACHE.table1);
        assertEquals(CACHE.getTable("Table2"), CACHE.table2);
        assertEquals(CACHE.getTable("Table3"), CACHE.table3);
        assertEquals(CACHE.getTable("Table4"), CACHE.table4);
        assertEquals(CACHE.getTable("Table5"), CACHE.table5);


        //--------------------------Table 1 test
        assertEquals(3, CACHE.table1.tableData.size());
        TableDataTestCache.Table1_data data = CACHE.table1.tableData.get(0);
        NamedObjectProperty prop = data.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data.getProperty("name");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Item1", prop.get());
        prop = data.getProperty("value");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(100, prop.get());

        data = CACHE.table1.tableData.get(1);
        prop = data.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data.getProperty("name");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Item2", prop.get());
        prop = data.getProperty("value");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(200, prop.get());

        data = CACHE.table1.tableData.get(2);
        prop = data.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data.getProperty("name");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Item3", prop.get());
        prop = data.getProperty("value");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(300, prop.get());

        //--------------------------Table 2 test
        assertEquals(3, CACHE.table2.tableData.size());
        TableDataTestCache.Table2_data data1 = CACHE.table2.tableData.get(0);
        prop = data1.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data1.getProperty("description");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Description1", prop.get());
        prop = data1.getProperty("is_active");
        assertInstanceOf(NamedObjectProperty.NamedBoolProperty.class, prop);
        assertEquals(true, prop.get());

        data1 = CACHE.table2.tableData.get(1);
        prop = data1.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data1.getProperty("description");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Description2", prop.get());
        prop = data1.getProperty("is_active");
        assertInstanceOf(NamedObjectProperty.NamedBoolProperty.class, prop);
        assertEquals(false, prop.get());

        data1 = CACHE.table2.tableData.get(2);
        prop = data1.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data1.getProperty("description");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Description3", prop.get());
        prop = data1.getProperty("is_active");
        assertInstanceOf(NamedObjectProperty.NamedBoolProperty.class, prop);
        assertEquals(true, prop.get());

        //--------------------------Table 3 test
        assertEquals(3, CACHE.table3.tableData.size());
        TableDataTestCache.Table3_data data2 = CACHE.table3.tableData.get(0);
        prop = data2.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data2.getProperty("user_id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(1, prop.get());
        prop = data2.getProperty("amount");
        assertInstanceOf(NamedObjectProperty.NamedDoubleProperty.class, prop);
        assertEquals(99.99, prop.get());

        data2 = CACHE.table3.tableData.get(1);
        prop = data2.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data2.getProperty("user_id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(2, prop.get());
        prop = data2.getProperty("amount");
        assertInstanceOf(NamedObjectProperty.NamedDoubleProperty.class, prop);
        assertEquals(150.75, prop.get());

        data2 = CACHE.table3.tableData.get(2);
        prop = data2.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data2.getProperty("user_id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(3, prop.get());
        prop = data2.getProperty("amount");
        assertInstanceOf(NamedObjectProperty.NamedDoubleProperty.class, prop);
        assertEquals(250.0, prop.get());

        //--------------------------Table 4 test

        LocalDateTime NOW_TRUNCATED = getDockerTimestamp(INSTANCE.getConnection().getConnection());

        assertEquals(3, CACHE.table4.tableData.size());
        TableDataTestCache.Table4_data data4 = CACHE.table4.tableData.get(0);
        prop = data4.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data4.getProperty("timestamp");
        assertInstanceOf(NamedObjectProperty.NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.get(), 1);
        prop = data4.getProperty("status");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Pending", prop.get());

        data4 = CACHE.table4.tableData.get(1);
        prop = data4.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data4.getProperty("timestamp");
        assertInstanceOf(NamedObjectProperty.NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.get(), 1);
        prop = data4.getProperty("status");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Completed", prop.get());

        data4 = CACHE.table4.tableData.get(2);
        prop = data4.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data4.getProperty("timestamp");
        assertInstanceOf(NamedObjectProperty.NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.get(), 1);
        prop = data4.getProperty("status");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Failed", prop.get());


        //--------------------------Table 5 test
        assertEquals(3, CACHE.table5.tableData.size());
        TableDataTestCache.Table5_data data5 = CACHE.table5.tableData.get(0);
        prop = data5.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data5.getProperty("title");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Title1", prop.get());
        prop = data5.getProperty("content");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Content1", prop.get());

        data5 = CACHE.table5.tableData.get(1);
        prop = data5.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data5.getProperty("title");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Title2", prop.get());
        prop = data5.getProperty("content");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Content2", prop.get());

        data5 = CACHE.table5.tableData.get(2);
        prop = data5.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data5.getProperty("title");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Title3", prop.get());
        prop = data5.getProperty("content");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Content3", prop.get());

        //check all tables are loaded from DB
        assertEquals(AbstractTable.Source.DB, CACHE.table1.SOURCE);
        assertEquals(AbstractTable.Source.DB, CACHE.table2.SOURCE);
        assertEquals(AbstractTable.Source.DB, CACHE.table3.SOURCE);
        assertEquals(AbstractTable.Source.DB, CACHE.table4.SOURCE);
        assertEquals(AbstractTable.Source.DB, CACHE.table5.SOURCE);

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
        prop = data.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data.getProperty("name");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Item1", prop.get());
        prop = data.getProperty("value");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(100, prop.get());

        data = CACHE.table1.tableData.get(1);
        prop = data.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data.getProperty("name");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Item2", prop.get());
        prop = data.getProperty("value");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(200, prop.get());

        data = CACHE.table1.tableData.get(2);
        prop = data.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data.getProperty("name");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Item3", prop.get());
        prop = data.getProperty("value");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(300, prop.get());

        //--------------------------Table 2 test
        assertEquals(3, CACHE.table2.tableData.size());
        data1 = CACHE.table2.tableData.get(0);
        prop = data1.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data1.getProperty("description");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Description1", prop.get());
        prop = data1.getProperty("is_active");
        assertInstanceOf(NamedObjectProperty.NamedBoolProperty.class, prop);
        assertEquals(true, prop.get());

        data1 = CACHE.table2.tableData.get(1);
        prop = data1.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data1.getProperty("description");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Description2", prop.get());
        prop = data1.getProperty("is_active");
        assertInstanceOf(NamedObjectProperty.NamedBoolProperty.class, prop);
        assertEquals(false, prop.get());

        data1 = CACHE.table2.tableData.get(2);
        prop = data1.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data1.getProperty("description");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Description3", prop.get());
        prop = data1.getProperty("is_active");
        assertInstanceOf(NamedObjectProperty.NamedBoolProperty.class, prop);
        assertEquals(true, prop.get());

        //--------------------------Table 3 test
        assertEquals(3, CACHE.table3.tableData.size());
        data2 = CACHE.table3.tableData.get(0);
        prop = data2.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data2.getProperty("user_id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(1, prop.get());
        prop = data2.getProperty("amount");
        assertInstanceOf(NamedObjectProperty.NamedDoubleProperty.class, prop);
        assertEquals(99.99, prop.get());

        data2 = CACHE.table3.tableData.get(1);
        prop = data2.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data2.getProperty("user_id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(2, prop.get());
        prop = data2.getProperty("amount");
        assertInstanceOf(NamedObjectProperty.NamedDoubleProperty.class, prop);
        assertEquals(150.75, prop.get());

        data2 = CACHE.table3.tableData.get(2);
        prop = data2.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data2.getProperty("user_id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(3, prop.get());
        prop = data2.getProperty("amount");
        assertInstanceOf(NamedObjectProperty.NamedDoubleProperty.class, prop);
        assertEquals(250.0, prop.get());

        //--------------------------Table 4 test

        NOW_TRUNCATED = getDockerTimestamp(INSTANCE.getConnection().getConnection());

        assertEquals(3, CACHE.table4.tableData.size());
        data4 = CACHE.table4.tableData.get(0);
        prop = data4.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data4.getProperty("timestamp");
        assertInstanceOf(NamedObjectProperty.NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.get(), 1);
        prop = data4.getProperty("status");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Pending", prop.get());

        data4 = CACHE.table4.tableData.get(1);
        prop = data4.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data4.getProperty("timestamp");
        assertInstanceOf(NamedObjectProperty.NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.get(), 1);
        prop = data4.getProperty("status");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Completed", prop.get());

        data4 = CACHE.table4.tableData.get(2);
        prop = data4.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data4.getProperty("timestamp");
        assertInstanceOf(NamedObjectProperty.NamedLocalDateTimeProperty.class, prop);
        assertLocalDate(NOW_TRUNCATED, (LocalDateTime) prop.get(), 1);
        prop = data4.getProperty("status");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Failed", prop.get());


        //--------------------------Table 5 test
        assertEquals(3, CACHE.table5.tableData.size());
        data5 = CACHE.table5.tableData.get(0);
        prop = data5.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(4, prop.get());
        prop = data5.getProperty("title");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Title1", prop.get());
        prop = data5.getProperty("content");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Content1", prop.get());

        data5 = CACHE.table5.tableData.get(1);
        prop = data5.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(7, prop.get());
        prop = data5.getProperty("title");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Title2", prop.get());
        prop = data5.getProperty("content");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Content2", prop.get());

        data5 = CACHE.table5.tableData.get(2);
        prop = data5.getProperty("id");
        assertInstanceOf(NamedObjectProperty.NamedIntegerProperty.class, prop);
        assertEquals(10, prop.get());
        prop = data5.getProperty("title");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Title3", prop.get());
        prop = data5.getProperty("content");
        assertInstanceOf(NamedObjectProperty.NamedStringProperty.class, prop);
        assertEquals("Content3", prop.get());

        //check all tables are loaded from DB
        assertEquals(AbstractTable.Source.Disk, CACHE.table1.SOURCE);
        assertEquals(AbstractTable.Source.Disk, CACHE.table2.SOURCE);
        assertEquals(AbstractTable.Source.Disk, CACHE.table3.SOURCE);
        assertEquals(AbstractTable.Source.Disk, CACHE.table4.SOURCE);
        assertEquals(AbstractTable.Source.Disk, CACHE.table5.SOURCE);

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