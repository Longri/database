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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FirebirdConnection extends DatabaseConnection {

    private final Logger log = LoggerFactory.getLogger(FirebirdConnection.class);
    private final String address;
    private final String port;
    private final String databasePath;
    private DatabaseUser user;

    public FirebirdConnection(String address, String port, String databasePath, DatabaseUser user) {
        super();
        this.address = address;
        this.port = port;
        this.databasePath = databasePath;
        this.user = user;
    }

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        String jdbcUrl = "jdbc:firebirdsql://" + address + ":" + port + "/" + databasePath;
        String username = user.getUserName();
        String password = null;
        try {
            password = user.getUserPasswordDecrypted();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public String getDatabaseName() {
        return databasePath;
    }

    @Override
    public SQL_TYPE getType() {
        return SQL_TYPE.Firebird;
    }

    @Override
    public String getAutoIncrementString() {
        return null;
    }

    @Override
    public void importDumbString(String dump) throws SQLException, ClassNotFoundException {

    }

    @Override
    public DatabaseUser getDatabaseUser() {
        return user;
    }

    @Override
    public void setDatabaseUser(DatabaseUser user) {
        this.user = user;
    }

    @Override
    public boolean databaseExist() {
        return false;
    }

    @Override
    public void createDatabase() {

    }

    @Override
    public boolean tableExist(String uniqueID, String tblName) throws SQLException {
        return false;
    }
}
