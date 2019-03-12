package com.itdan.demo.action;

import com.itdan.demo.service.DemoService;
import com.itdan.mvcfaremword.annocation.DanAutowrite;
import com.itdan.mvcfaremword.annocation.DanController;
import com.itdan.mvcfaremword.annocation.DanRequestParam;
import com.itdan.mvcfaremword.annocation.DanRquestMapping;

@DanController
@DanRquestMapping("/demo")
public class DemoAction {

    @DanAutowrite
    private DemoService demoService;

    @DanRquestMapping("/get/{name}")
    public  String get(@DanRequestParam String name){
        return demoService.get(name);
    }

    @DanRquestMapping("/query")
    public String query(){
        return "啥都没有";
    }

}
