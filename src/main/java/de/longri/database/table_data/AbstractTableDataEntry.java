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
package de.longri.database.table_data;

import de.longri.database.Abstract_Database;
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.StoreBase;
import de.longri.utils.NamedObjectProperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class AbstractTableDataEntry {

    public final ArrayList<NamedObjectProperty> properties = new ArrayList<>();
    public final AbstractTable TABLE;

    protected AbstractTableDataEntry(ArrayList<NamedObjectProperty> properties, AbstractTable table) {
        TABLE = table;

        for (String col : TABLE.getColumnNames()) {
            NamedObjectProperty prop = createProperty(col);

            if (prop == null) {
                throw new RuntimeException("Cant't create property for column: " + col);
            }

            NamedObjectProperty newProp = getProperty(properties, col);
            if (newProp != null)
                prop.set(newProp.getValue());
            this.properties.add(prop);
        }
    }

    protected AbstractTableDataEntry(ResultSet resultSet, AbstractTable table) throws SQLException {
        TABLE = table;
        for (String col : TABLE.getColumnNames()) {
            NamedObjectProperty prop = createProperty(col);

            if (prop == null) {
                throw new RuntimeException("Cant't create property for column: " + col);
            }

            if (prop instanceof NamedObjectProperty.NamedBoolProperty) {
                prop.set(resultSet.getBoolean(col));
            } else if (prop instanceof NamedObjectProperty.NamedIntegerProperty) {
                prop.set(resultSet.getInt(col));
            } else if (prop instanceof NamedObjectProperty.NamedStringProperty) {
                prop.set(resultSet.getString(col));
            } else if (prop instanceof NamedObjectProperty.NamedDoubleProperty) {
                prop.set(resultSet.getDouble(col));
            } else if (prop instanceof NamedObjectProperty.NamedLocalDateTimeProperty) {
                LocalDateTime dbDate = Abstract_Database.getDateTime(resultSet.getString(col));
                prop.set(dbDate);
            }
            properties.add(prop);
        }
    }


    protected AbstractTableDataEntry(StoreBase bitStore, AbstractTable table) throws SQLException, NotImplementedException {
        TABLE = table;
        int propertyCount = bitStore.readInt();
        for (int i = 0; i < propertyCount; i++) {
            properties.add(readProperty(bitStore));
        }
    }


    private NamedObjectProperty getProperty(ArrayList<NamedObjectProperty> properties, String name) {
        for (NamedObjectProperty prop : properties) {
            if (prop.getName().equals(name))
                return prop;
        }
        return null;
    }

    private NamedObjectProperty readProperty(StoreBase bitStore) throws NotImplementedException {
        int propertyType = bitStore.readInt();
        String propertyName = bitStore.readString();
        NamedObjectProperty prop = createProperty(propertyName);

        if (prop == null)
            throw new RuntimeException("Property " + propertyName + " not found for table " + TABLE.getTableName());

        if (propertyType == NamedBoolProperty) {
            prop.set(bitStore.readBool());
        } else if (propertyType == NamedIntegerProperty) {
            prop.set(bitStore.readInt());
        } else if (propertyType == NamedStringProperty) {
            prop.set(bitStore.readString());
        } else if (propertyType == NamedDoubleProperty) {
            String value = bitStore.readString();
            prop.set(Double.valueOf(value));
        } else if (propertyType == NamedLocalDateTimeProperty) {
            ((NamedObjectProperty.NamedLocalDateTimeProperty) prop).setFromString(bitStore.readString());
        } else {
            throw new RuntimeException("Unknown property type " + propertyType);
        }
        return prop;
    }

    protected abstract NamedObjectProperty createProperty(String name);

    public NamedObjectProperty getProperty(String name) {
        if (properties == null || properties.isEmpty()) throw new RuntimeException("properties not initialized");
        for (NamedObjectProperty prop : properties) {
            if (prop.getName().equals(name))
                return prop;
        }
        throw new RuntimeException("Property " + name + " not found for table " + TABLE.getTableName());
    }

    public static final int NamedBoolProperty = 0;
    public static final int NamedIntegerProperty = 1;
    public static final int NamedStringProperty = 2;
    public static final int NamedLocalDateTimeProperty = 3;
    public static final int NamedDoubleProperty = 4;

    public void serialize(StoreBase bitStore) throws NotImplementedException {
        bitStore.write(properties.size());
        for (NamedObjectProperty prop : properties) {
            if (prop instanceof NamedObjectProperty.NamedBoolProperty boolProperty) {
                bitStore.write(NamedBoolProperty);
                bitStore.write(prop.getName());
                bitStore.write(boolProperty.get());
            } else if (prop instanceof NamedObjectProperty.NamedIntegerProperty intProperty) {
                bitStore.write(NamedIntegerProperty);
                bitStore.write(prop.getName());
                bitStore.write(intProperty.get());
            } else if (prop instanceof NamedObjectProperty.NamedStringProperty stringProperty) {
                bitStore.write(NamedStringProperty);
                bitStore.write(prop.getName());
                bitStore.write(stringProperty.get());
            } else if (prop instanceof NamedObjectProperty.NamedDoubleProperty doubleProperty) {
                bitStore.write(NamedDoubleProperty);
                bitStore.write(prop.getName());
                bitStore.write(Double.toString(doubleProperty.get()));
            } else if (prop instanceof NamedObjectProperty.NamedLocalDateTimeProperty datetimeProperty) {
                bitStore.write(NamedLocalDateTimeProperty);
                bitStore.write(prop.getName());

                String obj = null;
                try {
                    obj = datetimeProperty.getAsString();
                } catch (Exception e) {
                    Object obk = datetimeProperty.get();
                    if (obk instanceof String)
                        obj = (String) obk;
                    else
                        obj = "NULL";
                }
                bitStore.write(obj);
            } else {
                if (prop == null) throw new RuntimeException("Property NULL at class: " + this.getClass());
                throw new RuntimeException("Unknown property type " + prop.getClass().getName());
            }

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTableDataEntry that = (AbstractTableDataEntry) o;
        if (properties.size() != that.properties.size()) return false;
        for (int i = 0; i < properties.size(); i++) {
            if (!properties.get(i).equals(that.properties.get(i))) {
                return false;
            }
        }
        return true;
    }

}
