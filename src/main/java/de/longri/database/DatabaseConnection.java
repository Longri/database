/*
 * Copyright (C) 2024 Longri
 *
 * This file is part of fxutils.
 *
 * fxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * fxutils is distributed in the hope that it will be useful,
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class DatabaseConnection {

    public boolean DEBUG = false;

    public DatabaseConnection() {
    }

    public abstract String getDatabaseName();

    public abstract SQL_TYPE getType();

    public abstract String getAutoIncrementString();

    public abstract void importDumbString(String dump) throws SQLException, ClassNotFoundException;

    public abstract DatabaseUser getDatabaseUser();

    public abstract void setDatabaseUser(DatabaseUser user);

    public abstract boolean databaseExist();

    public abstract void createDatabase();

    public interface ConnectionListener {
        void databaseConnected(boolean NEW_CREATED_CONNECTION, String uniqueID, ArrayList<String> OPEN_CONNECTION_IDs);

        void databaseDisconnected(boolean CLOSED_CONNECTION, String uniqueID, ArrayList<String> OPEN_CONNECTION_IDs);
    }

    private final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);


    protected Connection connection = null;
    protected ArrayList<ConnectionListener> listeners = new ArrayList<>();

    public void addConnectionListener(ConnectionListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }


    private final ArrayList<String> OPEN_CONNECTION_IDs = new ArrayList<>();

    public void connect(String uniqueID) throws SQLException, ClassNotFoundException {
        synchronized (OPEN_CONNECTION_IDs) {

            boolean NEW_CREATED_CONNECTION;

            if (connection == null) {
                connection = getConnection();
                NEW_CREATED_CONNECTION = true;
            } else {
                NEW_CREATED_CONNECTION = false;
            }
            if (!OPEN_CONNECTION_IDs.contains(uniqueID))
                OPEN_CONNECTION_IDs.add(uniqueID);

            if (DEBUG) {
                log.debug("open a connection [{}] from: {}", OPEN_CONNECTION_IDs.size(), getCaller());
                log.debug("  open connections: {}", OPEN_CONNECTION_IDs.toString());
            }

            //call listener
            for (ConnectionListener listener : listeners) {
                listener.databaseConnected(NEW_CREATED_CONNECTION, uniqueID, OPEN_CONNECTION_IDs);
            }

        }
    }

    public void disconnect(String uniqueID) throws SQLException {
        synchronized (OPEN_CONNECTION_IDs) {
            if (connection == null) {
                if (DEBUG) log.warn("Disconnect to a closed connection");
                return;
            }

            OPEN_CONNECTION_IDs.remove(uniqueID);

            boolean CLOSED_CONNECTION;

            if (OPEN_CONNECTION_IDs.isEmpty()) {
                connection.close();
                connection = null;
                CLOSED_CONNECTION = true;
            } else {
                CLOSED_CONNECTION = false;
            }

            if (DEBUG) {
                log.debug("close a connection [{}] from: {}", OPEN_CONNECTION_IDs.size(), getCaller());
                if (connection == null)
                    log.debug("   ALL connections are closed! Last Connection are {}", uniqueID);
                else
                    log.debug("  still open connections: {}", OPEN_CONNECTION_IDs.toString());
            }

            //call listener
            for (ConnectionListener listener : listeners) {
                listener.databaseDisconnected(CLOSED_CONNECTION, uniqueID, OPEN_CONNECTION_IDs);
            }

        }
    }

    protected abstract Connection getConnection() throws ClassNotFoundException, SQLException;

    public Statement createStatement() throws SQLException {
        synchronized (OPEN_CONNECTION_IDs) {
            if (connection == null) throw new RuntimeException("no connection to Database");
            return connection.createStatement();
        }
    }

    public abstract boolean tableExist(String uniqueID, String tblName) throws SQLException;

    public static String getCaller() {
        // Hole den aktuellen Stack-Trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Beachte, dass die Stack-Trace-Elemente ab Index 2 relevant sind, da die ersten zwei Elemente
        // in der Regel den Aufruf der getStackTrace() und printCaller() Methode selbst darstellen.
        if (stackTrace.length >= 5) {
            // Stack-Trace-Element an Index 3 entspricht dem Aufrufer
            StackTraceElement caller = stackTrace[4];
            return "MethodCaller: " + caller.getClassName() + "." + caller.getMethodName();
        } else {
            return "The MethodCaller could not be determined! ";
        }
    }
}
