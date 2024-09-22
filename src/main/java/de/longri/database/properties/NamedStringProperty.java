package de.longri.database.properties;

public class NamedStringProperty extends AbstractProperty<String> {

    /**
     * Constructor to initialize the name and value of the property.
     *
     * @param type  The name of the property
     * @param value The initial value of the property
     */
    public NamedStringProperty(NamedPropertyType type, String value) {
        super(type, value);
    }

    @Override
    public boolean valueEquals(String value) {
        return this.value.equals(value);
    }

    public String get() {
        return value;
    }
}
