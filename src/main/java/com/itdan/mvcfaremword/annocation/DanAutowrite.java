package com.itdan.mvcfaremword.annocation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DanAutowrite {
    String value()default "";
}
