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

import de.longri.database.cache.AbstractCache;
import de.longri.database.cache.AbstractTable;
import de.longri.database.cache.AbstractTableData;
import de.longri.database.properties.NamedPropertyType;
import de.longri.database.properties.PropertyBuilder;
import de.longri.database.properties.PropertyType;

public class TableDataTestCache extends AbstractCache {

    Table1 table1;
    Table2 table2;
    Table3 table3;
    Table4 table4;
    Table5 table5;

    public TableDataTestCache(String cachePath) {
        super(cachePath);
    }

    @Override
    protected AbstractTable<AbstractTableData>[] getTables() {
        if (table1 == null) {
            table1 = new Table1();
            table2 = new Table2();
            table3 = new Table3();
            table4 = new Table4();
            table5 = new Table5();
        }
        return new AbstractTable[]{table1, table2, table3, table4, table5};
    }

    public static class Table1 extends AbstractTable<Table1_data> {

        @Override
        public Class<?> getDataClass() {
            return Table1_data.class;
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
            return "Table1";
        }
    }

    public static class Table1_data extends AbstractTableData {
        public Table1_data(AbstractTable<? extends AbstractTableData> table) {
            super(table);
        }
    }

    public static class Table2 extends AbstractTable<Table2_data> {

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
            return "Table2";
        }

        @Override
        public Class<?> getDataClass() {
            return Table2_data.class;
        }
    }

    public static class Table2_data extends AbstractTableData {

        public Table2_data(AbstractTable<? extends AbstractTableData> table) {
            super(table);
        }
    }

    public static class Table3 extends AbstractTable<Table3_data> {

        @Override
        public NamedPropertyType[] getColumnTypes() {
            return new NamedPropertyType[]{
                    PropertyBuilder.create("id", PropertyType.Integer),
                    PropertyBuilder.create("user_id", PropertyType.Integer),
                    PropertyBuilder.create("amount", PropertyType.Double)
            };
        }

        @Override
        public String getTableName() {
            return "Table3";
        }

        @Override
        public Class<?> getDataClass() {
            return Table3_data.class;
        }
    }

    public static class Table3_data extends AbstractTableData {

        public Table3_data(AbstractTable<? extends AbstractTableData> table) {
            super(table);
        }
    }

    public static class Table4 extends AbstractTable<Table4_data> {

        @Override
        public NamedPropertyType[] getColumnTypes() {
            return new NamedPropertyType[]{
                    PropertyBuilder.create("id", PropertyType.Integer),
                    PropertyBuilder.create("user_id", PropertyType.LocalDateTime),
                    PropertyBuilder.create("amount", PropertyType.String)
            };
        }

        @Override
        public String getTableName() {
            return "Table4";
        }

        @Override
        public Class<?> getDataClass() {
            return Table4_data.class;
        }
    }

    public static class Table4_data extends AbstractTableData {

        public Table4_data(AbstractTable<? extends AbstractTableData> table) {
            super(table);
        }
    }

    public static class Table5 extends AbstractTable<Table5_data> {

        @Override
        public NamedPropertyType[] getColumnTypes() {
            return new NamedPropertyType[]{
                    PropertyBuilder.create("id", PropertyType.Integer),
                    PropertyBuilder.create("title", PropertyType.String),
                    PropertyBuilder.create("content", PropertyType.String)
            };
        }

        @Override
        public String getTableName() {
            return "Table5";
        }

        @Override
        public Class<?> getDataClass() {
            return Table5_data.class;
        }
    }

    public static class Table5_data extends AbstractTableData {

        public Table5_data(AbstractTable<? extends AbstractTableData> table) {
            super(table);
        }
    }
}
