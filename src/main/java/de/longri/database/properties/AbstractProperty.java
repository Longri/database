package de.longri.database.properties;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The AbstractProperty class represents a generic property that
 * contains a name, a value, and a change state.
 *
 * @param <T> The type of the value held by this property
 */
public abstract class AbstractProperty<T> {

    // An atomic boolean variable indicating whether the value has changed
    AtomicBoolean changed = new AtomicBoolean(false);

    // The value of this property
    T value;

    // The name and type of this property
    final NamedPropertyType type;

    /**
     * Constructor to initialize the name and value of the property.
     *
     * @param type  The name and type of the property
     * @param value The initial value of the property
     */
    public AbstractProperty(NamedPropertyType type, T value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the current value of the property.
     *
     * @return The current value of the property
     */
    public T getValue() {
        synchronized (changed) {
            return value;
        }
    }

    /**
     * Sets the value of this property to the specified value.
     * This will mark this property as changed if the value has changed!
     *
     * @param value The new value of the property
     */
    public void setValue(T value) {
        synchronized (changed) {
            if (valueEquals(value)) return;
            changed.set(true);
            this.value = value;
        }
    }

    /**
     * Returns the name of this property.
     *
     * @return The name of the property
     */
    public String getName() {
        return type.name;
    }

    /**
     * Checks if the value of this property has changed.
     *
     * @return true if the value has changed, otherwise false
     */
    public boolean isChanged() {
        synchronized (changed) {
            return changed.get();
        }
    }

    /**
     * Returns a string representation of this property.
     *
     * @return A string representation of this property
     */
    @Override
    public String toString() {
        return "Property " + type.name + " =" + value;
    }

    public abstract boolean valueEquals(T value);

    public PropertyType getType() {
        return type.type;
    }

    public NamedPropertyType getNamedType() {
        return type;
    }

    //------------------------------------------------------------------------
    // static helper


}
