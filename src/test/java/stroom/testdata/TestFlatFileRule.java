package stroom.testdata;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestFlatFileRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFlatFileRule.class);

    private static final int NUMBER_FILES = 3;
    private static final int ROW_COUNT = 8;

    @ClassRule
    public static final FlatFileTestDataRule testDataRule = FlatFileTestDataRule.withTempDirectory()
            .testDataGenerator(TestFlatFileRule::generateTestData)
            .numberOfFiles(NUMBER_FILES)
            .build();

    private static void generateTestData(final Consumer<String> writer) {
        DataGenerator.buildDefinition()
                .addFieldDefinition(DataGenerator.randomValueField("Species",
                        Arrays.asList("spider", "whale", "dog", "tiger", "monkey", "lion", "woodlouse", "honey-badger")))
                .addFieldDefinition(DataGenerator.randomValueField("Continent",
                        Arrays.asList("europe", "asia", "america", "antarctica", "africa", "australia")))
                .addFieldDefinition(DataGenerator.randomValueField("ObservationTeam",
                        Arrays.asList("alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel")))
                .addFieldDefinition(DataGenerator.randomDateTimeField("Time",
                        LocalDateTime.of(2016, 1, 1, 0, 0, 0),
                        LocalDateTime.of(2018, 1, 1, 0, 0, 0),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setDataWriter(FlatDataWriterBuilder.defaultCsvFormat())
                .rowCount(ROW_COUNT)
                .consumedBy(writer)
                .generate();
    }

    @Test
    public void testFilesCreated() throws IOException {
        // The rule keeps trak of the files it has created
        assertEquals(NUMBER_FILES, testDataRule.getDataFiles().size());

        // The rule exposes the folder it has created so you can find the files with a walk
        Files.walk(Paths.get(testDataRule.getFolder().getAbsolutePath()))
                .filter(p -> !Files.isDirectory(p))
                .peek(p -> assertTrue(testDataRule.getDataFiles().contains(p)))
                .forEach(p -> {
                    LOGGER.info("Path Created {}", p);

                    try (final Stream<String> stream = Files.lines(p)) {
                        stream.forEach(LOGGER::debug);
                    } catch (IOException e) {
                        fail(e.getLocalizedMessage());
                    }
                });


    }
}
