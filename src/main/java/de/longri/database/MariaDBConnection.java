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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaDBConnection extends MySqlConnection {

    final String[] ClusterAdresses;
    final String[] ClusterPorts;
    protected final String connectionString;

    public MariaDBConnection(String address, String port, String databaseName, DatabaseUser user) throws GeneralSecurityException, UnsupportedEncodingException {
        super(address, port, databaseName, user);
        ClusterAdresses = null;
        ClusterPorts = null;

        connectionString = "jdbc:mysql://" + address + ":" + port + "/" + databaseName;
    }

    public MariaDBConnection(String[] adresses, String[] ports, String databaseName, DatabaseUser user) throws GeneralSecurityException, UnsupportedEncodingException {
        super(adresses[0], ports[0], databaseName, user);
        ClusterAdresses = adresses;
        ClusterPorts = ports;

        StringBuilder connectionStringBuilder = new StringBuilder("jdbc:mysql://");

        for (int i = 0; i < ClusterAdresses.length; i++) {
            connectionStringBuilder.append(ClusterAdresses[i].trim()).append(":").append(ClusterPorts[i].trim());
            if (i < ClusterAdresses.length - 1)
                connectionStringBuilder.append(",");
        }

        connectionStringBuilder.append("/").append(databaseName);

        connectionString = connectionStringBuilder.toString();
    }

    @Override
    protected Connection getConnection() throws ClassNotFoundException, SQLException {

        if (ClusterAdresses == null) return super.getConnection();

        Class.forName("com.mysql.cj.jdbc.Driver");


        try {
            return DriverManager.getConnection(connectionString.toString(), getProperties());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toString() {
        return "MariaDBConnection: " + connectionString;
    }

}
