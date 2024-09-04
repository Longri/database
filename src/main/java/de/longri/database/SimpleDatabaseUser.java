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

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SimpleDatabaseUser implements DatabaseUser {

    final private String NAME;
    private String PASSWD;

    public SimpleDatabaseUser(String name, String passwd) {
        this.PASSWD = passwd;
        this.NAME = name;
    }

    @Override
    public void setPassword(String newPasswd) {
        PASSWD = newPasswd;
    }

    @Override
    public String getUserName() {
        return NAME;
    }

    @Override
    public String getUserPasswordDecrypted() throws GeneralSecurityException, IOException {
        return PASSWD;
    }

    @Override
    public String toString() {
        return "SimpleDatabaseUser: "+NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof DatabaseUser user) {
            return user.getUserName().equals(getUserName());
        }
        return false;
    }
}
