package de.longri.database;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MariaDBConnectionTest {

    @Test
    void test() throws GeneralSecurityException, UnsupportedEncodingException, SQLException, ClassNotFoundException {
        String address = "localhost";
        String port = "33306";
        String databaseName = "TestDB";
        DatabaseUser user = new SimpleDatabaseUser("admin", "admin-pw");

        MariaDBConnection connection = new MariaDBConnection(address, port, databaseName, user);

        assertNotNull(connection);
        connection.createDatabase();
        assertTrue(connection.databaseExist());

    }

    @Test
    void testCluster() throws GeneralSecurityException, UnsupportedEncodingException, SQLException, ClassNotFoundException {
        String[] address = new String[]{"localhost","localhost"};
        String[] port =new String[]{"13306","23306"};
        String databaseName = "TestDB2";
        DatabaseUser user = new SimpleDatabaseUser("admin", "admin-pw");

        MariaDBConnection connection = new MariaDBConnection(address, port, databaseName, user);

        assertNotNull(connection);
        connection.createDatabase();
        assertTrue(connection.databaseExist());



    }

}