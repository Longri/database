package de.longri.database.properties;

public enum PropertyType {
    Bool(0),
    Integer(1),
    String(2),
    LocalDateTime(3),
    Double(4);

    public final int id;

    PropertyType(int id) {
        this.id = id;
    }

    public static PropertyType fromId(int id) {
        for (PropertyType value : PropertyType.values()) {
            if (value.id == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("Ungültiger Wert: " + id);
    }
}
