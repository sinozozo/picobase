# 快速开始 {#getting-started}



## 安装 {#installation}

### 前置准备 {#prerequisites}

- 项目需采用 Spring Boot 2.x 作为基础

Spring Boot 3.x 版本，适配中 ...


### 安装向导 {#setup-wizard}

用户可以使用 [Spring Initializr](https://start.spring.io/) 快速构建 Spring Boot 项目,然后引入 PicoBase 的 Maven 或 Gradle 依赖。



::: code-group

```xml [Maven]
//TODO 待补充, 请先自行clone代码构建并引入
```
```xml [Gradle]
//TODO 待补充, 请先自行clone代码构建并引入
```

:::

数据源配置：

```yaml

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root # 请更换为自己的mysql账号
    password: xxxx # 请更换为自己的mysql密码
    url: jdbc:mysql://...:3306/xxx # 请更换为自己的mysql链接地址

```

PicoBase 配置(可选)：

```yaml

# PicoBase 配置
picobase:
  # 是否打印日志
  isLog: true
  # 是否打印 banner
  isPrint: true
  # jwt 秘钥
  jwtSecretKey: X8yXwWJE1k5zN7h+fP8g1U8QmW6i8LrQ4+QJyB2p6EU=

pb-console:
  auth: true  ## 是否需要登录认证 ， true 需要， false 不需要
  identity: admin@picobase.cn 
  password: admin@picobase.cn
  # dev 模式，打印sql日志，存储http执行信息到数据库中， 开启后会影响执行性能
  isDev: true

```


:::tip 关于 Mysql 账号权限
PicoBase 在启动和配置 Collection 时需要执行 DDL 语句，所以分配的 mysql 账号需具备 DDL 权限。
:::

## 启动项目 {#start-project}

完成以上配置即可启动项目（Spring boot 的启动 main 函数）。启动成功后

```html

┌─┐┬┌─┐┌─┐┌┐ ┌─┐┌─┐┌─┐
├─┘││  │ │├┴┐├─┤└─┐├┤ 
┴  ┴└─┘└─┘└─┘┴ ┴└─┘└─┘ v0.0.1

 http://www.picobase.cn

PB [INFO] -->: [thread-1] 全局配置 PbConfig{cookie=SaCookieConfig [domain=null, path=null, secure=false, httpOnly=false, sameSite=null], s3=S3Config{enable=true, endpoint='null', bucket='picobase', region='null', accessKey='null', secretKey='null', forcePathStyle=true}, tokenName='Authorization', timeout=2592000, activeTimeout=-1, isConcurrent=true, dynamicActiveTimeout=false, isShare=true, maxLoginCount=12, maxTryTimes=12, isReadBody=true, isReadHeader=true, isReadCookie=true, isWriteHeader=false, tokenStyle='uuid', autoRenew=true, tokenPrefix='null', tokenSessionCheckLogin=true, isPrint=true, isLog=true, isColorLog=true, logLevelInt=1, currDomain='null', dataRefreshPeriod=30, basic='', jwtSecretKey='X8yXwWJE1k5zN7h+fP8g1U8QmW6i8LrQ4+QJyB2p6EU='} 
....
2024-07-05 14:36:09.008  INFO 78016 --- [           main] com.picobase.StartUpApplication          : Started StartUpApplication in 2.814 seconds (JVM running for 3.678)
PB [INFO] -->: [thread-1] Initializing PB System Tables ...
2024-07-05 14:36:09.026  INFO 78016 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2024-07-05 14:36:09.408  INFO 78016 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
PB [DEBUG]-->: [thread-1] [501ms] [[{"executeNo":0,"sql":"create table if not exists pb_admin\n(\n    id              char(20)      not null\n        primary key,\n    avatar          int default 0 not null,\n    email           varchar(100)  not null,\n    tokenKey        varchar(100) ...: true
PB [DEBUG]-->: [thread-1] [65ms] ["SELECT * FROM `pb_collection` WHERE id=:id",{"id":"_pb_users_auth_"},"com.picobase.model.CollectionModel"]: CollectionModel{name='users', type='auth'}
PB [DEBUG]-->: [thread-1] [10ms] ["SELECT * FROM `pb_collection` WHERE id=:id",{"id":"_pb_log_"},"com.picobase.model.CollectionModel"]: CollectionModel{name='pb_log', type='base'}
PB [DEBUG]-->: [thread-1] [20ms] [[]]: true

启动成功：PicoBase配置如下：PbConsoleConfig{auth=true, identity='admin@admin.com', password='admin@admin.com', include='/**', exclude='', isDev=true, s3Config=null, dataDirPath='/Users/zouqiang/Documents/IdeaProjects/java-projects/pico/pb_data/storage'}
2024-07-05 14:36:10.125  INFO 78016 --- [on(2)-127.0.0.1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2024-07-05 14:36:10.125  INFO 78016 --- [on(2)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2024-07-05 14:36:10.126  INFO 78016 --- [on(2)-127.0.0.1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms

```

访问 `http://localhost:8080/console/` 即可访问 PicoBase 的控制台， 注意最后一个 `/` 是必须的。

首次登录，在登录界面中输入配置文件中的identity和password配置（我这里是 admin@picobase.cn 和 admin@picobase.cn） 即可登录。

登录后主页如下：

![main-page.png](..%2Fpublic%2Fgetting-started%2Fmain-page.png)

:::tip 系统启动
系统启动后会检查 mysql 中是否包含 Picobase 的系统表（pb_collection、pb_admin、pb_log），如果没有则会自动创建。
:::

## 创建第一个 Demo 项目 {#create-a-demo-project}

假设我们要实现一个 TODO List 的用户任务管理的功能， 用于为外部提供接口 TODO 管理的接口。首先我们要创建一个 Todo [collection](./collection)(picobase 中的数据模型对应数据库中的表)。
该 Todo collection 应包含 `taskName` , `taskContent` ,`user`,`isCompleted` 四个字段。 其中 `user` 字段用于表述当前任务属于哪个用户。

点击左侧的 `+ New Collection` 按钮， 便可打开一个 `Todo` collection 的页面。

![todo-create-page.png](..%2Fpublic%2Fgetting-started%2Ftodo-create-page.png)

注意 `user` 字段， 这里选择 `relation` 类型，并关联了系统创建的 `users` collection， 从而实现了 `user` 字段的关联。`isCompleted` 字段用于表述当前任务是否完成，这里为了演示，直接存储为中文文本值。

至此，这个 Todo 接口就算开发完成了 🎉🎉🎉。

接口的使用文档可以点击打开 Todo collection 右侧 `Api preview` 页面。也可以在该控制台中手动执行这些接口的调用，所有调用信息均可在 `Logs` 菜单中查看。

## 进阶用法 {#advanced-usage}
 

PicoBase 跟传统脚手架项目不同，其运行过程中并没有生成额外代码。

基于 collection 元数据和动态 sql 构建实现，PicoBase 支持复杂的正向反向级联查询，详见 [API 访问规则和过滤](./api-rules-and-filters)。

一些访问示例：

```shell

## 访问 Todo collection  的第一页数据， 按照 `user.name` 字段进行过滤  ~ 表示模糊匹配， = 表示绝对匹配， `isComplated = '已完成'` 过滤
## 执行日志->["SELECT DISTINCT `Todo`.* FROM `Todo` LEFT JOIN `users` `Todo_user` ON Todo_user.id = Todo.user WHERE (Todo_user.name LIKE :tl1xpJ AND Todo.isComplated = :t2O4Q6) LIMIT 1",{"tl1xpJ":"%三%","t2O4Q6":"已完成"},{}]: List<RecordModel> size:1 
GET /api/collections/Todo/records?page=1&perPage=1&filter=user.name ~ '三' && isComplated = '已完成'&fields=id

## 查询今天存在Todo 未完成的用户Collection（反向查询）
GET /api/collections/users/records?page=1&perPage=40&sort=-created&skipTotal=1&filter=Todo_via_user.isComplated ?= '未完成' && Todo_via_user.created < @todayEnd&expand=&fields=

```

