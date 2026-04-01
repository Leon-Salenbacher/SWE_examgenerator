package objects;

import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@NoArgsConstructor
public class DataObjectReflectionSupport {

    public static Map<String, String> getAttributes(DataObject object) {
        Map<String, String> attributes = new LinkedHashMap<>();
        for (Field field : getAnnotatedFields(object.getClass())) {
            XmlField annotation = field.getAnnotation(XmlField.class);
            Object value = readValue(object, field);
            attributes.put(annotation.value(), serializeValue(value));
        }
        return attributes;
    }

    public static List<String> getAttributeNames(Class<?> type) {
        return getAnnotatedFields(type).stream()
                .map(field -> field.getAnnotation(XmlField.class).value())
                .toList();
    }

    public static void applyAttributes(DataObject object, Map<String, String> attributes) {
        for (Field field : getAnnotatedFields(object.getClass())) {
            XmlField annotation = field.getAnnotation(XmlField.class);
            if (!attributes.containsKey(annotation.value())) {
                continue;
            }
            writeValue(object, field, attributes.get(annotation.value()));
        }
    }

    private static List<Field> getAnnotatedFields(Class<?> type) {
        List<Class<?>> hierarchy = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            hierarchy.add(0, current);
        }

        List<Field> fields = new ArrayList<>();
        for (Class<?> current : hierarchy) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(XmlField.class)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private static Object readValue(DataObject object, Field field) {
        try {
            Method getter = findGetter(object.getClass(), field);
            if (getter != null) {
                return getter.invoke(object);
            }
            return field.get(object);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not read field '" + field.getName() + "'.", e);
        }
    }

    private static void writeValue(DataObject object, Field field, String rawValue) {
        try {
            field.set(object, deserializeValue(field, rawValue));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not write field '" + field.getName() + "'.", e);
        }
    }

    private static Method findGetter(Class<?> type, Field field) {
        String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        List<String> getterNames = field.getType() == boolean.class || field.getType() == Boolean.class
                ? List.of("is" + suffix, "get" + suffix)
                : List.of("get" + suffix);

        for (String getterName : getterNames) {
            try {
                return type.getMethod(getterName);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static String serializeValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> item == null ? "" : item.toString())
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
        }
        return value.toString();
    }

    private static Object deserializeValue(Field field, String rawValue) {
        Class<?> type = field.getType();
        if (type == String.class) {
            return rawValue;
        }
        if (type == int.class || type == Integer.class) {
            if (rawValue == null || rawValue.isBlank()) {
                return 0;
            }
            return Integer.parseInt(rawValue);
        }
        if (List.class.isAssignableFrom(type)) {
            if (rawValue == null || rawValue.isBlank()) {
                return new ArrayList<>();
            }
            return Arrays.stream(rawValue.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList();
        }
        throw new IllegalStateException(
                "Unsupported XML field type '%s' for field '%s'.".formatted(
                        type.getName(),
                        field.getName()
                )
        );
    }
}
