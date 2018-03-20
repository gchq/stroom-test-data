package stroom.testdata;

import java.util.stream.Stream;

public interface DataWriter<T> {

    Stream<T> mapRecords(final Stream<Record> recordStream);
}
