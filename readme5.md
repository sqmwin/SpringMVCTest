[TOC]

---

# 5. SSM整合开发

## 5.1 搭建SSM开发环境

### 5.1.1 导入jar包

1.  mybatis的jar包
2.  ehcahce的jar包
3.  spring的jar包
4.  mybatis与spring整合jar包
5.  json的jar包
6.  jackson的jar包
7.  hibernate验证器jar包
8.  其他jar包

### 5.1.2 配置web.xml

1.  指定spring配置文件的位置	
2.  注册 ServletContext 监听器
3.  注册字符集过滤器
4.  配置中央调度器

## 5.2 配置式开发

1.  定义实体类Student
2.  定义Student表
3.  定义index页面
4.  定义处理器
5.  定义service
6.  定义dao接口
7.  定义dao的mapper配置文件
8.  定义mybatis的主配置文件
9.  定义jdbc.properties
10.  定义spring的配置文件
     1.  spring-db.xml
     2.  spring-mybatis.xml
     3.  spring-service.xml
     4.  spring-tx.xml
11.  定义spring-mvc配置文件
12.  定义视图页面show.jsp

## 5.3 全注解开发

1.	复制之前的项目
2.	修改spring-mvc.xml
3.	修改该处理器类

### 5.3.2 将Spring改为注解

1.  将Service改为注解,完成dao的注入
    1.  修改spring-service.xml
    2.  修改service实现类
2.  将事务以注解方式织入到service层
    1.  修改spring-tx.xml
    2.  修改service实现类

###5.3.3 将mybatis改为注解

