package de.longri.database.cache;


import de.longri.database.Abstract_Database;
import de.longri.database.properties.*;
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.StoreBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class AbstractTableData {

    public final ArrayList<AbstractProperty<?>> columns = new ArrayList<>();
    public final AbstractTable<? extends AbstractTableData> TABLE;
    private boolean IS_NEW_ROW = false;

    public AbstractTableData(AbstractTable<? extends AbstractTableData> table) {
        initialColumns(table);
        TABLE = table;
        IS_NEW_ROW = true;
    }

    protected void setFromResultSet(ResultSet resultSet) throws SQLException {
        for (AbstractProperty<?> col : columns) {
            if (col instanceof NamedBoolProperty boolProperty) {
                boolProperty.setValue(resultSet.getBoolean(col.getName()));
            } else if (col instanceof NamedIntegerProperty intProperty) {
                intProperty.setValue(resultSet.getInt(col.getName()));
            } else if (col instanceof NamedStringProperty stringProperty) {
                stringProperty.setValue(resultSet.getString(col.getName()));
            } else if (col instanceof NamedDoubleProperty doubleProperty) {
                doubleProperty.setValue(resultSet.getDouble(col.getName()));
            } else if (col instanceof NamedLocalDateTimeProperty datetimeProperty) {
                LocalDateTime dbDate = Abstract_Database.getDateTime(resultSet.getString(col.getName()));
                datetimeProperty.setValue(dbDate);
            }
        }
        IS_NEW_ROW = false;
    }

    protected void setFromBitStore(StoreBase bitStore) throws NotImplementedException {
        int propertyCount = bitStore.readInt();
        for (int i = 0; i < propertyCount; i++) {
            int propertyType = bitStore.readInt();
            PropertyType type = PropertyType.fromId(propertyType);
            String propertyName = bitStore.readString();
            AbstractProperty<?> col = getColumn(propertyName);
            if (type != col.getType())
                throw new RuntimeException("Property type of stored column " + propertyName + "is not equal to type of column in table " + TABLE.getTableName() + "!");
            switch (col) {
                case NamedBoolProperty boolProperty -> boolProperty.setValue(bitStore.readBool());
                case NamedIntegerProperty intProperty -> intProperty.setValue(bitStore.readInt());
                case NamedStringProperty stringProperty -> stringProperty.setValue(bitStore.readString());
                case NamedDoubleProperty doubleProperty -> {
                    String value = bitStore.readString();
                    doubleProperty.setValue(Double.valueOf(value));
                }
                case NamedLocalDateTimeProperty datetimeProperty -> {
                    LocalDateTime dbDate = Abstract_Database.getDateTime(bitStore.readString());
                    datetimeProperty.setValue(dbDate);
                }
                default -> {
                }
            }
        }
        IS_NEW_ROW = false;
    }

    private void initialColumns(AbstractTable<? extends AbstractTableData> table) {
        for (NamedPropertyType type : table.getColumnTypes()) {
            columns.add(PropertyBuilder.create(type));
        }
    }

    public AbstractProperty<?> get(String name) {
        for (AbstractProperty<?> column : columns) {
            if (column.getName().equals(name))
                return column;
        }
        throw new RuntimeException("can't find column with name: " + name);
    }

    private AbstractProperty<?> getColumn(String name) {
        for (AbstractProperty<?> col : columns) {
            if (col.getName().equals(name))
                return col;
        }
        throw new RuntimeException("can't find column with name: " + name);
    }

    public void serialize(StoreBase bitStore) throws NotImplementedException {
        bitStore.write(columns.size());
        for (AbstractProperty<?> prop : columns) {
            bitStore.write(prop.getType().ordinal());
            bitStore.write(prop.getName());
            switch (prop) {
                case NamedBoolProperty boolProperty -> bitStore.write(boolProperty.get());
                case NamedIntegerProperty intProperty -> bitStore.write(intProperty.get());
                case NamedStringProperty stringProperty -> bitStore.write(stringProperty.get());
                case NamedDoubleProperty doubleProperty -> bitStore.write(Double.toString(doubleProperty.get()));
                case NamedLocalDateTimeProperty datetimeProperty -> bitStore.write(datetimeProperty.getAsString());
                default -> throw new RuntimeException("Unknown property type " + prop.getClass().getName());
            }
        }
    }

    public boolean isNewEntry() {
        return IS_NEW_ROW;
    }
}
