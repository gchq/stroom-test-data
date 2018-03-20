package stroom.testdata;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.testdata.fields.Field;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Class for generating test data by constructing field definitions. Each {@link Field} definition
 * defines how the next value will be generated. A number of different types of predefined {@link Field}
 * types are available. For examples of how to use this class see test class TestDataGenerator
 */
public class DataGenerator {

    //TODO possibly add a list of first names and surnames so we can have fields like
    //firstName, surname, fullName, etc.

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGenerator.class);

    /**
     * Method to begin the process of building a test data generator definition and producing the test data.
     *
     */
    public static DataDefinitionBuilder definitionBuilder() {
        return new DataDefinitionBuilder();
    }

    /**
     * @return A pre-canned stream consumer that writes each string to System.out
     */
    public static Consumer<Stream<String>> getSystemOutConsumer() {
        return stringStream ->
                stringStream.forEach(System.out::println);
    }

    /**
     * @return A pre-canned stream consumer that writes each string to the file
     * at filePath
     */
    public static Consumer<Stream<String>> getFileOutputConsumer(final Path filePath) {

        Preconditions.checkNotNull(filePath);

        return stringStream -> {
            try {
                Files.write(filePath, (Iterable<String>) stringStream::iterator);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error writing to file %s",
                        filePath.toAbsolutePath().toString()), e);
            }
        };
    }




    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

}

