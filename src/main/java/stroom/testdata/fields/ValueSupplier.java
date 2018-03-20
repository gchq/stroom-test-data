package stroom.testdata.fields;

public interface ValueSupplier<T> {

    /**
     * Will ALWAYS return a non-null value. The {@link FieldDefinition} is responsible for whether this method is
     * called or not.
     * @return
     */
    FieldValue<T> supply();
}
