package stroom.testdata.fields;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class SimpleFieldDefinition<T> extends AbstractFieldDefinition implements FieldDefinition<T> {

    private final ValueSupplier<T> valueSupplier;
    private final double nullValueProbability;
    private final Function<FieldValue<T>, String> formatter;

    SimpleFieldDefinition(final String name,
                          final FieldDefinition<?> parent,
                          final ValueSupplier<T> valueSupplier,
                          final double nullValueProbability,
                          final Function<FieldValue<T>, String> formatter) {
        super(name, parent);
        this.valueSupplier = valueSupplier;
        this.nullValueProbability = nullValueProbability;
        this.formatter = formatter;
    }

    @Override
    public Optional<FieldValue<T>> getNext() {
        if (shouldReturnValue()) {
            return Optional.of(valueSupplier.supply());
        } else {
            return Optional.empty();
        }
    }



    @Override
    public String getName() {
        return super.getName();
    }

    private boolean shouldReturnValue() {
        //TODO based on nullValueProbability
    }

}
