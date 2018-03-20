package stroom.testdata.fields;

import java.util.List;

public class ListFieldValue extends AbstractFieldValue<List<FieldValue<?>>> implements FieldValue<List<FieldValue<?>>> {

    private final List<FieldValue<?>> childValues;

    ListFieldValue(final FieldDefinition<List<FieldValue<?>>> fieldDefinition,
                   final FieldValue<?> parent,
                   final List<FieldValue<?>> childValues) {

        super(fieldDefinition, parent);
        this.childValues = childValues;
    }

    @Override
    public FieldDefinition<List<FieldValue<?>>> getFieldDefinition() {
        return super.getFieldDefinition();
    }

    @Override
    public List<FieldValue<?>> getValue() {
        return childValues;
    }

    @Override
    public String getFormattedValue() {
        return null;
    }
}
