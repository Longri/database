package de.longri.database.cache;

import de.longri.database.properties.*;
import de.longri.serializable.BitStore;
import de.longri.serializable.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AbstractTableTest {

    static final String TEST_CACHE_PATH = "./TEST/ABSTRACT_TABLE_CACHE_TEST/";

    @BeforeAll
    static void setUp() throws IOException {
        //delete cache folder
        deleteDirectoryRecursive(new File(TEST_CACHE_PATH));
    }

    @Test
    void getName() {
        Table1 table1 = new Table1();
        assertEquals("table1", table1.getTableName());
        assertEquals(3, table1.getColumnTypes().length);
        TableData1 tableData1 = table1.createNewEntry();

        AbstractProperty<?> idType = tableData1.columns.get(0);
        assertEquals(PropertyType.Integer, idType.getType());
        assertEquals("id", idType.getName());
        AbstractProperty<?> nameType = tableData1.columns.get(1);
        assertEquals(PropertyType.String, nameType.getType());
        assertEquals("name", nameType.getName());
        AbstractProperty<?> valueType = tableData1.columns.get(2);
        assertEquals(PropertyType.Integer, valueType.getType());
        assertEquals("value", valueType.getName());

        assertTrue(tableData1.isNewEntry());


        Table2 table2 = new Table2();
        assertEquals("table2", table2.getTableName());
        assertEquals(3, table2.getColumnTypes().length);
        TableData2 tableData2 = table2.createNewEntry();
        assertEquals(PropertyType.Integer, tableData2.id.getType());
        assertEquals("id", tableData2.id.getName());
        assertEquals(PropertyType.String, tableData2.description.getType());
        assertEquals("description", tableData2.description.getName());
        assertEquals(PropertyType.Bool, tableData2.isActive.getType());
        assertEquals("is_active", tableData2.isActive.getName());
        assertTrue(tableData2.isNewEntry());
    }

    @Test
    void getCacheTest() throws NotImplementedException, IOException, SQLException, ClassNotFoundException, InterruptedException {
        TestCache testCache = new TestCache(TEST_CACHE_PATH);

        assertInstanceOf(Table1.class, testCache.TABLES.get(0));
        assertInstanceOf(Table2.class, testCache.TABLES.get(1));
        assertNotNull(testCache.TABLE1);
        assertNotNull(testCache.TABLE2);
        assertEquals(0, testCache.TABLE1.size());
        assertEquals(0, testCache.TABLE2.size());

        TableData1 newTable1Entry = testCache.TABLE1.createNewEntry();
        assertEquals(0, newTable1Entry.id.getValue());
        assertEquals("", newTable1Entry.name.getValue());
        assertEquals(0, newTable1Entry.value.getValue());
        assertTrue(newTable1Entry.isNewEntry());

        testCache.TABLE1.add(newTable1Entry);
        assertEquals(1, testCache.TABLE1.size());

        testCache.saveAllToDisk();

        TestCache testCache2 = new TestCache(TEST_CACHE_PATH);
        testCache2.loadAllFromDisk(); // without connection load from Disk only
        assertEquals(1, testCache2.TABLE1.size());
        assertEquals(0, testCache2.TABLE2.size());

    }

    static class TestCache extends AbstractCache {

        public final Table1 TABLE1;
        public final Table2 TABLE2;

        TestCache(String testCachePath) {
            super(testCachePath);

            Object table = getTable("table1");
            if (table == null) throw new RuntimeException("Table1 not found");
            TABLE1 = (Table1) table;
            table = getTable("table2");
            if (table == null) throw new RuntimeException("Table2 not found");
            TABLE2 = (Table2) table;

        }


        @Override
        protected AbstractTable[] getTables() {
            return new AbstractTable[]{new Table1(), new Table2()};
        }
    }

    static class Table1 extends AbstractTable<TableData1> {
        @Override
        public Class<?> getDataClass() {
            return TableData1.class;
        }

        @Override
        public NamedPropertyType[] getColumnTypes() {
            return new NamedPropertyType[]{
                    PropertyBuilder.create("id", PropertyType.Integer),
                    PropertyBuilder.create("name", PropertyType.String),
                    PropertyBuilder.create("value", PropertyType.Integer)
            };
        }

        @Override
        public String getTableName() {
            return "table1";
        }
    }

    static class TableData1 extends AbstractTableData {

        final AbstractProperty<?> id;
        final AbstractProperty<?> name;
        final AbstractProperty<?> value;

        public TableData1(AbstractTable<? extends AbstractTableData> table) {
            super(table);
            id = get("id");
            name = get("name");
            value = get("value");
        }
    }

    static class Table2 extends AbstractTable<TableData2> {
        @Override
        public NamedPropertyType[] getColumnTypes() {
            return new NamedPropertyType[]{
                    PropertyBuilder.create("id", PropertyType.Integer),
                    PropertyBuilder.create("description", PropertyType.String),
                    PropertyBuilder.create("is_active", PropertyType.Bool)
            };
        }

        @Override
        public String getTableName() {
            return "table2";
        }

        @Override
        public Class<?> getDataClass() {
            return TableData2.class;
        }
    }

    static class TableData2 extends AbstractTableData {

        final NamedIntegerProperty id;
        final NamedStringProperty description;
        final NamedBoolProperty isActive;

        public TableData2(AbstractTable<? extends AbstractTableData> table) {
            super(table);
            AbstractProperty<?> idType = get("id");
            if ((idType instanceof NamedIntegerProperty namedIntegerProperty)) {
                id = namedIntegerProperty;
            } else {
                throw new RuntimeException("id has wrong property type");
            }

            AbstractProperty<?> descriptionType = get("description");
            if ((descriptionType instanceof NamedStringProperty namedStringProperty)) {
                description = namedStringProperty;
            } else {
                throw new RuntimeException("description has wrong property type");
            }

            AbstractProperty<?> isActiveType = get("is_active");
            if ((isActiveType instanceof NamedBoolProperty namedBoolProperty)) {
                isActive = namedBoolProperty;
            } else {
                throw new RuntimeException("isActive has wrong property type");
            }
        }
    }


    //----------------------------------------------------------
    public static boolean deleteDirectoryRecursive(File dir) {
        if (!dir.exists()) {
            return false;
        }

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) { // Sicherstellen, dass der Verzeichnisinhalt nicht null ist
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursive(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        return dir.delete();
    }
}