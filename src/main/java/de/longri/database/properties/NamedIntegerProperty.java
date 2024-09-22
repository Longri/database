package de.longri.database.properties;

import java.util.Objects;

public class NamedIntegerProperty extends AbstractProperty<Integer> {

    /**
     * Constructor to initialize the name and value of the property.
     *
     * @param type  The name of the property
     * @param value The initial value of the property
     */
    public NamedIntegerProperty(NamedPropertyType type, Integer value) {
        super(type, value);
    }

    @Override
    public boolean valueEquals(Integer value) {
        return Objects.equals(this.value, value);
    }

    public int get() {
        return value;
    }
}
