// Rahul Padhi 
// ECS 160

package javacson;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javacson.Annotations.CsonIgnore;
import javacson.Annotations.CsonName;

public class CsonObjectSerializer {

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

    public String serialize(Object object) {
        if (object == null || object.getClass().isArray()) {
            throw new CsonSerializationError("Null objects or arrays are not supported");
        }
        return serialize(Collections.singletonList(object));
    }

    private List<Field> getSerializableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getFields())
            .filter(field -> field.getAnnotation(CsonIgnore.class) == null)
            .sorted(Comparator.comparing(Field::getName))
            .collect(Collectors.toList());
    }

    private String encodeSchema(String typeName, List<Field> fields) {
        StringBuilder schemaBuilder = new StringBuilder(CsonCodepoints.TYPE_DEF).append(typeName);
        for (Field field : fields) {
            String fieldName = getFieldName(field);
            String fieldType = getCsonType(field);
            schemaBuilder.append(CsonCodepoints.FIELD_DEF).append(fieldName)
                         .append(CsonCodepoints.FIELD_DEF_TYPE).append(fieldType);
        }
        return schemaBuilder.toString();
    }

    private String encodeObject(String typeName, List<Field> fields, Object obj) {
        StringBuilder objectBuilder = new StringBuilder(CsonCodepoints.OBJECT_VAL).append(typeName);
        for (Field field : fields) {
            Object value = getFieldValue(field, obj);
            objectBuilder.append(CsonCodepoints.FIELD_VAL).append(encodeValue(value, getCsonType(field)));
        }
        return objectBuilder.toString();
    }

    private String getFieldName(Field field) {
        CsonName nameAnnotation = field.getAnnotation(CsonName.class);
        return nameAnnotation != null ? nameAnnotation.value() : field.getName();
    }

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

    private Object getFieldValue(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new CsonSerializationError("Unable to access field value: " + field.getName());
        }
    }

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
