[TOC]

---

# 2.	SpringMVC 配置式开发

配置式开发:**处理器类**是程序员**手工定义**的、实现了**特定接口的类**，然后再在 **SpringMVC 配置文件**中对该类进行**显式的、明确的注册**

## 2.1 处理器映射器 HandleMapping

-   HandleMapping接口:**根据request请求**找到**对应的Handler处理器**及**Interceptor拦截器**,并将他们**封装在HandlerExecutionChain对象**中,返回**给中央调度器**.其常用实现类:
    -   BeanNameUrlHandlerMapping
    -   SimpleUrlHandlerMapping

### 2.1.1 BeanNameUrlHandlerMapping bean名称与url处理器映射器

-   bean名称url处理器映射器,根据**请求的url**与spring容器中**bean的id**进行匹配,获得**映射**关系

-   ```xml
        <!--BeanNameUrlHandlerMapping处理器映射器:将url与bean的id匹配-->
        <bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>
    ```

-   由源码可知,处理器映射器的方法可以看出,理器的 Bean 的id，必须以“/”开头,否则无法加入到 urls 数组中

-   而 urls 数组中的 **url** 则是**中央调度器**用于判定“**该url所对应的类**是否**作为处理器**交给处理器适配器进行适配”的依据

-   ```java
    public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

    	/**
    	 * Checks name and aliases of the given bean for URLs, starting with "/".
    	 */
    	@Override
    	protected String[] determineUrlsForHandler(String beanName) {
    		List<String> urls = new ArrayList<>();
    		if (beanName.startsWith("/")) {
    			urls.add(beanName);
    		}
    		String[] aliases = obtainApplicationContext().getAliases(beanName);
    		for (String alias : aliases) {
    			if (alias.startsWith("/")) {
    				urls.add(alias);
    			}
    		}
    		return StringUtils.toStringArray(urls);
    	}
    ```

### 2.1.2 SimpleUrlHandlerMapping 简单url处理器映射器(常用)

-   使用BeanNameUrlHandlerMapping映射器有两点不足:

    -   bean的id作为url的请求路径,而不是bean的名称
    -   bean的定义域url绑定在一起,多个url请求同一个处理器时,就要配置多个该处理器的bean

-   **SimpleUrlHandlerMapping** **简单url处理器映射器**,将url与处理器的定义分离,**对url与处理器的bean进行统一映射管理**

    -   根据**请求的url**与spirng容器中定义的**处理器映射器子标签mappings**的**key属性**进行匹配;匹配上后再将该key的value与处理器bean的id进行匹配,从而url与处理器bean匹配上了

    -   ```xml
            <!--SimpleUrlHandlerMapping:将url作为key与bean id作为value进行匹配-->
            <bean class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
                <property name="mappings">
                    <props>
                        <prop key="/hello.do">myController</prop>
                    </props>
                </property>
            </bean>
        ```

## 2.2 处理器适配器 HandlerAdapter

-   适配器解决的问题:使得原本由于接口不兼容而不能一起工作的那些类可以在一起工作
-   处理器适配器的作用:将多种处理器（实现了不同接口的处理器），通过处理器适配器的适配，使它们可以进行统一标准的工作，对请求进行统一方式的处理
-   Handler定义为Controller接口的实现类,是因为这里使用的处理器适配器是SimpleControllerHandlerAdapter
    -   其源码中将handler强转为controller,所以要定义为controller接口的实现类,否则会出错
        -   中央调度器会首先调用该适配器的supports()方法,判断该Handler是否与Controller具有is-a关系,具有才会强转
-   HandlerAdapter会根据处理器实现的接口不同,对处理器进行适配,适配后对处理器进行执行
-   通过扩展处理器适配器， 可以执行多种类型的处理器,常用适配器如下两种:

### 2.2.1 SimpleControllerHandlerAdapter 简单控制器处理器适配器

-   所有**实现了 Controller 接口**的处理器 Bean，**均是通过此适配器**进行适配、执行的

-   Controller接口中有一个方法:

-   ```java
    	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;
    ```

