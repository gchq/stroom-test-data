package stroom.testdata.fields;

public class AbstractFieldValue<T> {

    private final FieldDefinition<T> fieldDefinition;
    private final FieldValue<?> parent;

    AbstractFieldValue(final FieldDefinition<T> fieldDefinition, final FieldValue<?> parent) {
        this.fieldDefinition = fieldDefinition;
        this.parent = parent;
    }

    FieldDefinition<T> getFieldDefinition() {
        return fieldDefinition;
    }

    FieldValue<?> getParent() {
        return parent;
    }
}
