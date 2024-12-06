package javacson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static javacson.CsonCodepoints.CSON_SCHEMA_DATA_SEP;
import static javacson.CsonCodepoints.OBJECT_VAL;
import static javacson.CsonCodepoints.FIELD_VAL;

public class TestCsonObjectSerializer {
    @Nested
    public class SingleField {
        public class Person {
            public int age;

            public Person(int age) {
                this.age = age;
            }
        }

        @Test
        public void shouldHandleOne() {
            CsonObjectSerializer serialzer = new CsonObjectSerializer();
            var result = serialzer.serialize(List.of(
                new Person(42)
            ));
            assertEquals(
                "🍽Person🥣age🧂int32🔥🍲Person🌶️42",
                result
            );
        }

        @Test
        public void shouldHandleMultiple() {
            CsonObjectSerializer serialzer = new CsonObjectSerializer();
            var result = serialzer.serialize(List.of(
                new Person(42),
                new Person(90)
            ));
            assertEquals(
                "🍽Person🥣age🧂int32🔥"
                + "🍲Person🌶️42"
                + "🍲Person🌶️90",
                result
            );
        }
    }

    @Nested
    public class ProtectLevels {
        public class Foo {
            public int afield = 0;
            int bfield = 0;
            private int cfield = 0;
        }

        public class StaticTest {
            public int afield = 0;
            public static int sfield = 1;
        }

        @Test
        public void shouldOnlyBePrivateFields() {
            CsonObjectSerializer serialzer = new CsonObjectSerializer();
            var result = serialzer.serialize(List.of(
                new Foo()
            ));
            assertEquals(
                "🍽Foo🥣afield🧂int32"
                + "🔥"
                + "🍲Foo🌶️0",
                result
            );
        }

        @Test
        public void shouldNotIncludeStatic() {
            CsonObjectSerializer serialzer = new CsonObjectSerializer();
            var result = serialzer.serialize(List.of(
                new StaticTest()
            ));
            assertEquals(
                "🍽StaticTest🥣afield🧂int32"
                + "🔥"
                + "🍲StaticTest🌶️0",
                result
            );
        }
    }

    @Nested
    public class Inheritance {
        public class A {
            public int afield = 0;
        }

        public class B extends A {
            public int bfield = 1;
        }

        @Test
        public void shouldOnlyBePrivateFields() {
            CsonObjectSerializer serialzer = new CsonObjectSerializer();
            var result = serialzer.serialize(List.of(
                new A(),
                new B()
            ));
            assertEquals(
                "🍽A🥣afield🧂int32"
                + "🍽B🥣afield🧂int32🥣bfield🧂int32"
                + CSON_SCHEMA_DATA_SEP
                + OBJECT_VAL + "A" + FIELD_VAL + "0"
                + OBJECT_VAL + "B" + FIELD_VAL + "0" + FIELD_VAL + "1",
                result
            );
        }

    }

    @Nested
    public class HandleOrdering {
        public class Foo {
            public int cfoo = 3;
            public int afoo = 1;
            public int bfoo = 2;
        }

        @Test
        public void shouldGiveFieldsInLexographicOrder() {
            CsonObjectSerializer serializer = new CsonObjectSerializer();
            var result = serializer.serialize(List.of(
                new Foo()
            ));
            assertEquals(
                "🍽Foo"
                + "🥣afoo🧂int32"
                + "🥣bfoo🧂int32"
                + "🥣cfoo🧂int32"
                + CsonCodepoints.CSON_SCHEMA_DATA_SEP
                + "🍲Foo🌶️1🌶️2🌶️3",
                result
            );
        }
    }

    @Nested
    public class HandleBoxed {
        public class Foo {
            public Long afoo = 3L;
            public Float bfoo = 1.4f;
        }

        @Test
        public void shouldHandleBoxedValues() {
            CsonObjectSerializer serializer = new CsonObjectSerializer();
            var result = serializer.serialize(List.of(
                new Foo()
            ));
            assertEquals(
                "🍽Foo"
                + "🥣afoo🧂int64"
                + "🥣bfoo🧂float32"
                + CsonCodepoints.CSON_SCHEMA_DATA_SEP
                + "🍲Foo🌶️3🌶️1.4",
                result
            );
        }
    }

    @Nested
    public class BadFields {
        public class FooArray {
            public int[] afoo;
        }

        public class FooOtherType {
            public CsonObjectSerializer meta;
        }

        @Test
        public void shouldFailOnArray() {
            CsonObjectSerializer serializer = new CsonObjectSerializer();
            assertThrows(CsonSerializationError.class, () -> {
                serializer.serialize(List.of(
                    new FooArray()
                ));
            });
        }

        @Test
        public void shouldFailOnOtherType() {
            CsonObjectSerializer serializer = new CsonObjectSerializer();
            assertThrows(CsonSerializationError.class, () -> {
                serializer.serialize(List.of(
                    new FooOtherType()
                ));
            });
        }
    }

    @Nested
    public class StringVals {
        public class Foo {
            public String strField = "hello world";
        }

        @Test
        public void shouldEncodeString() {
            CsonObjectSerializer serializer = new CsonObjectSerializer();
            var result = serializer.serialize(List.of(
                new Foo()
            ));
            assertEquals(
                "🍽Foo"
                + "🥣strField🧂string"
                + CsonCodepoints.CSON_SCHEMA_DATA_SEP
                + "🍲Foo🌶️hello world",
                result
            );
        }
    }

    @Nested
    public class FullEndToEnd {
        public class Vehicle {}

        public class LandVehicle extends Vehicle {
            public double lengthM;
            public float weightKg;
            public int wheels;

            public LandVehicle(double lengthM, float weightKg, int wheels) {
                this.lengthM = lengthM;
                this.weightKg = weightKg;
                this.wheels = wheels;
            }
        }

        public class Spaceship extends Vehicle {
            public boolean fullyOperational;
            public String name;
            public int maxCrew;
            public long powerLevel;
            public boolean superluminal;

            public Spaceship(
                boolean fullyOperational,
                int maxCrew, 
                String name,
                long powerLevel, 
                boolean superluminal
            ) {
                this.name = name;
                this.maxCrew = maxCrew;
                this.powerLevel = powerLevel;
                this.superluminal = superluminal;
                this.fullyOperational = fullyOperational;
            }
        }

        @Test
        public void shouldHandleMany() {
            CsonObjectSerializer serialzer = new CsonObjectSerializer();
            var result = serialzer.serialize(List.of(
                new LandVehicle(3.3, 1003.43f, 4),
                new Spaceship(true, 2, "X-Wing", 9001, true),
                new LandVehicle(2.2, 500.43f, 2)
            ));
            assertEquals(
                "🍽LandVehicle"
                + "🥣lengthM🧂float64"
                + "🥣weightKg🧂float32"
                + "🥣wheels🧂int32"
                + "🍽Spaceship"
                + "🥣fullyOperational🧂boolean"
                + "🥣maxCrew🧂int32"
                + "🥣name🧂string"
                + "🥣powerLevel🧂int64"
                + "🥣superluminal🧂boolean"
                + "🔥"
                + "🍲LandVehicle🌶️3.3🌶️1003.43🌶️4"
                + "🍲Spaceship🌶️👍🌶️2🌶️X-Wing🌶️9001🌶️👍"
                + "🍲LandVehicle🌶️2.2🌶️500.43🌶️2",
                result
            );
        }
    }
}