-   **handleRequest()**用于处理用户提交的请求,通过调用service层代码,实现对用户请求的计算响应,**并把数据与页面封装为一个ModelAndView对象,返回给中央调度器**

### 2.2.2 HttpRequestHandlerAdapter Http请求处理器适配器

-   所有**实现了 HttpRequestHandler 接口**的处理器 Bean，均是**通过此适配器**进行适配、执行的

-   HttpRequestHandler接口中有一个方法:

-   ```java
    	void handleRequest(HttpServletRequest request, HttpServletResponse response)
    			throws ServletException, IOException;
    ```

-   **handleRequest()方法无返回值**,数据是通过直接放入request,session等域属性中,由request,session完成到目标页的跳转

-   此时视图解析器无需配置

-   ```java
    public class MyHttpRequestController implements HttpRequestHandler {
        @Override
        public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            request.setAttribute("welcome", "hello spring world");
            request.getRequestDispatcher("/web-resources/welcome.jsp").forward(request,response);   
        }
    }
    ```

-   在springmvc.xml中注册MyHttpRequestController

-   ```xml
        <!--注册MyHttpRequestController-->
        <bean class="com.sqm.handler.MyHttpRequestController" id="myHttpRequestController"/>
    ```

## 2.3 处理器

-   处理器除了实现 Controller接口外，还可以继承自一些其它的类来完成一些特殊的功能

###2.3.1继承自AbstractController抽象控制器类

-   AbstractController抽象类还继承一个父类**WebContentGenerator**

    -   **WebContentGenerator**:web内容生成器,具有supportedMethods属性:可以设置Http数据的提交方式,默认支持GET,POST

-   处理器集成自AbstractController,在注册时可设置supportedMethods的属性为GET或POST,必须大写

    -   当属性为	**POST**时,该请求**只能通过表单或AJAX请求方式进行提交,不允许通过地址栏,超链接,html标签里的src方式提交,因为这三种都是GET方式**,否则会给出405错误

-   客户端浏览器常用的request方式:

-   | 序号   | 请求方式        | 提交方式           |
    | ---- | ----------- | -------------- |
    | 1    | 表单请求        | 默认GET,可以指定POST |
    | 2    | AJAX请求      | 默认GET,可以指定POST |
    | 3    | 地址栏请求       | GET请求          |
    | 4    | 超链接请求       | GET请求          |
    | 5    | src属性资源路径请求 | GET请求          |

    AbstractController 类中有一个抽象方法需要实现

-   ```java
    	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
    			throws Exception;
    ```

-   handleRequestInternal():定义处理器时,就需要实现此方法

    -   自定义的处理器的处理器方法 **handleRequestInternal()** 最终被 **AbstractController 类handleRequest()方法**调用执行。而AbstractController类的handleRequest()方法是被**处理器适配器**调用执行的

-   ```java
    public class MyAbstractController extends AbstractController {
        //此方法被AbstractController的handleReqeust()调用执行,最终被处理器映射器调用执行
        @Override
        protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("welcome", "hello spring mvc world");
            modelAndView.setViewName("welcome");
            return modelAndView;
        }
    }
    ```

-   在springmvc中注册:

-   ```xml
    	<!--继承自controller的处理器都要使用视图解析器-->
    	<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
            <property name="prefix" value="/web-resources/"/>
            <property name="suffix" value=".jsp"/>
        </bean>
    	<!--注册MyAbstractController,设定supportedMethods是POST-->
        <bean class="com.sqm.handler.MyAbstractController" id="myAbstractController">
            <!--<property name="supportedMethods" value="POST"/>-->
            <property name="supportedMethods" value="GET"/>
        </bean>
    ```

-   使用post方式:

-   ```xml
            <property name="supportedMethods" value="POST"/>
    ```

-   在index添加submit

-   ```jsp
      <form action="${pageContext.request.contextPath}/abstract-controller.do" method="post">
          <input type="submit" value="abstract-controller"/>
      </form>
    ```

### ~~2.3.2 继承自MultiActionController~~(在spring 5 中没有此类)

-   MultiActionController类:继承自AbstractController,也可以设置supportedMethods属性来设置提交方式

