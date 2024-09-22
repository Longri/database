package de.longri.database.properties;

public class PropertyBuilder {

    public static AbstractProperty<?> create(NamedPropertyType type) {

        return switch (type.type) {
            case Bool -> new NamedBoolProperty(type, false);
            case Double -> new NamedDoubleProperty(type, 0.0);
            case Integer -> new NamedIntegerProperty(type, 0);
            case LocalDateTime -> new NamedLocalDateTimeProperty(type, null);
            case String -> new NamedStringProperty(type, "");
        };
    }

    public static NamedPropertyType create(String name, PropertyType type) {
        return new NamedPropertyType(name, type);
    }
}
