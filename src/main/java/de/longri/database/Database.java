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


import java.sql.SQLException;
import java.sql.Statement;

public interface Database {

    void connect(String uniqueID) throws SQLException, ClassNotFoundException;

    void disconnect(String uniqueID);

    int executeUpdate(String uniqueID, String sql) throws SQLException;

    boolean tableExist(String uniqueID, String tblName);

    SQL_TYPE getType();

    Statement createStatement() throws SQLException;

    DatabaseUser getDatabaseUser();

    void setUser(DatabaseUser user);

    DatabaseConnection getConnection();

    String getString(String uniqueID, String sql);
}
