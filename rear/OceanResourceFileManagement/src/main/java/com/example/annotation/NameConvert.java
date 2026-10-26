package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NameConvert {
    boolean tableToEnglish() default true;
    boolean sheetToEnglish() default true;

    boolean returnFieldToChinese() default false;
}
