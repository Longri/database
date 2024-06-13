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

import com.healthmarketscience.jackcess.*;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 07.04.2016.
 * <p>
 * changed to separate DB handling for general use by Longri 14.10.2020
 * <p>
 * <p>
 * this code is based on the library 'Jackcess'
 * https://jackcess.sourceforge.io/
 */
public abstract class AccessDB {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AccessDB.class);
    public static final String BR = System.getProperty("line.separator");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("kk:mm", Locale.GERMANY);
    public static final DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    public static final DateFormat DAY_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);

    private static boolean driverLoaded = false;

    private static void loadDriver() throws ClassNotFoundException {
        if (driverLoaded) return;
        //You must add the hsqldb jar to the classpath and call
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Class.forName("com.healthmarketscience.jackcess.DatabaseBuilder");
        // before you try to open the connection (once is enough) for the DriverManager to find the driver with that url!
        driverLoaded = true;
    }

    private final String PATH;
    private Connection conn;
    private final com.healthmarketscience.jackcess.Database jackDB;


    //#####################################################################################################################
    public AccessDB(String path) throws SQLException, ClassNotFoundException, IOException {
        loadDriver();
        PATH = path;
        File dbFile = new File(path);
        boolean createNew = !(dbFile.exists());
        if (createNew) {
            dbFile.getParentFile().mkdirs();
            jackDB = DatabaseBuilder.create(com.healthmarketscience.jackcess.Database.FileFormat.V2016, dbFile);
        } else {
            jackDB = DatabaseBuilder.open(dbFile);
        }
        Properties connProperties = new Properties();
        connProperties.setProperty("charset", "UTF-8");
        conn = DriverManager.getConnection("jdbc:ucanaccess://" + path, connProperties);
//        if (createNew)
//            newDB();

        checkSchemeVersion();
        closeConnection();

    }

    protected abstract void alterDatabaseVersion(int newVersion, int altVersion);

    protected abstract int getLatestDatabaseChange();

    public ResultSet rawQuery(String sqlQuery) throws SQLException {
        return conn.prepareCall(sqlQuery).executeQuery();
    }

    private void openConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            Properties connProperties = new Properties();
            connProperties.setProperty("charset", "UTF-8");
            conn = DriverManager.getConnection("jdbc:ucanaccess://" + PATH, connProperties);
        }
    }

    private void closeConnection() throws SQLException {
        if (conn != null) conn.close();
        conn = null;
    }

    private void checkSchemeVersion() throws SQLException {
        int latest = getLatestDatabaseChange();
        int actual = getDatabaseSchemeVersion();

        if (latest > actual) {
            alterDatabaseVersion(latest, actual);
            setDatabaseSchemeVersion();
        }
    }

    private AtomicInteger shemaVersion = new AtomicInteger(-1);

    public int getDatabaseSchemeVersion() {

        if (shemaVersion.get() >= 0) return shemaVersion.get();

        if (!isTableExists("Config")) {
            return -1;
        }

        ResultSet cursor = null;
        try {
            cursor = rawQuery("SELECT * FROM Config WHERE Key like 'DatabaseSchemeVersion'");

            cursor.next();
            int version = Integer.parseInt(cursor.getString(2));
            shemaVersion.set(version);
            return version;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    private void setDatabaseSchemeVersion() throws SQLException {
        shemaVersion.set(getLatestDatabaseChange());
    }

    public boolean isTableExists(String tableName) {
        for (Table table : jackDB)
            if (table.getName().equals(tableName)) return true;
        return false;
    }


}
