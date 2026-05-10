package models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Marks a model field as a persisted XML attribute.
 */
public @interface XmlField {
    /**
     * @return XML attribute name used for the annotated field
     */
    String value();
}
