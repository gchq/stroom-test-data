package stroom.testdata.fields;

public class SimpleFieldValue<T> extends AbstractFieldValue<T> implements FieldValue<T> {

    private final T value;

    SimpleFieldValue(final FieldDefinition<T> fieldDefinition,
                     final FieldValue<?> parent,
                     final T value) {

        super(fieldDefinition, parent);
        this.value = value;
    }


    @Override
    public FieldDefinition getFieldDefinition() {
        return null;
    }

    @Override
    public T getValue() {
        return null;
    }

    @Override
    public String getFormattedValue() {
        return null;
    }
}
