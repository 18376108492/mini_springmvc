package com.itdan.mvcfaremword.annocation;

import java.lang.annotation.*;


@Target(ElementType.TYPE)//声明作用域范围
@Retention(RetentionPolicy.RUNTIME)//声明作用域的生命周期
@Documented//声明其为可见的
public @interface DanService {
    String value()default "";
}
