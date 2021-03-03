package com.jon.mySpring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.FileHandler;

public class MyAnnotationConfigApplicationContext {

    private List<String> beanNames = new ArrayList<>();

    // IoC 容器
    private Map<String, Object> ioc = new HashMap<>();

    public MyAnnotationConfigApplicationContext(String pack){

        // 构建 BeanDefinitions
        Set<BeanDefinition> beanDefinitions = findBeanDefinitions(pack);

        // 根据原材料创建 bean
        createObject(beanDefinitions);

        // 自动装载
        autoWireObject(beanDefinitions);
    }

    public String[] getBeanDefinitionNames(){
        return beanNames.toArray(new String[0]);
    }

    public Integer getBeanDefinitionCount(){
        return beanNames.size();
    }


    // 自动注入， 给 @Autowired 注解的属性 注入对象
    private void autoWireObject(Set<BeanDefinition> beanDefinitions) {

        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();

        while(iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            // 获取到这个类
            Class clazz = beanDefinition.getBeanClass();
            // 遍历属性
            Field[] declaredFields = clazz.getDeclaredFields();
            for(Field field : declaredFields){
                Autowired autowired = field.getAnnotation(Autowired.class);
                if(autowired != null){
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    if(qualifier != null){
                        // 按名字找
                        try {
                            // 得到名字
                            String beanName = qualifier.value();
                            // 按名字 从 IoC container 里返回 要注入的对象
                            Object bean = getBean(beanName);

                            // 给 该属性赋对象 （获取属性名， 方法名， 调用方法赋值）
                            String fieldName = field.getName();
                            String setterName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
                            // getMethod(String, Parameter Type)
                            Method setMethod = clazz.getMethod(setterName, field.getType());

                            // 要获取 IoC 里面的 Bean 对象
                            Object object = getBean(beanDefinition.getBeanName());

                            setMethod.invoke(object,bean);

                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }else{
                        // 按类型
                        // 根据 属性的 类型 的名字 找 bean
                        String typeName = field.getType().getName(); // com.jon.mySpring.entity.Order

                        // com.jon.mySpring.entity.Order -> Order -> order
                        String baseName = typeName.replaceAll(clazz.getPackage().getName() + ".", "");
                        baseName = baseName.substring(0,1).toLowerCase() + baseName.substring(1);

                        System.out.println("type : " + baseName);

                        // 获取 要注入 的 object
                        Object bean = getBean(baseName);

                        // 找 set 方法
                        String fieldName = field.getName();
                        String setterName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
                        try {

                            Method setMethod = clazz.getMethod(setterName,field.getType());

                            Object object = getBean(beanDefinition.getBeanName());

                            setMethod.invoke(object,bean);

                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

        }
    }

    //  创建 beans， 填充 IoC 容器 (key = beanName, value = Class object )
    public void createObject(Set<BeanDefinition> beanDefinitions){
        // 迭代出来
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();

        while(iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();

            // 获取到 bean 的名字 和 对应的类对象
            Class clazz = beanDefinition.getBeanClass();
            String beanName = beanDefinition.getBeanName();

            // 根据 @Value 给对象的属性赋值
            try {
                // 实例化一个对象
                Object object = clazz.getConstructor().newInstance();
                // 属性赋值
                Field[] declaredFields = object.getClass().getDeclaredFields();
                for (Field field : declaredFields) {
                    Value annotation = field.getAnnotation(Value.class);
                    if (annotation != null) {
                        // 获取到 注解的值 （string）
                        String value = annotation.value();
                        // 使用反射给属性赋值
                        // 获取 set 属性 的方法
                        String fieldName = field.getName();
                        // 构造 setter 的名字，利用反射得到 setter方法
                        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Method method = clazz.getMethod(setterName, field.getType());
                        // 数据类型转换 （因为 @Value 的值是字符串 ）
                        Object val = null;
                        if (field.getType().getName().equals("java.lang.Integer")) {
                            val = Integer.parseInt(value);
                        }
                        if (field.getType().getName().equals("java.lang.Float")) {
                            val = Float.parseFloat(value);
                        }
                        if (field.getType().getName().equals("java.lang.String")) {
                            val = value;
                        }
                        method.invoke(object, val);
                    }
                }
                // 存入缓存
                ioc.put(beanName, object);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Object getBean(String beanName){
        return ioc.get(beanName);
    }

    public Object getBeanByType(Class<?> beanType){
        Object obj = null;

        return obj;
    }


    // 找到所有 添加了 @Component 的类， 获取该类，和 beanName, 封装成 BeanDefinition
    public Set<BeanDefinition> findBeanDefinitions(String pack){

        Set<Class<?>> classes = Tools.getClasses(pack);

        Iterator<Class<?>> iterator = classes.iterator();

        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        // 1、获取包下所有的类
        while (iterator.hasNext()){

            // 遍历，找到添加了注解的类
            Class<?> clazz = iterator.next();
            Component componentAnnotation = clazz.getAnnotation(Component.class);
            if(componentAnnotation != null){
              // 获取 注解的 Value @Component("beanName")
                String beanName = componentAnnotation.value();
                if("".equals(beanName)){
                    // 获取类名（首字母小写）  先获取包名 + '.' 在把这些替换为 null，首字母小写，即可得到
                    String className = clazz.getName().replaceAll(clazz.getPackage().getName()+".","") ;
                    beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
                }
                // 3、合成 BeanDefinition 对象, 装载
                beanDefinitions.add(new BeanDefinition(beanName, clazz));
                // 4、装载到 beanNames list 里面
                beanNames.add(beanName);
            }
        }

        for (BeanDefinition b:beanDefinitions
             ) {
            System.out.println(b.getBeanName() + "-" + b.getBeanClass().getName());
        }


        return beanDefinitions;
    }
}