1.  修改处理器类
2.  修改springmvc.xml
3.  访问方式
    1.  ~~**InternalPathMethodNameResolver**方法名解析器（默认）~~
    2.  ~~**PropertiesMethodNameResolver** 方法名解析器~~
    3.  ~~**ParameterMethodNameResolver** 方法名解析器~~

## 2.4 ModelAndView

-   模型与视图,通过addObject()方法向模型中添加数据,通过setViewName()方法向模型添加视图名称

1.  **模型Model**
    1.  **模型本质就是HashMap,但是性能更高**
    2.  HashMap是一个单向查找数组
        -   HashMap是一个单向链表数组,底层数组元素为一个Entry对象,entry只能通过next查找下一个元素,无法查找上一个元素
    3.  LinkedHashMap
        -   LinkedHashMap本质是一个HashMap,将Entry内部类扩展,Entry变为双向,可以通过before查找上一个

2.  **视图View**

    -   setViewName()指定视图名称,这里的视图名称将会对应一个视图对象,一般是不会在这里直接写上要跳转的页面

    -   试图对象会被封装在ModelAndView中,传给视图解析器来解析,最终转换为相应页面

    -   Biew对象本质仅仅是一个String,后续步骤会对这个View对象进行进一步封装

    -   当处理器的handleRequest()方法的返回值ModelAndView没有数据要携带,可以直接通过ModelAndView的带参构造器将视图名称放入ModelAndView中

    -   ```java
        public class MyController implements Controller {
            @Override
            public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
              	//	public ModelAndView(String viewName) { this.view = viewName; }
              	//当没有数据要携带时通过带参构造将视图名称放入ModelAndView中
                return new ModelAndView("welcome");
            }
        }
        ```

## 2.5 视图解析器ViewResolver

-   视图解析器**ViewResolver接口**负责将处理器的处理结果生成View视图,以下是常见的实现类

### 2.5.1 InternalResourceViewResolver内部资源视图解析器

-   InternalResourceViewResolver:内部资源视图解析器用于完成对当前web应用**内部资源的封装与跳转**
    -   内部资源的查找规则:将ModelAndView的setViewName()的视图名称与InternalResourceViewResolver视图解析器的前后缀(prefix\subfix)拼接后形成一个web应用内部资源路径
    -   之前一直使用的是这个

### 2.5.2 BeanNameViewResolver bean名称视图解析器

-   InternalResourceViewResolver使用很不灵活:
    -   只能完成内部资源的封装后的跳转,无法转外部资源,如外部网页
    -   对于内部资源只能定义一个格式的资源,及一个路径(prefix)与一个文件类型(subfix)
-   BeanNameViewResolver:**将资源封装为spring容器中的bean**,ModelAndView通过**设置视图名称(setViewName())与该bean的id对应**,来完成资源的跳转
    -   bean名称视图解析器可定义多个view视图bean,不同的ModelAndView定位到不同的id来进行跳转

1.  修改springmvc.xml

    -   将视图解析器替换为:org.springframework.web.servlet.view.BeanNameViewResolver,无属性
    -   注册View的bean
        -   **内部资源bean**的class:org.springframework.web.servlet.view.**InternalResourceView**
        -   **外部资源bean**的class:org.springframework.web.servlet.view.**RedirectView**
        -   都是通过url属性指定资源路径

    ```xml
        <!--BeanNameViewResolver:通过注册view的bean后,处理器的ModelAndView与之对应,完成跳转-->
        <bean class="org.springframework.web.servlet.view.BeanNameViewResolver"/>
        <!--注册view:内部资源-->
        <bean class="org.springframework.web.servlet.view.InternalResourceView" id="welcomeView">
            <property name="url" value="/web-resources/welcome.jsp"/>
        </bean>
        <!--注册一个外部View资源-->
        <bean class="org.springframework.web.servlet.view.RedirectView" id="bingView">
            <property name="url" value="http://www.bing.cn"/>
        </bean>
    ```

2.  修改处理器类

    ```java
    public class MyController implements Controller {
        @Override
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            //return new ModelAndView("welcome");
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("welcome", "View");
            modelAndView.setViewName("welcomeView");
            //modelAndView.setViewName("bingView");
            return modelAndView;
        }
    }
    ```

