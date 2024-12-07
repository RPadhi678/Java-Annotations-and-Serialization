// Rahul Padhi 
// ECS 160

package javacson;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import javacson.Annotations.CsonName;

public class StudentTest {

    private CsonSerializationError finalAssertThrows;

    // Test case for serializing mixed object types in a single list.
    @Test
    public void shouldSerializeMixedTypes() {
        // fooField and barField are used by the serializer via reflection
        class Foo {
            @SuppressWarnings("unused")
            public int fooField = 42;
        }

        class Bar {
            @SuppressWarnings("unused")
            public String barField = "Hello";
        }

        CsonObjectSerializer serializer = new CsonObjectSerializer();
        var result = serializer.serialize(List.of(new Foo(), new Bar()));

        // Check schema and data for mixed types
        String expected = "🍽Foo🥣fooField🧂int32"
                        + "🍽Bar🥣barField🧂string"
                        + "🔥"
                        + "🍲Foo🌶️42"
                        + "🍲Bar🌶️Hello";

        assertEquals(expected, result);
    }

    // Test case for detecting duplicate field names due to @CsonName annotations.
    @Test
    public void shouldThrowErrorOnDuplicateFieldNames() {
        class BadClass {
            @CsonName("duplicateField")
            public int field1 = 1;

            @CsonName("duplicateField")
            public String field2 = "duplicate";
        }

        CsonObjectSerializer serializer = new CsonObjectSerializer();

        // Expecting a CsonSerializationError due to duplicate field names in the schema
        finalAssertThrows = assertThrows(CsonSerializationError.class, () -> serializer.serialize(new BadClass()));
    }
}
