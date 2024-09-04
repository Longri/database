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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Properties;

public class MySqlConnection extends DatabaseConnection {

    private final Logger log = LoggerFactory.getLogger(MySqlConnection.class);
    protected final String address;
    protected final String port;
    protected final String databaseName;
    protected final String connectionString;
    protected DatabaseUser user;

    public MySqlConnection(String address, String port, String databaseName, DatabaseUser user) throws GeneralSecurityException, UnsupportedEncodingException {
        super();
        this.address = address;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        connectionString = "jdbc:mysql://" + address + ":" + port + "/" + databaseName;
    }

    @Override
    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try {
            return DriverManager.getConnection(connectionString, getProperties());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean tableExist(String uniqueID, String tblName) throws SQLException {
        String sql = "SHOW TABLES FROM " + databaseName + " LIKE '" + tblName + "';";
        Statement stmt = null;
        try {
            try {
                connect(uniqueID);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            stmt = createStatement();
            ResultSet result = stmt.executeQuery(sql);
            result.next();
            return result.getRow() > 0;
        } finally {
            disconnect(uniqueID);
        }
    }


    protected Properties getProperties() throws GeneralSecurityException, IOException {
        Properties properties = new Properties();
        properties.setProperty("user", user.getUserName());
        properties.setProperty("password", user.getUserPasswordDecrypted());
        properties.setProperty("MaxPooledStatements", "250");
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf-8");
        return properties;
    }

    @Override
    public SQL_TYPE getType() {
        return SQL_TYPE.MySql;
    }

    @Override
    public String getAutoIncrementString() {
        return "AUTO_INCREMENT";
    }

    @Override
    public void importDumbString(String dump) throws SQLException, ClassNotFoundException {
        String[] commands = dump.split(";\n");

        try {
            Statement statement = createStatement();

            connection.setAutoCommit(false); // default true
            SQLException sqle = null;
            String lastCommand = "";
            try {
                for (String s : commands) {
                    s = s.trim();
                    if (s.isEmpty()) continue;
                    if (s.startsWith("INSERT INTO `SCHEME`")) {
                        s = s.replace("INSERT INTO `SCHEME`", "INSERT IGNORE INTO `SCHEME`");
                    }
                    lastCommand = s;
                    statement.execute(s);
                }
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                sqle = e;
            }
            statement.close();
            connection.setAutoCommit(true);
            if (sqle != null) {
                sqle.addSuppressed(new Exception("With command:" + lastCommand));
                throw sqle;
            }

        } catch (Exception ex) {
            throw ex;
        }

    }

    @Override
    public DatabaseUser getDatabaseUser() {
        return this.user;
    }

    @Override
    public void setDatabaseUser(DatabaseUser user) {
        this.user = user;
    }


    private final String UNIQUE_ID = "MySqlConnection.databaseExist()";

    @Override
    public boolean databaseExist() {
        try {
            this.connect(UNIQUE_ID);
        } catch (Exception e) {
            return false;
        }
        try {
            this.disconnect(UNIQUE_ID);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @Override
    public void createDatabase() {
        try {
            MySqlConnection.createNewDatabase(address, port, databaseName, user.getUserName(), user.getUserPasswordDecrypted(), user.getUserName(), user.getUserPasswordDecrypted());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public static void dropDatabase(String address, String port, String databaseName, String admin, String adminPass) throws SQLException {
        String sql = "DROP DATABASE IF EXISTS `" + databaseName + "`;";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Properties properties = new Properties();
            properties.setProperty("user", admin);
            properties.setProperty("password", adminPass);
            properties.setProperty("MaxPooledStatements", "250");

            String connectionstring = "jdbc:mysql://" + address + ":" + port + "/?";

            Connection connection = DriverManager.getConnection(connectionstring, properties);

            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(sql);

            statement.close();
            connection.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void createNewDatabase(String address, String port, String databaseName, String user, String userPass, String admin, String adminPass) throws SQLException {
        //try to create Database, if admin set

        if (admin != null && adminPass != null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                Properties properties = new Properties();
                properties.setProperty("user", admin);
                properties.setProperty("password", adminPass);
                properties.setProperty("MaxPooledStatements", "250");

                String connectionstring = "jdbc:mysql://" + address + ":" + port + "/?";

                Connection connection = DriverManager.getConnection(connectionstring, properties);

                Statement statement = connection.createStatement();

                int result = statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + databaseName + "` CHARACTER SET utf8 COLLATE utf8_general_ci");

                String createUser = "CREATE USER IF NOT EXISTS '" + user + "' IDENTIFIED BY '" + userPass + "';";
                result = statement.executeUpdate(createUser);

                //set rights
                String grant = "GRANT ALL PRIVILEGES ON `" + databaseName + "`.* TO '" + user + "'@'%';";
                result = statement.executeUpdate(grant);

                statement.close();
                connection.close();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "MySqlConnection: " + connectionString;
    }
}
