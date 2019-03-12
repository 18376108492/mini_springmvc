package com.itdan.demo.service.impl;

import com.itdan.demo.service.DemoService;
import com.itdan.mvcfaremword.annocation.DanService;

@DanService
public class DemoServiceImpl implements DemoService {

    @Override
    public String get(String str) {
        return "我的名字叫做:"+str;
    }
}
