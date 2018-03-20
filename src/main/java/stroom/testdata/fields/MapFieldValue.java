package stroom.testdata.fields;

import java.util.Map;

public class MapFieldValue extends AbstractFieldValue<Map<String, FieldValue<?>>> implements FieldValue<Map<String, FieldValue<?>>> {

    private final Map<String, FieldValue<?>> childValues;

    MapFieldValue(final FieldDefinition<Map<String, FieldValue<?>>> fieldDefinition,
                  final FieldValue<?> parent,
                  final Map<String, FieldValue<?>> childValues) {

        super(fieldDefinition, parent);
        this.childValues = childValues;
    }

    @Override
    public FieldDefinition<Map<String, FieldValue<?>>> getFieldDefinition() {
        return super.getFieldDefinition();
    }

    @Override
    public Map<String, FieldValue<?>> getValue() {
        return childValues;
    }

    @Override
    public String getFormattedValue() {
        return null;
    }
}
