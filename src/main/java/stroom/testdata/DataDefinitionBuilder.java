package stroom.testdata;

import com.google.common.base.Preconditions;
import stroom.testdata.fields.Field;
import stroom.testdata.fields.FieldDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DataDefinitionBuilder {

    private List<FieldDefinition<?>> fieldDefinitions = new ArrayList<>();
    private Consumer<Stream<String>> rowStreamConsumer;
    private int rowCount = 1;
    private DataWriter dataWriter;
    private boolean isParallel = false;

    public DataDefinitionBuilder addFieldDefinition(final Field fieldDefinition) {
        boolean isNamedAlreadyUsed = fieldDefinitions.stream()
                .map(Field::getName)
                .anyMatch(Predicate.isEqual(fieldDefinition.getName()));
        Preconditions.checkArgument(!isNamedAlreadyUsed, "Name [%s] is already in use", fieldDefinition.getName());

        fieldDefinitions.add(Preconditions.checkNotNull(fieldDefinition));
        return this;
    }

    public DataDefinitionBuilder consumedBy(final Consumer<Stream<String>> rowStreamConsumer) {
        this.rowStreamConsumer = Preconditions.checkNotNull(rowStreamConsumer);
        return this;
    }

    public DataDefinitionBuilder setDataWriter(final DataWriter dataWriter) {
        this.dataWriter = Preconditions.checkNotNull(dataWriter);
        return this;
    }

    public DataDefinitionBuilder rowCount(final int rowCount) {
        Preconditions.checkArgument(rowCount > 0, "rowCount must be > 0");
        this.rowCount = rowCount;
        return this;
    }

    public DataDefinitionBuilder multiThreaded() {
        this.isParallel = true;
        return this;
    }

    public DataDefinition build() {
        if (fieldDefinitions.isEmpty()) {
            throw new RuntimeException("No field definitions defined");
        }
        if (rowStreamConsumer == null) {
            throw new RuntimeException("No consumer defined");
        }
        if (dataWriter == null) {
            //default to CSV
            dataWriter = FlatDataWriterBuilder.defaultCsvFormat();
        }
        return new DataDefinition(fieldDefinitions, rowStreamConsumer, rowCount, dataWriter, isParallel);
    }

    /**
     * Convenience method to immediately generate without getting a {@link DataDefinition} instance first
     */
    public void generate() {
        build().generate();
    }
}
