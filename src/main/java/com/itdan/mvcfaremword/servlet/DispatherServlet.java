package com.itdan.mvcfaremword.servlet;



import com.itdan.mvcfaremword.annocation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatherServlet  extends HttpServlet {

    //加载配置文件对象
    private Properties contextConfig=new Properties();
    //存储对象名集合
    private List<String>classNames=new ArrayList<>();

    //ioc容器
    private Map<String,Object> ioc=new HashMap<>();

    //处理器映射关系
    private Map<String,Method> handleMapping=new HashMap<>();

    /**
     * 初始化
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("ContextConfigLocation"));

        //2.解析配置文件,扫描所有相关的包
        doScanner(contextConfig.getProperty("scanpackage"));

        //3.初始化所有相关的类，并保存到IOC容器中
        doInstance();

        //4.完成自动化注入，DI
        doAutoWrite();

        //5.创建HandleMapping将我们的URL与方法形成对应关系
        initHandlerMapping();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        //调用doGet和odPost方法，将结果返回控制台
        //MVC阶段
        try {

            doDisPath(req,resp);
        }catch (Exception e){
            e.printStackTrace();
            //返回500异常
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    //-------------------------------------------------------------------

    /**
     * 使用流的方式读取properties配置文件
     * @param ContextConfigLocation 加载的源文件
     */
    private void doLoadConfig(String ContextConfigLocation){

        //从类路径下去获取properties文件路径
         InputStream inputStream =null;
         try {
             inputStream = this.getClass().getClassLoader().getResourceAsStream(ContextConfigLocation);
            contextConfig.load(inputStream);


        }catch (Exception e){
            e.printStackTrace();
        }finally {
             try {
                 if(inputStream!=null){
                     inputStream.close();
                 }
             }catch (Exception e){
                 e.printStackTrace();

             }
             //关闭资源
         }

     }


    /**
     * 解析配置文件,扫描所有相关的包
     * @param scanPackage 包名
     */
    private void doScanner(String scanPackage){
          //替换文件名路径
          URL url=this.getClass().getClassLoader().getResource(
                   "/"+scanPackage.replaceAll("\\.","/"));

          //使用该路径生成一个新的文件
          File f=new File(url.getFile());
          File []file= f.listFiles();
          for (File file1:file){
              //判断如果该包是文件夹，在继续获取其子文件
              if(file1.isDirectory()){
                  doScanner(scanPackage+"."+file1.getName());
              }else{
                  if(!file1.getName().endsWith(".class")){
                      continue;
                  }else {
                      //如果不是
                      String className= (scanPackage+"."+file1.getName().replace(".class","")).trim();
                      classNames.add(className);
                  }
              }
          }


     }

    /**
     * 获取classNames集合中的对象名，然后实例化对象
     */
    private void doInstance(){
        if(classNames.isEmpty()){
            return;
        }

            //去遍历出对象名
            try {
                for (String name:classNames){
                    Class<?>clz=Class.forName(name);

                    //根据注解是包不包含该元素来判断是否要实例化对象
                    if(clz.isAnnotationPresent(DanAutowrite.class)){
                        //返回源代码中给出的底层类的简称。如果底层类是匿名的则返回一个空字符串。
                       String beanName=clz.getSimpleName();
                        //实例化对象并将其存入ioc容器中
                        //将对象首字母小写
                        beanName= lowerBeanName(beanName);
                        ioc.put(beanName,clz.newInstance());


                    }else if(clz.isAnnotationPresent(DanService.class)){

                        //1.自定义注解类型
                        //getAnnotation():如果该元素的指定注释类型的注释存在于此对象上，则返回这些注释，否则返回 null
                        DanService service=clz.getAnnotation(DanService.class);
                        String beanName= service.value();
                        if ("".equals(beanName.trim())){
                            //首字母小写
                            beanName= lowerBeanName(clz.getSimpleName());

                        }
                        //实例化对象，并将对象存入ioc容器中
                         Object instance= clz.newInstance();
                        ioc.put(beanName,instance);

                        //2.用接口的全称作为key，用接口的实现类作为值
                         //获取接口
                          Class<?>[]interfaces= clz.getInterfaces();

                        for (Class<?> clazz:interfaces){

                            //当接口的实现类大于一时，我们给其抛出一个异常
                            if(ioc.containsKey(clazz.getName())){
                                //因为该对象已经存在容器中
                                throw  new Exception("该对象已经存在容器中");
                            }
                            ioc.put(clazz.getName(),instance);
                        }
                    }


                }

            }catch (Exception e){

            }

        }

    /**
     * 完成自动化注入，DI
     */
    private void doAutoWrite(){
        //如果容器为空，直接返回
        if(ioc.isEmpty()){
            return;
        }

        //自动注入操作
        for (Map.Entry<String,Object> entry :ioc.entrySet()){
            //获取相应键值对上对象的字段
            Field[] fields= entry.getClass().getDeclaredFields();
            for (Field field:fields){
                if(!field.isAnnotationPresent(DanAutowrite.class)){
                    continue;
                }else {
                       //获取自动注入对象
                     DanAutowrite autowrite= field.getAnnotation(DanAutowrite.class);
                     String beanName=autowrite.value();

                     if("".equals(beanName.trim())){
                             beanName=field.getType().getName();
                     }
                     //强制获取私有字段
                    field.setAccessible(true);
                     try {
                         field.set(entry.getValue(),ioc.get(beanName));
                     }catch (Exception e){
                         e.printStackTrace();
                     }

                }
            }

        }

     }

    /**
     * 创建HandleMapping将我们的URL与方法形成对应关系
     */
    private void initHandlerMapping(){

        //如果容器为空，直接返回
        if(ioc.isEmpty()){
            return;
        }

        //映射路径
        for (Map.Entry<String,Object> entry :ioc.entrySet()){
            Class<?> clz=entry.getValue().getClass();
            //判断是否为controller便签类
           if(!clz.isAnnotationPresent(DanController.class)){
               continue;//如果不是接打断
           }
           String baseUrl="";//路径
            if(clz.isAnnotationPresent(DanRquestMapping.class)){
                DanRquestMapping rquestMapping= clz.getAnnotation(DanRquestMapping.class);
                baseUrl=rquestMapping.value();
            }
            //获取类的所有方法
            Method[]methods=clz.getMethods();
            for (Method m:methods){
                //拼接路径
                if(!clz.isAnnotationPresent(DanRquestMapping.class)){
                    continue;//如果不是接打断
                }
                DanRquestMapping rquestMapping= clz.getAnnotation(DanRquestMapping.class);

                String url="/"+baseUrl+"/"+ rquestMapping.value().replaceAll("/+","/");

                handleMapping.put(url,m);

            }

        }
     }

    /**
     *
     * @param req
     * @param resp
     */
    private void doDisPath(HttpServletRequest req, HttpServletResponse resp) throws  Exception{
        if(this.handleMapping.isEmpty()){
                return;
        }

        String url=req.getRequestURL().toString();
        String contextPath=req.getContextPath();
        url.replace(contextPath,"").replaceAll("/+","/");
        //查看该路径是否存在映射中心，不存在返回404
        if(!this.handleMapping.containsKey(url)){
                 resp.getWriter().write("404");
                 return;
        }

        //获取方法
        Method method= this.handleMapping.get(url);
        //获取参数
           Map<String,String[]> parame=req.getParameterMap();

           String beanName=lowerBeanName(method.getDeclaringClass().getSimpleName());
           method.invoke(ioc.get(beanName),new Object[]{req,resp,parame.get("name")[0]});
    }

    /**
     *将传入的对象名，首字母小写
     * @param beanName 对象名
     */
    private String lowerBeanName(String beanName){
       char[] chars=  beanName.toCharArray();
         chars[0]=+32;
         return String.valueOf(chars);
    }

}
