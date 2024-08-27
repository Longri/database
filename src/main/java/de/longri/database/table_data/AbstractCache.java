package de.longri.database.table_data;

import de.longri.database.Abstract_Database;
import de.longri.database.DatabaseConnection;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.StoreBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class AbstractCache {

    private static final Logger log = LoggerFactory.getLogger(AbstractCache.class);
    static final String UNIQUE_ID_THREAD_DATA_LOAD_ALL = "UNIQUE_ID_THREAD_DATA_LOAD_ALL";
    static final String UNIQUE_ID_SET_LAST_MODIFY_TABLE = "UNIQUE_ID_SET_LAST_MODIFY_TABLE";
    static final String UNIQUE_ID_LOAD_ALL_FROM_DISK = "UNIQUE_ID_LOAD_ALL_FROM_DISK";

    HashMap<String,LocalDateTime> LAST_MODIFY_MAP = new HashMap<>();
    ArrayList<AbstractTable<AbstractTableDataEntry>> TABLES;

    protected final String CACHE_FOLDER;

    public AbstractCache(String cacheFolder) {
        CACHE_FOLDER = cacheFolder;
    }

    public AbstractCache() {
        this("./CACHE");
    }

    public void forceReloadCache(DatabaseConnection connection) throws SQLException, IOException, ClassNotFoundException, NotImplementedException {
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

            boolean anyChanges = loadAllFromDisk(connection);
            if (anyChanges) saveAllToDisk();
        }
    }

    public AbstractTable<AbstractTableDataEntry> getTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }

        if (TABLES == null) {
            throw new RuntimeException("Tables not initialized");
        }

        for (AbstractTable<AbstractTableDataEntry> table : TABLES) {
            if (table.getTableName().equals(tableName)) {
                return table;
            }
        }
        throw new RuntimeException("Table " + tableName + " not found");
    }


    protected abstract AbstractTable<AbstractTableDataEntry>[] getTables();

    public File getCacheFolder() {
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        return cacheFolder;
    }

    private void chkTables() {
        if (TABLES == null) {
            TABLES = new ArrayList<>();
            AbstractTable<AbstractTableDataEntry>[] tables = getTables();
            TABLES.addAll(Arrays.asList(tables));
        }
    }

    public void loadAllFromDB(DatabaseConnection connection) throws SQLException, ClassNotFoundException {
        log.debug("loadAllFromDB");
        chkTables();
        connection.connect(UNIQUE_ID_THREAD_DATA_LOAD_ALL);
        for (AbstractTable<AbstractTableDataEntry> table : TABLES) {
            loadTableFromDB(connection, table);
        }
        connection.disconnect(UNIQUE_ID_THREAD_DATA_LOAD_ALL);

        logCacheInfo("Load Cache from DB");
    }

    public void loadTableFromDB(DatabaseConnection connection, AbstractTable<AbstractTableDataEntry> table) throws SQLException {
        String tableName = table.getTableName();

        //delete alt data
        table.clear();

        //load all data from table and store in an object
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
        table.add(rs);
        table.SOURCE = AbstractTable.Source.DB;

        LocalDateTime lastModify = getLastModifiedOnDb(table.getTableName());
        table.setDbLastModify(lastModify);
    }

    public boolean loadAllFromDisk(DatabaseConnection connection) throws IOException,  SQLException, ClassNotFoundException {
        chkTables();
        File newCacheFile = new File(getCacheFolder(), "tables_cache.bin");

        boolean anyChanges = false;

        if (newCacheFile.exists()) {
            connection.connect(UNIQUE_ID_LOAD_ALL_FROM_DISK);

            log.debug("loadAllFromDisk, cache folder: {}", newCacheFile.getAbsolutePath());
            try {
                InputStream is = new FileInputStream(newCacheFile);
                byte[] bytes = is.readAllBytes();
                is.close();

                StoreBase bitStore = new BitStore(bytes);
                int tableCount = bitStore.readInt();
                for (int i = 0; i < tableCount; i++) {
                    String tableName = bitStore.readString();
                    LocalDateTime lastModifiedOnDisk = bitStore.readLocalDateTime();
                    AbstractTable<AbstractTableDataEntry> table = getTable(tableName);

                    //delete alt data
                    table.clear();

                    LocalDateTime lastModifiedOnDB = getLastModifiedOnDb(tableName);

                    if (lastModifiedOnDB == null || lastModifiedOnDB.isAfter(lastModifiedOnDisk)) {
                        //cache is outdated, load from DB
                        anyChanges = true;

                        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
                        table.add(rs);
                        table.SOURCE = AbstractTable.Source.DB;

                    } else {
                        table.loadFromDisk(getCacheFolder());
                        table.SOURCE = AbstractTable.Source.Disk;
                    }
                    table.setDbLastModify(getLastModifiedOnDb(tableName));
                }
            } catch (Exception e) {
                log.error("loadAllFromDisk", e);
                log.warn("Destroy Cache and load all from DB");
                anyChanges = true;
                deleteDirectory(getCacheFolder());
                loadAllFromDB(connection);
            }
            connection.disconnect(UNIQUE_ID_LOAD_ALL_FROM_DISK);
        } else {
            // if cache not exists, load from DB
            log.debug("loadAllFromDisk, cache folder [{}] not exist, load from DB", newCacheFile.getAbsolutePath());
            anyChanges = true;
            loadAllFromDB(connection);
        }


        logCacheInfo("Load Cache from disk");
        return anyChanges;
    }

    public void saveAllToDisk() throws IOException, NotImplementedException {

        log.debug("Write Cache to disk!");

        BitStore bitStore = new BitStore();
        bitStore.write(TABLES.size());

        for (AbstractTable<AbstractTableDataEntry> table : TABLES) {
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
        StringBuilder sb = new StringBuilder("+----------------------------------+---------+--------+----------+------------------------+\n" +
                "|            TableName             | entries | fromDB | fromDisk |      last modify       |\n" +
                "+----------------------------------+---------+--------+----------+------------------------+\n");

        for (AbstractTable<AbstractTableDataEntry> table : TABLES) {
            table.writeInfoTable(sb);
        }

        sb.append("+----------------------------------+---------+--------+----------+------------------------+");
        log.info("\n" + sb.toString());
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

    private void loadLAstModifiedFromDB(DatabaseConnection connection) throws IOException,  SQLException, ClassNotFoundException {

        log.debug("loadLastModifiedFromDB");

        String sql = "SELECT * FROM last_modified ";
        connection.connect(UNIQUE_ID_SET_LAST_MODIFY_TABLE);
        ResultSet rs = connection.createStatement().executeQuery(sql);
        while (rs.next()) {
            LocalDateTime lastModifiedOnDb = Abstract_Database.getDateTime(rs.getString("localDateTime"));
            LAST_MODIFY_MAP.put(rs.getString("tableName"), lastModifiedOnDb);
        }
        connection.disconnect(UNIQUE_ID_SET_LAST_MODIFY_TABLE);
    }

    private LocalDateTime getLastModifiedOnDb(String tableName) throws SQLException {
        return LAST_MODIFY_MAP.get(tableName);
    }
}
