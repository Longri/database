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

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public abstract class JunitDefaultsTestDB {


    public static enum TEST_DB_LOCATION {
        LOCALHOST, LOCALHOST_CLUSTER, MariaDB, MariaDB_CLUSTER
    }

    static final TEST_DB_LOCATION LOCATION = TEST_DB_LOCATION.LOCALHOST_CLUSTER;


    private final boolean ClusterTest;
    private final boolean LocalHostTest;

    public JunitDefaultsTestDB(String testDB) {
        ClusterTest = LOCATION == TEST_DB_LOCATION.MariaDB_CLUSTER || LOCATION == TEST_DB_LOCATION.LOCALHOST_CLUSTER;
        LocalHostTest = LOCATION == TEST_DB_LOCATION.LOCALHOST_CLUSTER || LOCATION == TEST_DB_LOCATION.LOCALHOST;
        databaseName = testDB;
    }

    private final ArrayList<Host> testHosts = new ArrayList<>();
    private final String databaseName;
    //      String databaseName = "TestDB2";
    Host HOST1 = new Host("localhost", "13306");
    Host HOST2 = new Host("localhost", "23306");
    Host HOST3 = new Host("localhost", "33306");

    Host HOST4 = new Host("10.3.1.200", "3306");
    Host HOST5 = new Host("10.3.1.201", "3306");


    static {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("./gradle.properties.local")) {
            // Laden der Properties-Datei
            properties.load(input);

            // Setzen aller geladenen Properties als System-Properties
            properties.forEach((key, value) -> {
                System.setProperty((String) key, (String) value);
            });

            System.out.println("Properties wurden erfolgreich geladen und gesetzt.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private DatabaseUser localUser = new SimpleDatabaseUser("admin", "admin-pw");
    private DatabaseUser serverUser = new SimpleDatabaseUser(System.getProperty("ServerUserName"), System.getProperty("ServerUserPW"));
    private DatabaseConnection ClusterConnection;

    void instanceSetUp() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException {
        if (LocalHostTest) {
            testHosts.add(HOST1);
            testHosts.add(HOST2);
            testHosts.add(HOST3);
        } else {
            testHosts.add(HOST4);
            testHosts.add(HOST5);
        }


        //before create a connection create the Database on Cluster
        createTestDB(databaseName);

        if (ClusterTest) {
            if (LocalHostTest) {
                ClusterConnection = new MariaDB_Cluster_Connection(databaseName, localUser, testHosts);
            } else {
                ClusterConnection = new MariaDB_Cluster_Connection(databaseName, serverUser, testHosts);
            }
        } else {
            if (LocalHostTest) {
                ClusterConnection = new MySqlConnection(HOST1.host, HOST1.port, databaseName, localUser);
            } else {
                ClusterConnection = new MySqlConnection(HOST4.host, HOST4.port, databaseName, serverUser);
            }
        }


//

    }


    void instanceTearDown() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException {
        deleteTestDB(databaseName);
    }

    public void createTestDB(String dataBaseName) throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException, InterruptedException {
        String hostString;
        if (LocalHostTest) {
            hostString = HOST1.toString();
        } else {
            hostString = HOST4.toString();
        }


        if (dBExists(dataBaseName)) {
            deleteTestDB(dataBaseName);
        }
        String connectionString = "jdbc:mysql://" + hostString + "/?";
        Connection sqlConnection = DriverManager.getConnection(connectionString, getProperties());
        sqlConnection.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + dataBaseName + ";");

        // change connection to created DB
        connectionString = "jdbc:mysql://" + hostString + "/" + dataBaseName;
        sqlConnection = DriverManager.getConnection(connectionString, getProperties());

        String[] commands = getMySqlDump(dataBaseName).split(";\n");

        Statement statement = sqlConnection.createStatement();

        for (String s : commands) {
            s = s.trim();
            if (s.isEmpty()) continue;
            if (s.startsWith("INSERT INTO `SCHEME`")) {
                s = s.replace("INSERT INTO `SCHEME`", "INSERT IGNORE INTO `SCHEME`");
            }
            statement.execute(s);

            Thread.sleep(50);
        }

        statement.close();
        sqlConnection.setAutoCommit(true);

        sqlConnection.close();

    }

    private Properties getProperties() throws GeneralSecurityException, IOException {
        Properties properties = new Properties();

        if (LocalHostTest) {
            properties.setProperty("user", localUser.getUserName());
            properties.setProperty("password", localUser.getUserPasswordDecrypted());
        } else {
            properties.setProperty("user", serverUser.getUserName());
            properties.setProperty("password", serverUser.getUserPasswordDecrypted());
        }
        properties.setProperty("MaxPooledStatements", "250");
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf-8");
        return properties;
    }

    private String getMySqlDump(String databaseName) throws SQLException {

        String fileName = "testDump.sql";

        try {
            // Laden Sie die Ressource als InputStream
            InputStream inputStream = JunitDefaultsTestDB.class.getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                throw new IOException("Datei nicht gefunden: " + fileName);
            }

            // Verwenden Sie IOUtils, um den Inhalt der Datei in einen String zu laden
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
// Schließen Sie den InputStream
            inputStream.close();
            return content;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteTestDB(String dataBaseName) throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException {

        Abstract_Database db;
        if (LocalHostTest) {
            db = new Abstract_Database(new MySqlConnection(HOST1.host, HOST1.port, dataBaseName, localUser)) {
                @Override
                public String getString(String uniqueID, String s) {
                    return null;
                }

                @Override
                public int getLastDatabaseSchemeVersion() {
                    return 0;
                }

                @Override
                public int updateSchemeVersion(int i, int... ints) throws SQLException {
                    return 0;
                }

                @Override
                public String getSqlDump() throws SQLException {
                    return null;
                }

                @Override
                public String getMySqlDump() throws SQLException {
                    return null;
                }
            };
        } else {
            db = new Abstract_Database(new MySqlConnection(HOST4.host, HOST4.port, dataBaseName, serverUser)) {
                @Override
                public String getString(String uniqueID, String s) {
                    return null;
                }

                @Override
                public int getLastDatabaseSchemeVersion() {
                    return 0;
                }

                @Override
                public int updateSchemeVersion(int i, int... ints) throws SQLException {
                    return 0;
                }

                @Override
                public String getSqlDump() throws SQLException {
                    return null;
                }

                @Override
                public String getMySqlDump() throws SQLException {
                    return null;
                }
            };
        }


        String sql = "DROP SCHEMA `" + dataBaseName + "`;";
        db.connect("UNIQUE_ID_DELETE_DB");
        db.createStatement().execute(sql);
        db.disconnect("UNIQUE_ID_DELETE_DB");
    }

    public boolean dBExists(String dbName) throws ClassNotFoundException {

        try {

            Connection conn = getNativeConnection(dbName);

            ResultSet resultSet = conn.getMetaData().getCatalogs();
            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                if (databaseName.equals(dbName)) {
                    return true;
                }
            }
            resultSet.close();
        } catch (Exception ignore) {

        }
        return false;
    }

    private Connection getNativeConnection(String dbName) throws ClassNotFoundException, GeneralSecurityException, IOException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String hostString;
        if (LocalHostTest) {
            hostString = HOST1.toString();
        } else {
            hostString = HOST4.toString();
        }

        String connectionstring = "jdbc:mysql://" + hostString + "/" + dbName;

        Properties properties = new Properties();
        if (LocalHostTest) {
            properties.setProperty("user", localUser.getUserName());
            properties.setProperty("password", localUser.getUserPasswordDecrypted());
        } else {
            properties.setProperty("user", serverUser.getUserName());
            properties.setProperty("password", serverUser.getUserPasswordDecrypted());
        }

        properties.setProperty("MaxPooledStatements", "250");
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf-8");


        Connection conn = DriverManager.getConnection(connectionstring, properties);
        return conn;
    }

    protected DatabaseConnection getConnection() {
        return ClusterConnection;
    }

    protected String getDatabaseName() {
        return databaseName;
    }

    protected DatabaseUser getDatabaseUser() {
        if (LocalHostTest) {
            return localUser;
        }else{
            return serverUser;
        }

    }
}
