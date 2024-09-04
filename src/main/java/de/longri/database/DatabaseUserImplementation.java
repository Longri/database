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

import de.longri.utils.Crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DatabaseUserImplementation implements DatabaseUser {

    protected String sqlUser;

    @Override
    public void setPassword(String password) {
        try {
            this.encryptedPassword = Crypto.encrypt(password);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    protected String encryptedPassword;

    public DatabaseUserImplementation(String sqlUser, String password) {
        this.sqlUser = sqlUser;
        try {
            this.encryptedPassword = Crypto.encrypt(password);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public DatabaseUserImplementation(DatabaseUserImplementation other) {
        this.sqlUser = other.sqlUser;
        this.encryptedPassword = other.encryptedPassword;
    }

    @Override
    public String getUserName() {
        return sqlUser;
    }

    @Override
    public String getUserPasswordDecrypted() throws GeneralSecurityException, IOException {
        return Crypto.decrypt(encryptedPassword);
    }
}
