package de.longri.database.properties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class NamedLocalDateTimeProperty extends AbstractProperty<LocalDateTime> {

    public static final DateTimeFormatter formatterGerman = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static DateTimeFormatter formatterUS = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");


    /**
     * Constructor to initialize the name and value of the property.
     *
     * @param type  The name of the property
     * @param value The initial value of the property
     */
    public NamedLocalDateTimeProperty(NamedPropertyType type, LocalDateTime value) {
        super(type, value);
    }

    @Override
    public boolean valueEquals(LocalDateTime value) {
        if (this.value == null && value == null) return true;
        if (this.value == null || value == null) return false;
        return this.value.truncatedTo(ChronoUnit.SECONDS)
                .equals(value.truncatedTo(ChronoUnit.SECONDS));
    }

    @Override
    public String toString() {
        // convert unix time to readable
        LocalDateTime triggerTime = value;
        String valueString = triggerTime == null ? "NULL" : formatterGerman.format(triggerTime);
        return "Property " + type.name + " =" + valueString;
    }

    public String getAsString() {
        if (value == null) return "NULL";
        if (value.equals(LocalDateTime.MIN)) return "LocalDateTime.MIN";
        if (value.equals(LocalDateTime.MAX)) return "LocalDateTime.MAX";
        return formatterGerman.format(value);
    }

    public void setFromString(String value) {
        switch (value) {
            case "NULL" -> this.setValue(null);
            case "LocalDateTime.MIN" -> this.setValue(LocalDateTime.MIN);
            case "LocalDateTime.MAX" -> this.setValue(LocalDateTime.MAX);
            case null, default -> {
                try {
                    assert value != null;
                    this.setValue(LocalDateTime.parse(value, formatterGerman));
                } catch (Exception e) {
                    this.setValue(LocalDateTime.parse(value, formatterUS));
                }
            }
        }
    }

    public LocalDateTime get() {
        return value;
    }
}
