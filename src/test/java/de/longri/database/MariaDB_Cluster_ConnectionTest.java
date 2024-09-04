package de.longri.database;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class MariaDB_Cluster_ConnectionTest extends JunitDefaultsTestDB {

    static MariaDB_Cluster_ConnectionTest INSTANCE = new MariaDB_Cluster_ConnectionTest();

    @BeforeAll
    static void setUp() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException {
        INSTANCE.instanceSetUp();
    }

    @AfterAll
    static void tearDown() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException {
        INSTANCE.instanceTearDown();
    }


    MariaDB_Cluster_ConnectionTest() {
        super("ClusterTestDB");
    }


    @Test
    void getDatabaseNameTest() {
        assertEquals(INSTANCE.getDatabaseName(), INSTANCE.getConnection().getDatabaseName());
    }

    @Test
    void getTypeTest() {
        assertEquals(SQL_TYPE.MySql, INSTANCE.getConnection().getType());
    }

    @Test
    void getDatabaseUserTest() {
        assertEquals(INSTANCE.getDatabaseUser(), INSTANCE.getConnection().getDatabaseUser());
    }

    @Test
    void getConnectionTest() throws SQLException, ClassNotFoundException {

        // connections must rotate

        String connectionString1 = "jdbc:mysql://localhost:13306,localhost:23306,localhost:33306/ClusterTestDB";
        String connectionString2 = "jdbc:mysql://localhost:23306,localhost:33306,localhost:13306/ClusterTestDB";
        String connectionString3 = "jdbc:mysql://localhost:33306,localhost:13306,localhost:23306/ClusterTestDB";

        Connection connection = INSTANCE.getConnection().getConnection();
        assertInstanceOf(PooledConnection.class, connection);

        assertEquals(connectionString2, connection.getMetaData().getURL());

        connection = INSTANCE.getConnection().getConnection();
        assertInstanceOf(PooledConnection.class, connection);
        assertEquals(connectionString3, connection.getMetaData().getURL());

        connection = INSTANCE.getConnection().getConnection();
        assertInstanceOf(PooledConnection.class, connection);
        assertEquals(connectionString1, connection.getMetaData().getURL());


    }

}