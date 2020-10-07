package stroom.testdata;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);

    @TempDir
    public Path tempDir;

    @Test
    public void testCsv_default() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(FlatDataWriterBuilder.defaultCsvFormat())
                .rowCount(recCount)
                .consumedBy(stringStream ->
                        stringStream.forEach(lines::add))
                .generate();

        //addition of header row
        Assertions.assertThat(lines).hasSize(recCount + 1);
        LOGGER.debug("Dumping generated rows");
        lines.forEach(System.out::println);
    }

    @Test
    public void testCsv_custom() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(FlatDataWriterBuilder.builder()
                        .delimitedBy("|")
                        .enclosedBy("\"")
                        .outputHeaderRow(false)
                        .build())
                .rowCount(recCount)
                .consumedBy(stringStream ->
                        stringStream.forEach(lines::add))
                .generate();

        //no header row so lines == recCount
        Assertions.assertThat(lines)
                .hasSize(recCount);
        LOGGER.debug("Dumping generated rows");
        lines.forEach(System.out::println);
    }

    @Test
    public void testMultiLineFileOutputConsumer() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Path outFile = getTempFile();
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(FlatDataWriterBuilder.defaultCsvFormat())
                .rowCount(recCount)
                .consumedBy(DataGenerator.getFileOutputConsumer(outFile))
                .generate();

        //addition of header row
        Assertions.assertThat(outFile)
                .isRegularFile();

        final int lineCount = dumpFile(outFile);

        Assertions.assertThat(lineCount).isEqualTo(recCount + 1);
    }

    @Test
    public void testSingleLineFileOutputConsumer() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Path outFile = getTempFile();
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(FlatDataWriterBuilder.defaultCsvFormat())
                .rowCount(recCount)
                .consumedBy(DataGenerator.getFileOutputConsumer(outFile, "|"))
                .generate();

        //addition of header row
        Assertions.assertThat(outFile)
                .isRegularFile();

        final int lineCount = dumpFile(outFile);

        Assertions.assertThat(lineCount).isEqualTo(1);
    }

    @Test
    public void testXmlElements_default() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(XmlElementsDataWriterBuilder.defaultXmlElementFormat())
                .rowCount(recCount)
                .consumedBy(stringStream ->
                        stringStream.forEach(lines::add))
                .generate();

        //addition of xml declaration, plus opening/closing root elements
        Assertions.assertThat(lines)
                .hasSize(recCount + 3);
        LOGGER.debug("Dumping generated rows");
        lines.forEach(System.out::println);
    }

    @Test
    public void testXmlElements_custom() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(XmlElementsDataWriterBuilder.builder()
                        .namespace("myNamespace")
                        .rootElementName("myRootElm")
                        .recordElementName("myRecordElm")
                        .build())
                .rowCount(recCount)
                .consumedBy(stringStream ->
                        stringStream.forEach(lines::add))
                .generate();

        //addition of xml declaration, plus opening/closing root elements
        Assertions.assertThat(lines)
                .hasSize(recCount + 3);
        LOGGER.debug("Dumping generated rows");
        lines.forEach(System.out::println);
    }

    @Test
    public void testXmlAttributes_default() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(XmlAttributesDataWriterBuilder.defaultXmlElementFormat())
                .rowCount(recCount)
                .consumedBy(stringStream ->
                        stringStream.forEach(lines::add))
                .generate();

        //addition of xml declaration, plus opening/closing root elements
        Assertions.assertThat(lines)
                .hasSize(recCount + 3);
        LOGGER.debug("Dumping generated rows");
        lines.forEach(System.out::println);
    }

    @Test
    public void testXmlAttributes_custom() {
        DataGenerator.DefinitionBuilder builder = buildBasicDefinition();

        int recCount = 10;
        Queue<String> lines = new ConcurrentLinkedQueue<>();
        builder
                .setDataWriter(XmlAttributesDataWriterBuilder.builder()
                        .namespace("myNamespace")
                        .rootElementName("myRootElm")
                        .recordElementName("myRecordElm")
                        .fieldValueElementName("myFieldValue")
                        .build())
                .rowCount(recCount)
                .consumedBy(stringStream ->
                        stringStream.forEach(lines::add))
                .generate();

        //addition of xml declaration, plus opening/closing root elements
        Assertions.assertThat(lines)
                .hasSize(recCount + 3);
        LOGGER.debug("Dumping generated rows");
        lines.forEach(System.out::println);
    }

    @Test
    public void testRandomWordsField_singleItem() {
        Field field = DataGenerator.randomWordsField(
                "myField",
                2,
                4,
                Collections.singletonList("MY_SINGLE_VALUE"));

        String val = field.getNext();

        System.out.println("val = " + val);
    }

    @Test
    public void testRandomWordsField_emptyList() {
        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> {
                    Field field = DataGenerator.randomWordsField(
                            "myField",
                            2,
                            4,
                            Collections.emptyList());

                    String val = field.getNext();
                })
                .withMessageContaining("wordList must have size greater than zero");
    }

    private DataGenerator.DefinitionBuilder buildBasicDefinition() {
        //start building a definition that uses all field types
        return DataGenerator.buildDefinition()
                .multiThreaded()
                .addFieldDefinition(DataGenerator.fakerField(
                        "carNumberPlate",
                        faker -> faker.regexify("[A-Z]{2}[0156][0-9] [A-Z]{3}")))
                .addFieldDefinition(DataGenerator.sequentialValueField(
                        "sequentialValueField",
                        Arrays.asList("One", "Two", "Three")))
                .addFieldDefinition(DataGenerator.randomValueField(
                        "randomValueField",
                        Arrays.asList("Red", "Green", "Blue")))
                .addFieldDefinition(DataGenerator.randomNumberedValueField(
                        "randomNumberedValueField",
                        "user-%s",
                        3))
                .addFieldDefinition(DataGenerator.sequentiallyNumberedValueField(
                        "sequentiallyNumberedValueField",
                        "user-%s",
                        5,
                        10))
                .addFieldDefinition(DataGenerator.sequentialNumberField(
                        "sequentialNumberField",
                        5,
                        10))
                .addFieldDefinition(DataGenerator.randomNumberField(
                        "randomNumberField",
                        1,
                        5))
                .addFieldDefinition(DataGenerator.randomIpV4Field("randomIpV4Field"))
                .addFieldDefinition(DataGenerator.randomDateTimeField(
                        "randomDateTimeField",
                        LocalDateTime.of(2016, 1, 1, 0, 0, 0),
                        LocalDateTime.of(2018, 1, 1, 0, 0, 0),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addFieldDefinition(DataGenerator.randomDateTimeField(
                        "randomDateTimeField2",
                        LocalDateTime.of(2016, 1, 1, 0, 0, 0),
                        LocalDateTime.of(2018, 1, 1, 0, 0, 0),
                        "yyyyMMdd"))
                .addFieldDefinition(DataGenerator.sequentialDateTimeField(
                        "sequentialDateTimeField",
                        LocalDateTime.of(2016, 1, 1, 0, 0, 0),
                        Duration.ofDays(1),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addFieldDefinition(DataGenerator.sequentialDateTimeField(
                        "sequentialDateTimeField2",
                        LocalDateTime.of(2016, 1, 1, 0, 0, 0),
                        Duration.ofDays(1),
                        "yyyyMMdd"))
                .addFieldDefinition(DataGenerator.uuidField("uuidField"))
                .addFieldDefinition(DataGenerator.randomWordsField(
                        "randomWordsField",
                        0,
                        3,
                        Arrays.asList("attractive", "bald", "beautiful", "chubby", "drab", "elegant", "scruffy", "fit", "glamorous", "handsome", "unkempt")));
    }

    private Path getTempFile() {
        try {
            Path file = Files.createTempFile(tempDir, "stroom-test-", "");
            LOGGER.info("Using file path {}", file.toAbsolutePath().normalize().toString());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int dumpFile(final Path file) {
        Objects.requireNonNull(file);
        Utils.checkArgument(Files.isReadable(file));
        try {
            try (Stream<String> stringStream = Files.lines(file)) {
                final AtomicInteger counter = new AtomicInteger(0);
                final String content = stringStream
                        .peek(str -> counter.incrementAndGet())
                        .collect(Collectors.joining("\n"));

                LOGGER.info("File {} contents:\n{}",
                        file.toAbsolutePath().normalize().toString(),
                        content);

                return counter.get();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}