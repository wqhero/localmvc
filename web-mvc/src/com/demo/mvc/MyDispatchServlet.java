package com.demo.mvc;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: wuquan
 * @Date: 2020-02-10 10:25
 */
//@WebServlet(name = "MyDispatchServlet")
public class MyDispatchServlet extends HttpServlet {



    /* controller 对象*/
    private Map<String, Object> controllerBeans = new ConcurrentHashMap<>();
    /* url - controller*/
    private Map<String, Object> urlController = new ConcurrentHashMap<>();
    /* url - method*/
    private Map<String, Method> urlMethod = new ConcurrentHashMap<>();

    private List<String> className = new ArrayList<>();

    private Map<String, Object> iocMap = new HashMap<>();
    @Override
    public void init() throws ServletException {
        //1 所有controller对象
        try {
            getClasses(this.getClass().getPackage().getName());
            initHandlerMapping();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            this.doDispatch(response, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletResponse response, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if(urlMethod.isEmpty()){
            return;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = uri.replace(contextPath, "").replaceAll("/+", "/");
        Method method = urlMethod.get(url);
        method.invoke(urlController.get(url), request, response);
    }
    // 获取所有类
    private List<Class> getClasses(String packageName) throws Exception {
        //开始获取多有类
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        String filePath = url.getFile();
        File file = new File(filePath);
        String[] fileList = file.list();
        for (String f : fileList) {
            File tempFile = new File(filePath + f);
            if(tempFile.isDirectory()){
                getClasses(packageName +"."+f);
            }else{
                className.add(packageName + "." + tempFile.getName());
            }
        }
        // 2 获取带有controller注解的类
        for (String clazz : className) {

            String name = clazz.replaceAll(".class", "");
            Class<?> aClass = Class.forName(name);
            if(aClass.isAnnotationPresent(MyController.class)){
                iocMap.put(aClass.getSimpleName(), aClass.newInstance());
            }
        }
        return null;
    }
    // 3 获取url - method, url-controller
    private void initHandlerMapping() throws IllegalAccessException, InstantiationException {
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class)){
               continue;
            }
            // 基础url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = annotation.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if(!method.isAnnotationPresent(MyRequestMapping.class)){
                    continue;
                }
                String requestUrl = method.getAnnotation(MyRequestMapping.class).value();
                requestUrl = (baseUrl + "/" + requestUrl).replaceAll("/+", "/");
                urlMethod.put(requestUrl, method);
                urlController.put(requestUrl, clazz.newInstance());
            }

        }
    }
}
