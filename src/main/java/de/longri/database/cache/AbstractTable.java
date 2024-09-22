package de.longri.database.cache;

import de.longri.database.properties.NamedPropertyType;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.StoreBase;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractTable<T extends AbstractTableData> implements Iterable<T> {

    public abstract NamedPropertyType[] getColumnTypes();

    public abstract String getTableName();

    public abstract Class<?> getDataClass();

    private final static Logger log = LoggerFactory.getLogger(de.longri.database.table_data.AbstractTable.class);

    public boolean isEmpty() {
        return tableData.isEmpty();
    }

    public int size() {
        return tableData.size();
    }

    public TableReadSource SOURCE = TableReadSource.unknown;

    public String SourceThread = "";

    public String SourceConnection = "";

    public final String tableName;

    public final ArrayList<T> tableData = new ArrayList<>();

    public LocalDateTime lastModified = LocalDateTime.MAX;

    public AbstractTable() {
        this.tableName = getTableName();
    }

    public LocalDateTime getDbLastModify() {
        return lastModified;
    }

    public void clear() {
        tableData.clear();
    }

    public void add(T data) {
        tableData.add(data);
    }

    public void add(ResultSet rs) throws SQLException {
        while (rs.next()) {
            this.add(this.create(rs));
        }
    }

    T createNewEntry() {
        try {
            // Dynamische Erstellung von ConcreteTableData
            Class<?> dataClass = getDataClass();
            Constructor<?> dataConstructor = dataClass.getConstructor(AbstractTable.class);
            AbstractTableData tableData = (AbstractTableData) dataConstructor.newInstance(this);
            return (T) tableData;
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("can't create instanz of dataClass: " + getDataClass());
    }

    private T create(ResultSet rs) throws SQLException {
        try {
            // Dynamische Erstellung von ConcreteTableData
            Class<?> dataClass = getDataClass();
            Constructor<?> dataConstructor = dataClass.getConstructor(AbstractTable.class);
            AbstractTableData tableData = (AbstractTableData) dataConstructor.newInstance(this);
            tableData.setFromResultSet(rs);
            return (T) tableData;
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("can't create instanz of dataClass: " + getDataClass());
    }

    private T create(BitStore bitStore) throws NotImplementedException {
        try {
            // Dynamische Erstellung von ConcreteTableData
            Class<?> dataClass = getDataClass();
            Constructor<?> dataConstructor = dataClass.getConstructor(AbstractTable.class);
            AbstractTableData tableData = (AbstractTableData) dataConstructor.newInstance(this);
            tableData.setFromBitStore(bitStore);
            return (T) tableData;
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("can't create instanz of dataClass: " + getDataClass());
    }

    public void saveToDisk(File cacheFolder) throws NotImplementedException, IOException {
        // serialize and store on disk

        StoreBase bitStore = new BitStore();

        bitStore.write(tableData.size());

        for (T data : tableData) {
            data.serialize(bitStore);
        }

        byte[] bytes = bitStore.getArray();
        File newCacheFile = new File(cacheFolder, this.tableName + "_cache.bin");
        if (!newCacheFile.exists()) {
            newCacheFile.getParentFile().mkdirs();
            newCacheFile.createNewFile();
        }
        OutputStream os = new FileOutputStream(newCacheFile);
        os.write(bytes);
        os.close();

    }

    public void loadFromDisk(File cacheFolder) {
        File newCacheFile = new File(cacheFolder, this.tableName + "_cache.bin");
        if (newCacheFile.exists()) {
            try {
                InputStream is = new FileInputStream(newCacheFile);
                byte[] bytes = is.readAllBytes();
                is.close();

                BitStore bitStore = new BitStore(bytes);

                int tableDataSize = bitStore.readInt();

                for (int i = 0; i < tableDataSize; i++) {

                    this.add(create(bitStore));
                }
            } catch (Exception e) {
                log.error("Error with loading data from disk", e);
                //Anything is wrong with the cache file. Delete it!
                newCacheFile.delete();
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof de.longri.database.table_data.AbstractTable otherTable) {
            return tableData.equals(otherTable.tableData);
        }
        return false;
    }

    public void setDbLastModify(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }


    @Override
    public String toString() {
        // iterate items
        if (tableData.size() == 0) return "[]";
        ArrayList<T> items = this.tableData;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(items.get(0));
        int size = items.size();
        for (int i = 1; i < size; i++) {
            buffer.append(", ");
            buffer.append(items.get(i));
        }
        buffer.append(']');
        return buffer.toString();
    }


    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return tableData.iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    public void forEach(Consumer action) {
        tableData.forEach(action);
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="../util/Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @Override
    public Spliterator<T> spliterator() {
        return tableData.spliterator();
    }


}
