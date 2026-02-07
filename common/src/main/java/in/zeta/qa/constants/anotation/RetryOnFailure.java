package in.zeta.qa.constants.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RetryOnFailure {
    int count() default 1; // Number of retry attempts
    int delayInSeconds() default 1;
    int[] statusCodes() default {0};// Delay
}
