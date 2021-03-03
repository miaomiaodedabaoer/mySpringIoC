## 手写 IoC

### IoC 意义：

弱依赖：根据注解进行配置，变更简单

高效：将类对象存储到 IoC 容器中，实现单例



### 实现功能

1. 自动扫描包下的类

2. 按注解创建对象（@Value）

3. 按注解注入对象（@Autowired，@Qualifier)

4. IoC容器存储对象单例

   

### 主要步骤

1. FindBeanDefinitions, 扫描指定包下的类，根据 @Component 创建 BeanDefinitions 集合 （存储 beanName 及对应的 Class）
2. CreateObject, 根据 BeanDefinitions 创建单例对象，过程中根据 @Value 对一些属性赋值
3. AutowireObject, 给 @Autowired 注解的属性注入对象（从IoC 集合中）



##### 参考

https://www.bilibili.com/video/BV1AV411i7VH