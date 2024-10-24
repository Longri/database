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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public abstract class JunitDefaultsTestDB {

    public JunitDefaultsTestDB(String testDB) {
        databaseName = testDB;
    }

    private final ArrayList<Host> testHosts = new ArrayList<>();
    private final String databaseName;
    //      String databaseName = "TestDB2";
    Host HOST1 = new Host("localhost", "13306");
    Host HOST2 = new Host("localhost", "23306");
    Host HOST3 = new Host("localhost", "33306");


    private DatabaseUser user = new SimpleDatabaseUser("admin", "admin-pw");
    private MariaDB_Cluster_Connection ClusterConnection;

    void instanceSetUp() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException {
        testHosts.add(HOST1);
        testHosts.add(HOST2);
        testHosts.add(HOST3);

        //before create a connection create the Database on Cluster
        createTestDB(databaseName);
        Thread.sleep(500);
        ClusterConnection = new MariaDB_Cluster_Connection(databaseName, user, testHosts);
    }


    void instanceTearDown() throws SQLException, GeneralSecurityException, IOException, ClassNotFoundException {
       //  deleteTestDB(databaseName);
    }

    public void createTestDB(String dataBaseName) throws GeneralSecurityException, IOException, SQLException, ClassNotFoundException, InterruptedException {
        if (dBExists(dataBaseName)) {
            deleteTestDB(dataBaseName);
        }
        String connectionString = "jdbc:mysql://" + HOST1.toString() + "/?";
        Connection sqlConnection = DriverManager.getConnection(connectionString, getProperties());
        sqlConnection.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + dataBaseName + ";");

        // change connection to created DB
        connectionString = "jdbc:mysql://" + HOST1.toString() + "/" + dataBaseName;
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
        properties.setProperty("user", user.getUserName());
        properties.setProperty("password", user.getUserPasswordDecrypted());
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
        Abstract_Database db = new Abstract_Database(new MySqlConnection(HOST1.host, HOST1.port, dataBaseName, user)) {
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


        String connectionstring = "jdbc:mysql://" + HOST1.toString() + "/" + dbName;

        Properties properties = new Properties();
        properties.setProperty("user", user.getUserName());
        properties.setProperty("password", user.getUserPasswordDecrypted());
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
        return user;
    }
}
