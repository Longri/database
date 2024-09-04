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


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Abstract_Database implements Database {
    final DatabaseConnection connection;

    public Abstract_Database(DatabaseConnection c) {
        this.connection = c;
    }

    public abstract int getLastDatabaseSchemeVersion();

    public abstract int updateSchemeVersion(int oldDatabaseSchemeVersion, int... newDatabaseSchemeVersion) throws SQLException;

    public void createNewDatabase() throws SQLException {

        if (!connection.databaseExist()) {
            connection.createDatabase();
        }


        updateSchemeVersion(0);
    }

    @Override
    public void connect(String uniqueID) throws SQLException, ClassNotFoundException {
        connection.connect(uniqueID);
    }

    @Override
    public void disconnect(String uniqueID) {
        try {
            connection.disconnect(uniqueID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void connect(String uniqueID, boolean fireEvent) throws SQLException, ClassNotFoundException {
        connection.connect(uniqueID, fireEvent);
    }


    public void disconnect(String uniqueID, boolean fireEvent) {
        try {
            connection.disconnect(uniqueID, fireEvent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int executeUpdate(String uniqueID, String sql) throws SQLException {
        try {
            this.connect(uniqueID);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        Statement stmt = connection.createStatement();
        int changes = stmt.executeUpdate(sql);
        stmt.close();
        this.disconnect(uniqueID);
        return changes;
    }

    public abstract String getSqlDump() throws SQLException;

    public abstract String getMySqlDump() throws SQLException;

    @Override
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    @Override
    public boolean tableExist(String uniqueID, String tblName) {
        try {
            return connection.tableExist(uniqueID, tblName);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public SQL_TYPE getType() {
        return connection.getType();
    }

    public DatabaseConnection getConnection() {
        return connection;
    }

    private final String UNIQUE_ID_SET_VERSION = "Abstract_Database.setVersion()";

    protected void setVersion(int version) throws SQLException {
        try {
            connect(UNIQUE_ID_SET_VERSION);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Statement stmt = connection.createStatement();

        //first get the exist id
        String sql = "SELECT id FROM SCHEME";
        ResultSet rs = stmt.executeQuery(sql);
        int id = 0;
        while (rs.next()) {
            id = rs.getInt(1);
        }

        sql = "REPLACE INTO SCHEME (id, version) VALUES('" + id + "', " + Integer.toString(version) + ");";

        stmt.executeUpdate(sql);
        stmt.close();
        disconnect(UNIQUE_ID_SET_VERSION);
    }

    private final String UNIQUE_ID_GET_DATABASE_VERSION = "Abstract_Database.getDatabaseSchemeVersion()";

    public int getDatabaseSchemeVersion() throws SQLException, ClassNotFoundException {
        String sql = "SELECT version FROM SCHEME;";
        Statement stmt = null;
        connect(UNIQUE_ID_GET_DATABASE_VERSION);
        try {
            stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(sql);

            if (result.next()) {
                int exS = result.getInt(1);
                return exS;
            }
            return -3;

        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        } finally {
            try {
                stmt.close();
                this.disconnect(UNIQUE_ID_GET_DATABASE_VERSION);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addConnectionListener(DatabaseConnection.ConnectionListener connectionListener) {
        connection.addConnectionListener(connectionListener);
    }

    public void removeConnectionListener(DatabaseConnection.ConnectionListener connectionListener) {
        connection.removeConnectionListener(connectionListener);
    }


    public String getAutoIncrementString() {
        return connection.getAutoIncrementString();
    }

    public DatabaseUser getDatabaseUser() {
        return connection.getDatabaseUser();
    }

    public void setUser(DatabaseUser user) {
        connection.setDatabaseUser(user);
    }

    public String getString(String uniqueID, String sql) {
        try {
            connection.connect(uniqueID);
            ResultSet rst = createStatement().executeQuery(sql);
            rst.next();
            return rst.getString(1);
        } catch (SQLException | ClassNotFoundException e) {
            return null;
        } finally {
            try {
                connection.disconnect(uniqueID);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime getDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty() || dateTime.equalsIgnoreCase("NULL")) {
            return null;
        }
        return LocalDateTime.parse(dateTime, SQL_DATE_TIME_FORMATTER);
    }

    public static String getDateString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "NULL";
        }
        return SQL_DATE_TIME_FORMATTER.format(dateTime);
    }


}
