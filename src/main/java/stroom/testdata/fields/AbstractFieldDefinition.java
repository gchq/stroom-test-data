package stroom.testdata.fields;

public class AbstractFieldDefinition {

    private final String name;
    private final FieldDefinition<?> parent;

    AbstractFieldDefinition(final String name,
                            final FieldDefinition<?> parent) {
        this.name = name;
        this.parent = parent;
    }

    String getName() {
        return name;
    }
}
