package com.itdan.mvcfaremword.annocation;


import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DanRquestMapping {

    String value() default "";
}
