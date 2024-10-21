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
package de.longri.database.cache;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import de.longri.database.Abstract_Database;
import de.longri.database.DatabaseConnection;
import de.longri.database.MariaDB_Cluster_Connection;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.StoreBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCache {

    private static final Logger log = LoggerFactory.getLogger(AbstractCache.class);
    static final String UNIQUE_ID_THREAD_DATA_LOAD_ALL = "UNIQUE_ID_THREAD_DATA_LOAD_ALL";
    static final String UNIQUE_ID_SET_LAST_MODIFY_TABLE = "UNIQUE_ID_SET_LAST_MODIFY_TABLE";
    static final String UNIQUE_ID_LOAD_ALL_FROM_DISK = "UNIQUE_ID_LOAD_ALL_FROM_DISK";

    HashMap<String, LocalDateTime> LAST_MODIFY_MAP = new HashMap<>();
    ArrayList<AbstractTable<AbstractTableData>> TABLES;

    protected final String CACHE_FOLDER;

    public AbstractCache() {
        this("./CACHE");
    }

    public AbstractCache(String cacheFolder) {
        CACHE_FOLDER = cacheFolder;
        chkTables();
    }

    public void forceReloadCache(DatabaseConnection connection) throws SQLException, IOException, ClassNotFoundException, NotImplementedException, InterruptedException {
        synchronized (CACHE_FOLDER) {
            //first load lastModify map
            loadLAstModifiedFromDB(connection);
            loadAllFromDB(connection);
            saveAllToDisk();
        }
    }

    public void loadCache(DatabaseConnection connection) throws SQLException, IOException, ClassNotFoundException, NotImplementedException {

        synchronized (CACHE_FOLDER) {
            //first load lastModify map
            loadLAstModifiedFromDB(connection);

            boolean anyChanges = false;
            try {
                anyChanges = loadAllFromDisk(connection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (anyChanges) saveAllToDisk();
        }
    }

    public AbstractTable<AbstractTableData> getTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }

        if (TABLES == null) {
            throw new RuntimeException("Tables not initialized");
        }

        for (AbstractTable<AbstractTableData> table : TABLES) {
            if (table.getTableName().equals(tableName)) {
                return table;
            }
        }
        throw new RuntimeException("Table " + tableName + " not found");
    }

    protected abstract AbstractTable[] getTables();

    public File getCacheFolder() {
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        return cacheFolder;
    }

    protected void chkTables() {
        if (TABLES == null) {
            TABLES = new ArrayList<>();
            AbstractTable<AbstractTableData>[] tables = getTables();
            TABLES.addAll(Arrays.asList(tables));
        }
    }

    public void loadAllFromDB(DatabaseConnection connection) throws SQLException, ClassNotFoundException, InterruptedException {
        log.debug("loadAllFromDB");
        chkTables();
        if (!(connection instanceof MariaDB_Cluster_Connection)) connection.connect(UNIQUE_ID_THREAD_DATA_LOAD_ALL);
        int numberOfThreads = 12;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(TABLES.size());

        for (AbstractTable<AbstractTableData> table : TABLES) {
            executorService.submit(() -> {
                try {
                    loadTableFromDB(connection, table);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Zähler verringern, wenn die Aufgabe abgeschlossen ist
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        if (!(connection instanceof MariaDB_Cluster_Connection)) connection.disconnect(UNIQUE_ID_THREAD_DATA_LOAD_ALL);

        logCacheInfo("Load Cache from DB");
    }

    public void loadTableFromDB(DatabaseConnection connection, AbstractTable<AbstractTableData> table) throws SQLException {
        String tableName = table.getTableName();

        //delete alt data
        table.clear();

        //load all data from table and store in an object

        Statement st = connection.createStatement();

        ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + " " + table.getWhereClause() + ";");
        table.add(rs);
        table.SOURCE = TableReadSource.DB;
        table.SourceThread = Thread.currentThread().getName();
        DatabaseMetaData metaData = st.getConnection().getMetaData();
        table.SourceConnection = getConnectionInfo(metaData.getURL());

        LocalDateTime lastModify = getLastModifiedOnDb(table.getTableName());
        table.setDbLastModify(lastModify);
    }

    static Pattern pattern;

    static private String getConnectionInfo(String url) {

        if (pattern == null) {
            String regex = "jdbc:mysql://([^/,:]+:\\d+)";
            pattern = Pattern.compile(regex);
        }

        Matcher matcher = pattern.matcher(url);


        if (matcher.find()) {
            String result = matcher.group(1);
            boolean hasMultipleAddresses = url.substring(matcher.end(1)).contains(",");
            if (hasMultipleAddresses) {
                result = "CLUSTER " + result + ",...";
            }
            return result;
        } else {
            return "No match found for URL: " + url;
        }
    }

    public boolean loadAllFromDisk() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return this.loadAllFromDisk(null); // without connection load from Disk only
    }

    public boolean loadAllFromDisk(DatabaseConnection connection) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        chkTables();
        File newCacheFile = new File(getCacheFolder(), "tables_cache.bin");

        final AtomicBoolean anyChanges = new AtomicBoolean();

        if (newCacheFile.exists()) {
            if (connection != null && !(connection instanceof MariaDB_Cluster_Connection))
                connection.connect(UNIQUE_ID_LOAD_ALL_FROM_DISK);

            log.debug("loadAllFromDisk, cache folder: {}", newCacheFile.getAbsolutePath());
            try {
                InputStream is = new FileInputStream(newCacheFile);
                byte[] bytes = is.readAllBytes();
                is.close();

                StoreBase bitStore = new BitStore(bytes);
                int tableCount = bitStore.readInt();

                int numberOfThreads = 12;
                // Thread-Pool erstellen
                ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

                // CountDownLatch initialisieren
                CountDownLatch latch = new CountDownLatch(tableCount);

                for (int i = 0; i < tableCount; i++) {
                    String tableName = bitStore.readString();
                    LocalDateTime lastModifiedOnDisk = bitStore.readLocalDateTime();
                    executorService.submit(() -> {
                        try {
                            // Ihre Methode aufrufen
                            boolean changed = loadTableFromDisk(tableName, lastModifiedOnDisk, connection);
                            if (changed) anyChanges.set(true);
                        } catch (SQLException | NotImplementedException | IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            // Zähler verringern, wenn die Aufgabe abgeschlossen ist
                            latch.countDown();
                        }
                    });
                }

                // Warten, bis alle Aufgaben beendet sind
                latch.await();

                // Thread-Pool herunterfahren
                executorService.shutdown();

            } catch (Exception e) {
                log.error("loadAllFromDisk", e);
                log.warn("Destroy Cache and load all from DB");
                anyChanges.set(true);
                deleteDirectory(getCacheFolder());
                if (connection != null) loadAllFromDB(connection);
            }
            if (connection != null && !(connection instanceof MariaDB_Cluster_Connection))
                connection.disconnect(UNIQUE_ID_LOAD_ALL_FROM_DISK);
        } else {
            // if cache not exists, load from DB
            log.debug("loadAllFromDisk, cache folder [{}] not exist, load from DB", newCacheFile.getAbsolutePath());
            anyChanges.set(true);
            if (connection != null) loadAllFromDB(connection);
        }


        logCacheInfo("Load Cache from disk");
        return anyChanges.get();
    }

    protected boolean loadTableFromDisk(String tableName, LocalDateTime lastModifiedOnDisk, DatabaseConnection connection) throws IOException, SQLException, NotImplementedException {
        boolean anyChanges = false;


        AbstractTable<AbstractTableData> table = getTable(tableName);

        //delete alt data
        table.clear();

        LocalDateTime lastModifiedOnDB = connection == null ? null : getLastModifiedOnDb(tableName);

        if ((connection != null && lastModifiedOnDB == null) || (connection != null && lastModifiedOnDB.isAfter(lastModifiedOnDisk))) {
            //cache is outdated, load from DB
            anyChanges = true;

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + " " + table.getWhereClause() + ";");
            table.add(rs);
            table.SOURCE = TableReadSource.DB;
            table.SourceThread = Thread.currentThread().getName();
            DatabaseMetaData metaData = st.getConnection().getMetaData();
            table.SourceConnection = getConnectionInfo(metaData.getURL());

        } else {
            table.loadFromDisk(getCacheFolder());
            table.SOURCE = TableReadSource.Disk;
            table.SourceThread = Thread.currentThread().getName();
            table.SourceConnection = "HDD";
        }
        table.setDbLastModify(getLastModifiedOnDb(tableName));
        return anyChanges;
    }

    public void saveAllToDisk() throws IOException, NotImplementedException {

        log.debug("Write Cache to disk!");

        BitStore bitStore = new BitStore();
        bitStore.write(TABLES.size());

        for (AbstractTable<AbstractTableData> table : TABLES) {
            bitStore.write(table.getTableName());
            bitStore.write(table.getDbLastModify());
            table.saveToDisk(getCacheFolder());
        }

        byte[] bytes = bitStore.getArray();
        File newCacheFile = new File(getCacheFolder(), "tables_cache.bin");
        if (!newCacheFile.exists()) {
            newCacheFile.getParentFile().mkdirs();
            newCacheFile.createNewFile();
        }
        OutputStream os = new FileOutputStream(newCacheFile);
        os.write(bytes);
        os.close();

        log.debug("Cache written to disk: {}", newCacheFile.getAbsolutePath());


        logCacheInfo("Write Cache to disk");
    }

    private void logCacheInfo(String infoName) {
        log.info("Cache info: " + infoName);
        log.info("\n" + AsciiTable.getTable(TABLES, Arrays.asList(
                new Column().header("Name").with(table -> table != null ? table.tableName : "N/A"),
                new Column().header("entries").with(table -> table != null ? Integer.toString(table.tableData.size()) : "N/A"),
                new Column().header("fromDB").dataAlign(HorizontalAlign.CENTER).with(table -> {
                    if (table == null) return "N/A";
                    return table.SOURCE == TableReadSource.Disk ? "" : table.SourceThread;
                }),
                new Column().header("fromDisk").dataAlign(HorizontalAlign.CENTER).with(table -> {
                    if (table == null) return "N/A";
                    return table.SOURCE == TableReadSource.DB ? "" : table.SourceThread;
                }),
                new Column().header("last modify").with(table -> table != null ? Abstract_Database.getDateString(table.lastModified) : "N/A"),
                new Column().header("Connection").with(table -> table != null ? table.SourceConnection : "N/A")
        )));
    }

    public static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    protected void loadLAstModifiedFromDB(DatabaseConnection connection) throws SQLException, ClassNotFoundException {

        log.debug("loadLastModifiedFromDB");

        String sql = "SELECT * FROM last_modified ";
        if (!(connection instanceof MariaDB_Cluster_Connection)) connection.connect(UNIQUE_ID_SET_LAST_MODIFY_TABLE);
        ResultSet rs = connection.createStatement().executeQuery(sql);
        while (rs.next()) {
            LocalDateTime lastModifiedOnDb = Abstract_Database.getDateTime(rs.getString("localDateTime"));
            LAST_MODIFY_MAP.put(rs.getString("tableName"), lastModifiedOnDb);
        }
        if (!(connection instanceof MariaDB_Cluster_Connection)) connection.disconnect(UNIQUE_ID_SET_LAST_MODIFY_TABLE);
    }

    protected LocalDateTime getLastModifiedOnDb(String tableName) throws SQLException {
        return LAST_MODIFY_MAP.get(tableName);
    }
}
