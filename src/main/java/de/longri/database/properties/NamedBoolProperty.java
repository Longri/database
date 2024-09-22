package de.longri.database.properties;

public class NamedBoolProperty extends AbstractProperty<Boolean> {

    /**
     * Constructor to initialize the name and value of the property.
     *
     * @param type  The name of the property
     * @param value The initial value of the property
     */
    public NamedBoolProperty(NamedPropertyType type, Boolean value) {
        super(type, value);
    }

    @Override
    public boolean valueEquals(Boolean value) {
        return this.value == value;
    }

    public boolean get() {
        return value;
    }
}
