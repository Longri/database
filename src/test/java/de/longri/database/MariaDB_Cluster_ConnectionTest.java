package de.longri.database;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class MariaDB_Cluster_ConnectionTest extends JunitDefaults{



    @Test
    void getDatabaseName() {
        assertEquals(databaseName, ClusterConnection.getDatabaseName());
    }

    @Test
    void getType() {
        assertEquals(SQL_TYPE.MySql, ClusterConnection.getType());
    }

    @Test
    void getDatabaseUser() {
        assertEquals(user, ClusterConnection.getDatabaseUser());
    }

    @Test
    void getConnection() throws SQLException, ClassNotFoundException {

        // connections must rotate

        String connectionString1 = "jdbc:mysql://localhost:13306,localhost:23306,localhost:33306/ClusterTestDB";
        String connectionString2 = "jdbc:mysql://localhost:23306,localhost:33306,localhost:13306/ClusterTestDB";
        String connectionString3 = "jdbc:mysql://localhost:33306,localhost:13306,localhost:23306/ClusterTestDB";

        Connection connection = ClusterConnection.getConnection();
        assertInstanceOf(PooledConnection.class, connection);

        assertEquals(connectionString1, connection.getMetaData().getURL());

        connection = ClusterConnection.getConnection();
        assertInstanceOf(PooledConnection.class, connection);
        assertEquals(connectionString3, connection.getMetaData().getURL());

        connection = ClusterConnection.getConnection();
        assertInstanceOf(PooledConnection.class, connection);
        assertEquals(connectionString2, connection.getMetaData().getURL());



    }

}