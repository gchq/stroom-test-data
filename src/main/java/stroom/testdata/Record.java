package stroom.testdata;

import stroom.testdata.fields.Field;
import stroom.testdata.fields.FieldValue;

import java.util.List;

/**
 * Top level record
 */
public class Record {

    final List<FieldValue<?>> fieldDefinitions;

    public Record(List<Field> fieldDefinitions, List<String> values) {
        this.fieldDefinitions = fieldDefinitions;
        this.values = values;
    }

    public List<Field> getFieldDefinitions() {
        return fieldDefinitions;
    }

    public List<String> getValues() {
        return values;
    }
}
