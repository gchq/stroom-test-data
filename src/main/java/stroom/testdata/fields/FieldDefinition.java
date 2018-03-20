package stroom.testdata.fields;

import java.util.Optional;

public interface FieldDefinition<T> {

    /**
     * @return The next value for this field from the value supplier.
     * The value supplier may either be stateful, i.e. the next value is
     * dependant on values that cam before it or stateless, i.e. the next
     * value has no relation to previous values.
     */
    Optional<FieldValue<T>> getNext();

    /**
     * @return The name of the field
     */
    String getName();
}
