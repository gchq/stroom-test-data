package stroom.testdata.fields;

public interface FieldValue<T> {


    FieldDefinition getFieldDefinition();

    T getValue();

    String getFormattedValue();

    String toString();
}
