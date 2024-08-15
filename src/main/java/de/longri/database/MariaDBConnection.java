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
            connectionStringBuilder.append(ClusterAdresses[i]).append(":").append(ClusterPorts[i]);
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