### 2.5.3 XmlViewResolver xml视图解析器

-   当需要定义的view对象很多时,就会使springmvc.xml文件变得特别大,不易维护

    -   所以将view视图对象单独提取出来定义一个xml文件
        -   则使用XmlViewResolver解析器解析

-   定义存放View对象的文件:此xml文件约束仍然为spring基本bean约束,名称自定,例如my-views.xml

    -   ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <beans xmlns="http://www.springframework.org/schema/beans"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://www.springframework.org/schema/beans
               http://www.springframework.org/schema/beans/spring-beans.xsd">
            <!--注册view:内部资源-->
            <bean class="org.springframework.web.servlet.view.InternalResourceView" id="welcomeView">
            <property name="url" value="/web-resources/welcome.jsp"/>
            </bean>
            <!--注册一个外部View资源-->
            <bean class="org.springframework.web.servlet.view.RedirectView" id="bingView">
            <property name="url" value="http://www.bing.com"/>
            </bean>
        </beans>
        ```

-   在springmvc配置文件中注册XmlViewResolver,其location属性的值指向view的配置文件

    -   ```xml
            <!--XmlViewResolver:xml视图解析器-->
            <bean class="org.springframework.web.servlet.view.XmlViewResolver">
                <property name="location" value="classpath:my-views/my-views.xml"/>
            </bean>
        ```

### 2.5.4 ResourceBundleViewResolver 资源包视图解析器

-   View视图对象的注册除了在xml配置文件中注册外,也可以**使用properties文件注册**,是哦那个ResourceBundleViewResolver解析器

-   该属性文件需要定义在类路径下,且写法有要求:

-   ```properties
    资源名称.(class)=封装资源的View全限定性类名
    资源名称.url=资源路径
    ```

-   定义properties文件:

-   ```properties
    welcomeView.(class)=org.springframework.web.servlet.view.InternalResourceView
    welcomeView.url=/web-resources/welcome.jsp

    bingView.(class)=org.springframework.web.servlet.view.RedirectView
    bingView.url=http://www.bing.com/
    ```

-   修改springmvc配置文件:

    -   ResourceBundleViewResolver的**basename属性设置为properties配置文件文件名**

    -   ```xml
            <!--ResourcesBundleViewResolver:properties视图解析器-->
            <bean class="org.springframework.web.servlet.view.ResourceBundleViewResolver">
                <property name="basename" value="my-views"/>
            </bean>
        ```

#### 视图解析器的优先级

-   当同时存在多个视图解析器解析同一ModelAndView中同一ViewName时,通过视图解析器的**order属性**确定优先级
    -   **order的值为正整数,值越小优先级越高,值相同先注册的优先级越高**

1.  定义properties文件:同上

    1.  ```properties
        view.(class)=org.springframework.web.servlet.view.RedirectView
        view.url=http://www.bing.com/search/
        ```

2.  定义存放视图的xml文件:同上

    1.  ```xml
            <!--注册view:内部资源-->
            <bean class="org.springframework.web.servlet.view.InternalResourceView" id="view">
            <property name="url" value="/web-resources/welcome.jsp"/>
            </bean>
        ```

3.  定义处理器类:同上

    1.  ```java
        public class MyController implements Controller {
            @Override
            public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.addObject("q", "view");
                modelAndView.setViewName("view");
                return modelAndView;
            }
        }
        ```

4.  修改springmvc.xml:将xml的order设置为1,resource bundle的order设置为2;之后再调换

    1.  ```xml
            <!--XmlViewResolver:xml视图解析器-->
            <bean class="org.springframework.web.servlet.view.XmlViewResolver">
                <property name="location" value="classpath:my-views/my-views.xml"/>
                <property name="order" value="2"/>
            </bean>

            <!--ResourcesBundleViewResolver:properties视图解析器-->
            <bean class="org.springframework.web.servlet.view.ResourceBundleViewResolver">
                <property name="basename" value="my-views"/>
                <property name="order" value="1"/>
            </bean>
        ```