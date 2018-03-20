package stroom.testdata;

import stroom.testdata.fields.FieldDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Holds all the definitions for the fields and values that will be generated,
 * how many will be generated, how they will be formatted, etc.
 */
public class DataDefinition {

    private List<FieldDefinition<?>> fieldDefinitions = new ArrayList<>();
    private Consumer<Stream<String>> rowStreamConsumer;
    private int rowCount = 1;
    private DataWriter dataWriter;
    private boolean isParallel = false;

    DataDefinition(final List<FieldDefinition<?>> fieldDefinitions,
                   final Consumer<Stream<String>> rowStreamConsumer,
                   final int rowCount,
                   final DataWriter dataWriter,
                   final boolean isParallel) {

        this.fieldDefinitions = fieldDefinitions;
        this.rowStreamConsumer = rowStreamConsumer;
        this.rowCount = rowCount;
        this.dataWriter = dataWriter;
        this.isParallel = isParallel;
    }

    public void generate() {
        //convert our stream of data records into a stream of strings that possibly
        //includes adding things like header/footer rows, tags, delimiters, etc.
        final Stream<String> rowStream = dataWriter.mapRecords(fieldDefinitions, generateDataRows());
        rowStreamConsumer.accept(rowStream);

    }

    private Stream<Record> generateDataRows() {

        Function<Integer, Record> toRecordMapper = integer -> {
            List<String> values = fieldDefinitions.stream()
                    .map(field -> {
                        try {
                            return field.getNext();
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Error getting next value for field %s, %s",
                                    field.getName(), e.getMessage()), e);
                        }
                    })
                    .collect(Collectors.toList());
            return new Record(fieldDefinitions, values);
        };

        IntStream stream = IntStream.rangeClosed(1, rowCount);

        if (isParallel) {
            stream = stream.parallel();
        } else {
            stream = stream.sequential();
        }

        return stream
                .boxed()
                .map(toRecordMapper);
    }
}
