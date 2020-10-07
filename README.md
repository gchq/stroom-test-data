# stroom-test-data
A generic library for generating test data with configurable fields, formats and outputs.

The defintion of the test data is done in pure java so can easily be used in a JUnit test.

## Example 

An example use is as follows:

``` java
DataGenerator.buildDefinition()
    .addFieldDefinition(DataGenerator.fakerField(
            "beer",
            faker -> faker.beer().name()))
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
            Arrays.asList(
                "attractive", "bald", "beautiful", "chubby", "drab", 
                "elegant", "scruffy", "fit", "glamorous", "handsome", "unkempt")))
    .setDataWriter(FlatDataWriterBuilder.defaultCsvFormat())
    .consumedBy(DataGenerator.getFileOutputConsumer(Paths.get("/tmp/testdata.csv")))
    .multiThreaded()
    .rowCount(recCount)
    .generate();
```

## Adding stroom-test-data to your Java project

Stroom-test-data is published on Bintray at [bintray.com/stroom/stroom/stroom-test-data](https://bintray.com/stroom/stroom/stroom-test-data). You will need to add the `stroom` repository to your build tool. For example if you are using Gradle you will need to set your repositories to something like this:

``` groovy
repositories {
    mavenLocal()
    jcenter()
    maven { url "https://dl.bintray.com/stroom/stroom" }
}
```

You can then add the dependency as follows:

``` groovy
testCompile 'stroom:stroom-test-data:v0.1.0'
```

## Faker

This library makes use of [Java-Faker](https://github.com/DiUS/java-faker) to allow a richer set of field data to be generated.
Java-Faker can be used to provide field values as follows:

```java
        return DataGenerator.buildDefinition()
                .addFieldDefinition(DataGenerator.fakerField(
                        "beer",
                        faker -> faker.beer().name()))
                .addFieldDefinition(DataGenerator.fakerField(
                        "carNumberPlate",
                        faker -> faker.regexify("[A-Z]{2}[0-9]{2} [A-Z]{3}")))
```

Simply provide a `Function<Faker, String>` that uses one of the faker methods to generate some data.
The same Faker instance is used for all values generated for a field.
