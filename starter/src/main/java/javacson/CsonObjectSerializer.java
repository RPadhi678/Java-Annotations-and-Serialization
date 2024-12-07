// Rahul Padhi 
// ECS 160

package javacson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javacson.Annotations.CsonIgnore;
import javacson.Annotations.CsonName;

/**
 * Handles serialization of Java objects into CSON 
 */
public class CsonObjectSerializer {

    /**
     * Serializes a list of objects into a CSON formatted string.
     * Combines schemas for unique object types and encodes data for all instances.
     */
    public String serialize(List<Object> objects) {
        if (objects == null || objects.isEmpty()) {
            return "";
        }

        Map<String, List<Field>> schemaMap = new LinkedHashMap<>();
        StringBuilder dataBuilder = new StringBuilder();

        for (Object obj : objects) {
            Class<?> clazz = obj.getClass();
            String typeName = clazz.getSimpleName();
            List<Field> fields = getSerializableFields(clazz);
            schemaMap.putIfAbsent(typeName, fields);
            dataBuilder.append(encodeObject(typeName, fields, obj));
        }

        String schema = schemaMap.entrySet().stream()
            .map(entry -> encodeSchema(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining());

        return schema + CsonCodepoints.CSON_SCHEMA_DATA_SEP + dataBuilder.toString();
    }

    /**
     * Serializes a single object into a CSON formatted string.
     * Null objects or unsupported types will throw a CsonSerializationError.
     */
    public String serialize(Object object) {
        if (object == null || object.getClass().isArray()) {
            throw new CsonSerializationError("Null objects or arrays are not supported");
        }
        return serialize(Collections.singletonList(object));
    }

    /**
     * Retrieves public, non-static fields of a class, excluding those annotated with @CsonIgnore.
     * Returns fields sorted lexicographically by name for consistent schema ordering.
     */
    private List<Field> getSerializableFields(Class<?> class1) {
        return Arrays.stream(class1.getFields())
            .filter(field -> field.getAnnotation(CsonIgnore.class) == null)
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .sorted(Comparator.comparing(Field::getName))
            .collect(Collectors.toList());
    }

    /**
     * Constructs the schema section for a specific type,
     * ensuring no duplicate field names.
     */
    private String encodeSchema(String typeName, List<Field> fields) {
        StringBuilder schemaBuilder = new StringBuilder(CsonCodepoints.TYPE_DEF).append(typeName);

        Set<String> fieldNames = new HashSet<>();
        for (Field field : fields) {
            String fieldName = getFieldName(field);
            if (!fieldNames.add(fieldName)) {
                throw new CsonSerializationError(
                    "Duplicate field name detected in schema: " + fieldName
                );
            }
            String fieldType = getCsonType(field);
            schemaBuilder.append(CsonCodepoints.FIELD_DEF)
                         .append(fieldName)
                         .append(CsonCodepoints.FIELD_DEF_TYPE)
                         .append(fieldType);
        }
        return schemaBuilder.toString();
    }

    /**
     * Encodes the data section for an object by serializing all its field values in CSON format.
     */
    private String encodeObject(String typeName, List<Field> fields, Object obj) {
        StringBuilder objectBuilder = new StringBuilder(CsonCodepoints.OBJECT_VAL).append(typeName);
        for (Field field : fields) {
            Object value = getFieldValue(field, obj);
            objectBuilder.append(CsonCodepoints.FIELD_VAL)
                         .append(encodeValue(value, getCsonType(field)));
        }
        return objectBuilder.toString();
    }

    /**
     * Determines the name to use for a field in the schema, using @CsonName if present.
     */
    private String getFieldName(Field field) {
        CsonName nameAnnotation = field.getAnnotation(CsonName.class);
        return nameAnnotation != null ? nameAnnotation.value() : field.getName();
    }

    /**
     * Maps a Java field type to its equivalent CSON type. Throws an error for unsupported types.
     */
    private String getCsonType(Field field) {
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class) {
            return "int32";
        } else if (type == long.class || type == Long.class) {
            return "int64";
        } else if (type == float.class || type == Float.class) {
            return "float32";
        } else if (type == double.class || type == Double.class) {
            return "float64";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        } else if (type == String.class) {
            return "string";
        } else {
            throw new CsonSerializationError("Unsupported field type: " + type.getName());
        }
    }

    /**
     * Retrieves the value of a field for a given object, handling access issues gracefully.
     */
    private Object getFieldValue(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new CsonSerializationError("Unable to access field value: " + field.getName());
        }
    }

    /**
     * Converts a field value to its CSON representation, handling booleans and strings specially.
     */
    private String encodeValue(Object value, String type) {
        if (value == null) {
            throw new CsonSerializationError("Null values are not supported");
        }
        return switch (type) {
            case "boolean" -> (boolean) value ? CsonCodepoints.TRUE : CsonCodepoints.FALSE;
            case "string" -> Util.escapeString(value.toString());
            default -> value.toString();
        };
    }
}


