package de.longri.database;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Stack;

// Connection pool class
public class ConnectionPool {
    private Stack<PooledConnection> connectionPool;
    private final PoolConnectionCreater CREATER;

    public ConnectionPool(PoolConnectionCreater creater) throws SQLException {
        CREATER = creater;
        this.connectionPool = new Stack<>();

    }


    // Get a connection from the pool
    public synchronized PooledConnection getConnection() {
        if (connectionPool.isEmpty()) {
            try {
                connectionPool.push(CREATER.createNewPooledConnection());
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return connectionPool.pop();
    }

    // Return a connection back to the pool
    public synchronized void returnConnection(PooledConnection connection) {
        if (connection != null) {
            connectionPool.push(connection);
        }
    }

    // Shutdown the pool and close all connections
    public synchronized void shutdown() throws SQLException {
        while (!connectionPool.isEmpty()) {
            PooledConnection pooledConnection = connectionPool.pop();
            pooledConnection.getActualConnection().close();
        }
    }

    public void push(PooledConnection newPooledConnection) {
        connectionPool.push(newPooledConnection);
    }
}
