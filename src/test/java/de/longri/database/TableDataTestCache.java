package de.longri.database;

import de.longri.database.table_data.AbstractCache;
import de.longri.database.table_data.AbstractTable;
import de.longri.database.table_data.AbstractTableDataEntry;
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.StoreBase;
import de.longri.utils.NamedObjectProperty;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableDataTestCache extends AbstractCache {

    Table1 table1 = new Table1();
    Table2 table2 = new Table2();
    Table3 table3 = new Table3();
    Table4 table4 = new Table4();
    Table5 table5 = new Table5();

    public TableDataTestCache(String cachePath) {
        super(cachePath);
    }

    @Override
    protected AbstractTable<AbstractTableDataEntry>[] getTables() {
        return new AbstractTable[]{table1, table2, table3, table4, table5};
    }

    static class Table1 extends AbstractTable<Table1_data> {

        @Override
        public String[] getColumnNames() {
            return new String[]{"id", "name", "value"};
        }

        @Override
        protected Table1_data create(ResultSet rs) throws SQLException {
            return new Table1_data(rs, this);
        }

        @Override
        protected Table1_data create(StoreBase storeBase) throws SQLException, NotImplementedException {
            return new Table1_data(storeBase, this);
        }

        @Override
        public String getTableName() {
            return "Table1";
        }
    }

    static class Table1_data extends AbstractTableDataEntry {

        public Table1_data(ResultSet resultSet, Table1 table1Data) throws SQLException {
            super(resultSet, table1Data);
        }

        public Table1_data(StoreBase storeBase, Table1 table1Data) throws SQLException, NotImplementedException {
            super(storeBase, table1Data);
        }

        @Override
        protected NamedObjectProperty createProperty(String name) {
            switch (name) {
                case "id":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
                case "name":
                    return new NamedObjectProperty.NamedStringProperty(name);
                case "value":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
            }
            throw new RuntimeException("Property with name " + name + " not found");
        }
    }

    static class Table2 extends AbstractTable<Table2_data> {

        @Override
        public String[] getColumnNames() {
            return new String[]{"id", "description", "is_active"};
        }

        @Override
        protected Table2_data create(ResultSet rs) throws SQLException {
            return new Table2_data(rs, this);
        }

        @Override
        protected Table2_data create(StoreBase storeBase) throws SQLException, NotImplementedException {
            return new Table2_data(storeBase, this);
        }

        @Override
        public String getTableName() {
            return "Table2";
        }
    }

    static class Table2_data extends AbstractTableDataEntry {

        public Table2_data(ResultSet resultSet, Table2 Table2Data) throws SQLException {
            super(resultSet, Table2Data);
        }

        public Table2_data(StoreBase storeBase, Table2 Table2Data) throws SQLException, NotImplementedException {
            super(storeBase, Table2Data);
        }

        @Override
        protected NamedObjectProperty createProperty(String name) {
            switch (name) {
                case "id":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
                case "description":
                    return new NamedObjectProperty.NamedStringProperty(name);
                case "is_active":
                    return new NamedObjectProperty.NamedBoolProperty(name);
            }
            throw new RuntimeException("Property with name " + name + " not found");
        }
    }

    static class Table3 extends AbstractTable<Table3_data> {

        @Override
        public String[] getColumnNames() {
            return new String[]{"id", "user_id", "amount"};
        }

        @Override
        protected Table3_data create(ResultSet rs) throws SQLException {
            return new Table3_data(rs, this);
        }

        @Override
        protected Table3_data create(StoreBase storeBase) throws SQLException, NotImplementedException {
            return new Table3_data(storeBase, this);
        }

        @Override
        public String getTableName() {
            return "Table3";
        }
    }

    static class Table3_data extends AbstractTableDataEntry {

        public Table3_data(ResultSet resultSet, Table3 Table3Data) throws SQLException {
            super(resultSet, Table3Data);
        }

        public Table3_data(StoreBase storeBase, Table3 Table3Data) throws SQLException, NotImplementedException {
            super(storeBase, Table3Data);
        }

        @Override
        protected NamedObjectProperty createProperty(String name) {
            switch (name) {
                case "id":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
                case "user_id":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
                case "amount":
                    return new NamedObjectProperty.NamedDoubleProperty(name);
            }
            throw new RuntimeException("Property with name " + name + " not found");
        }
    }

    static class Table4 extends AbstractTable<Table4_data> {

        @Override
        public String[] getColumnNames() {
            return new String[]{"id", "timestamp", "status"};
        }

        @Override
        protected Table4_data create(ResultSet rs) throws SQLException {
            return new Table4_data(rs, this);
        }

        @Override
        protected Table4_data create(StoreBase storeBase) throws SQLException, NotImplementedException {
            return new Table4_data(storeBase, this);
        }

        @Override
        public String getTableName() {
            return "Table4";
        }
    }

    static class Table4_data extends AbstractTableDataEntry {

        public Table4_data(ResultSet resultSet, Table4 Table4Data) throws SQLException {
            super(resultSet, Table4Data);
        }

        public Table4_data(StoreBase storeBase, Table4 Table4Data) throws SQLException, NotImplementedException {
            super(storeBase, Table4Data);
        }

        @Override
        protected NamedObjectProperty createProperty(String name) {
            switch (name) {
                case "id":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
                case "timestamp":
                    return new NamedObjectProperty.NamedLocalDateTimeProperty(name);
                case "status":
                    return new NamedObjectProperty.NamedStringProperty(name);
            }
            throw new RuntimeException("Property with name " + name + " not found");
        }
    }

    static class Table5 extends AbstractTable<Table5_data> {

        @Override
        public String[] getColumnNames() {
            return new String[]{"id", "title", "content"};
        }

        @Override
        protected Table5_data create(ResultSet rs) throws SQLException {
            return new Table5_data(rs, this);
        }

        @Override
        protected Table5_data create(StoreBase storeBase) throws SQLException, NotImplementedException {
            return new Table5_data(storeBase, this);
        }

        @Override
        public String getTableName() {
            return "Table5";
        }
    }

    static class Table5_data extends AbstractTableDataEntry {

        public Table5_data(ResultSet resultSet, Table5 Table5Data) throws SQLException {
            super(resultSet, Table5Data);
        }

        public Table5_data(StoreBase storeBase, Table5 Table5Data) throws SQLException, NotImplementedException {
            super(storeBase, Table5Data);
        }

        @Override
        protected NamedObjectProperty createProperty(String name) {
            switch (name) {
                case "id":
                    return new NamedObjectProperty.NamedIntegerProperty(name);
                case "title":
                    return new NamedObjectProperty.NamedStringProperty(name);
                case "content":
                    return new NamedObjectProperty.NamedStringProperty(name);
            }
            throw new RuntimeException("Property with name " + name + " not found");
        }
    }
}
