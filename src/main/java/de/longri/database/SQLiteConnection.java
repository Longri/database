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

import java.io.File;
import java.sql.*;

public class SQLiteConnection extends DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(SQLiteConnection.class);

    final File databaseFile;

    public SQLiteConnection(File databaseFile) {
        this.databaseFile = databaseFile;
    }


    @Override
    public String getDatabaseName() {
        return null;
    }

    @Override
    public SQL_TYPE getType() {
        return SQL_TYPE.SQLite;
    }

    @Override
    public String getAutoIncrementString() {
        return "AUTOINCREMENT";
    }


    private final String UNIQUE_ID = "SQLiteConnection.importDumbString()";

    @Override
    public void importDumbString(String dump) throws SQLException, ClassNotFoundException {
        try {
            this.connect(UNIQUE_ID);
            Statement stmt = connection.createStatement();
            int changes = stmt.executeUpdate(dump);

            if (changes > 0) {
                log.debug("Database changes increment change count");
                dump = "UPDATE SCHEME SET changeid = changeid + 1 WHERE id=1";
                stmt.executeUpdate(dump);
                //            if (stmt.executeUpdate(sql) != 1)
                //                throw new RuntimeException("Cannt update change id");
            }
            stmt.close();
            this.disconnect(UNIQUE_ID);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseUser getDatabaseUser() {
        return null;
    }

    @Override
    public void setDatabaseUser(DatabaseUser user) {
        // do nothing
    }

    @Override
    public boolean databaseExist() {
        throw new RuntimeException("is not implemented");
    }

    @Override
    public void createDatabase() {
        throw new RuntimeException("is not implemented");
    }


    @Override
    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }

    @Override
    public boolean tableExist(String uniqueID, String tblName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tblName + "';";
        Statement stmt = null;
        try {
            this.connect(uniqueID);
            stmt = this.createStatement();
            ResultSet result = stmt.executeQuery(sql);
            result.next();
            String exS = result.getString(1);
            return exS.equals(tblName);
        } catch (ClassNotFoundException throwables) {
            return false;
        } finally {
            this.disconnect(uniqueID);
        }
    }
}
