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

import de.longri.fx.utils.SleepCall;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MariaDB_Cluster_Connection extends DatabaseConnection implements PoolConnectionCreater {


    final String DATABASE_NAME;
    final DatabaseUser DATABASE_USER;
    final ArrayList<Host> HOST_LIST;
    final ConnectionPool pool;


    public MariaDB_Cluster_Connection(String databaseName, DatabaseUser user, ArrayList<Host> hostList) throws SQLException, GeneralSecurityException, IOException {
        DATABASE_NAME = databaseName;
        DATABASE_USER = user;
        HOST_LIST = hostList;
        pool = new ConnectionPool(this);
        new SleepCall(200, () -> {
            try {
                fillPool();
            } catch (SQLException | GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        });


    }

    private void fillPool() throws SQLException, GeneralSecurityException, IOException {
        // fill with three times host list
        for (int i = 0; i < 3; i++) {
            for (Host host : HOST_LIST) {
                pool.push(createNewPooledConnection());
            }
        }
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
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

        Connection connection = getConnection();

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
        return DATABASE_USER;
    }

    @Override
    public void setDatabaseUser(DatabaseUser user) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean databaseExist() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + DATABASE_NAME + "'")) {

            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void createDatabase() {
        try {
            Host address = HOST_LIST.get(0);
            MySqlConnection.createNewDatabase(address.host, address.port, DATABASE_NAME, DATABASE_USER.getUserName(), DATABASE_USER.getUserPasswordDecrypted(), DATABASE_USER.getUserName(), DATABASE_USER.getUserPasswordDecrypted());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        return pool.getConnection();
    }

    private int rotationIndex = 1;

    @Override
    public PooledConnection createNewPooledConnection() throws SQLException, GeneralSecurityException, IOException {

        int hostCount = HOST_LIST.size();
        StringBuilder sb = new StringBuilder("jdbc:mysql://");

        for (int i = 0; i < hostCount; i++) {
            int index = (rotationIndex + i) % hostCount;
            Host host = HOST_LIST.get(index);
            sb.append(host.host).append(":").append(host.port);
            if (i < hostCount - 1) sb.append(",");
        }

        // Erhöhen Sie den rotationIndex für den nächsten Aufruf
        rotationIndex = (rotationIndex + 1) % hostCount;

        sb.append("/").append(DATABASE_NAME);

        String path = sb.toString();

        Connection conn = DriverManager.getConnection(path, getProperties());

//        Connection conn = DriverManager.getConnection(path, DATABASE_USER.getUserName(), DATABASE_USER.getUserPasswordDecrypted());
        return new PooledConnection(path, conn, pool);
    }

    private int startIndex = 0;

    private List<Host> getNextHosts() {
        List<Host> rotatedList = new ArrayList<>();
        for (int i = 0; i < HOST_LIST.size(); i++) {
            int index = (startIndex + i) % HOST_LIST.size();
            rotatedList.add(HOST_LIST.get(index));
        }
        startIndex = (startIndex + 1) % HOST_LIST.size();
        return rotatedList;
    }

    protected Properties getProperties() throws GeneralSecurityException, IOException {
        Properties properties = new Properties();
        properties.setProperty("user", DATABASE_USER.getUserName());
        properties.setProperty("password", DATABASE_USER.getUserPasswordDecrypted());
        properties.setProperty("MaxPooledStatements", "250");
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf-8");
        return properties;
    }


    @Override
    public Statement createStatement() throws SQLException {
        //get a new connection every time
        try {
            Connection connection = getConnection();

            Statement statement = connection.createStatement();
            return statement;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void connect(String uniqueID, boolean fireEvent) {
        throw new RuntimeException("not implemented! Every create statement call give a new connection");
    }

    @Override
    public void disconnect(String uniqueID, boolean fireEvent) {
        throw new RuntimeException("not implemented! Every create statement call give a new connection");
    }

    @Override
    public boolean tableExist(String uniqueID, String tblName) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + DATABASE_NAME + "' AND TABLE_NAME = '" + tblName + "'")) {

            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException | ClassNotFoundException ignore) {

        }
        return false;
    }
}
