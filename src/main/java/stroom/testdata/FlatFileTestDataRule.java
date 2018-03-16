package stroom.testdata;

import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.fail;

public class FlatFileTestDataRule implements TestRule {

    private final TemporaryFolder folder;
    private final int numberOfFiles;
    private final Consumer<Consumer<String>> testDataGenerator;
    private final List<Path> dataFiles = new ArrayList<>();

    private FlatFileTestDataRule(final Builder builder) {
        this.folder = builder.folder;
        this.numberOfFiles = builder.numberOfFiles;
        this.testDataGenerator = builder.testDataGenerator;
    }

    @Override
    public Statement apply(final Statement statement, Description description) {

        return folder.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    before();
                    statement.evaluate();
                } finally {
                    after();
                }
            }
        }, description);
    }

    public File getFolder() {
        return folder.getRoot();
    }

    public List<Path> getDataFiles() {
        return dataFiles;
    }

    private void before() throws IOException {
        for (int x=0; x<numberOfFiles; x++) {
            final String filename = String.format("%s.csv", UUID.randomUUID().toString());
            final File file = this.folder.newFile(filename);

            try (final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {

                testDataGenerator.accept(s -> {
                    try {
                        bw.write(String.format("%s\n", s));
                    } catch (final IOException e) {
                        fail(e.getLocalizedMessage());
                    }
                });
            }

            dataFiles.add(file.toPath());
        }
    }

    private void after() {

    }

    public static Builder withTempDirectory() {

        final TemporaryFolder folder = new TemporaryFolder();

        return new Builder(folder);
    }

    public static final class Builder {

        private final TemporaryFolder folder;
        private int numberOfFiles = 10;
        private Consumer<Consumer<String>> testDataGenerator;

        private Builder(final TemporaryFolder folder) {
            this.folder = folder;
        }

        public Builder numberOfFiles(final int value) {
            this.numberOfFiles = value;
            return this;
        }

        public Builder testDataGenerator(final Consumer<Consumer<String>> value) {
            this.testDataGenerator = value;
            return this;
        }

        public FlatFileTestDataRule build() {
            Objects.requireNonNull(this.testDataGenerator, "Test Data Generator Must be Specified");
            return new FlatFileTestDataRule(this);
        }
    }
}
