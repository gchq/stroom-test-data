# stroom-test-data
A generic library for generating test data with configurable fields, formats and outputs.

The defintion of the test data is done in pure java so can easily be used in a JUnit test.

An example use is as follows:

``` java
DataGenerator.buildDefinition()
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
    .addFieldDefinition(DataGenerator.randomClassNamesField(
            "randomClassNamesField",
            0,
            3))
    .addFieldDefinition(DataGenerator.randomWordsField(
            "randomWordsField",
            0,
            3,
            Arrays.asList("attractive", "bald", "beautiful", "chubby", "drab", "elegant", "scruffy", "fit", "glamorous", "handsome", "unkempt")))
    .setDataWriter(FlatDataWriterBuilder.defaultCsvFormat())
    .consumedBy(DataGenerator.getFileOutputConsumer(Paths.get("/tmp/testdata.csv")))
    .multiThreaded()
    .rowCount(recCount)
    .generate();
```
