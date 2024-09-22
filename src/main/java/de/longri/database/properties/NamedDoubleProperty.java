package de.longri.database.properties;

import java.util.Objects;

public class NamedDoubleProperty extends AbstractProperty<Double> {
    /**
     * Constructor to initialize the name and value of the property.
     *
     * @param type  The name of the property
     * @param value The initial value of the property
     */
    public NamedDoubleProperty(NamedPropertyType type, Double value) {
        super(type, value);
    }

    @Override
    public boolean valueEquals(Double value) {
        return Objects.equals(this.value, value);
    }

    public double get() {
        return value;
    }
}
