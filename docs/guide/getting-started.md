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
    username: root
    password: Bangbangdechelianwang
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
  auth: true
  identity: admin@admin.com
  password: admin@admin.com
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
┴  ┴└─┘└─┘└─┘┴ ┴└─┘└─┘ v0.1

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

假设选择在 `./docs` 中搭建 VitePress 项目，生成的文件结构应该是这样的：

```
.
├─ docs
│  ├─ .vitepress
│  │  └─ config.js
│  ├─ api-examples.md
│  ├─ markdown-examples.md
│  └─ index.md
└─ package.json
```

`docs` 目录作为 VitePress 站点的项目**根目录**。`.vitepress` 目录是 VitePress 配置文件、开发服务器缓存、构建输出和可选主题自定义代码的位置。

:::tip
默认情况下，VitePress 将其开发服务器缓存存储在 `.vitepress/cache` 中，并将生产构建输出存储在 `.vitepress/dist` 中。如果使用
Git，应该将它们添加到 `.gitignore` 文件中。也可以手动[配置](../reference/site-config#outdir)这些位置。
:::

### 配置文件 {#the-config-file}

配置文件 (`.vitepress/config.js`) 让你能够自定义 VitePress 站点的各个方面，最基本的选项是站点的标题和描述：

```js
// .vitepress/config.js
export default {
  // 站点级选项
  title: 'VitePress',
  description: 'Just playing around.',

  themeConfig: {
    // 主题级选项
  }
}
```

还可以通过 `themeConfig` 选项配置主题的行为。有关所有配置选项的完整详细信息，请参见[配置参考](../reference/site-config)。

### 源文件 {#source-files}

`.vitepress` 目录之外的 Markdown 文件被视为**源文件**。

VitePress 使用 **基于文件的路由**：每个 `.md` 文件将在相同的路径被编译成为 `.html` 文件。例如，`index.md`
将会被编译成 `index.html`，可以在生成的 VitePress 站点的根路径 `/` 进行访问。

VitePress 还提供了生成简洁 URL、重写路径和动态生成页面的能力。这些将在[路由指南](./realworld)中进行介绍。

## 启动并运行 {#up-and-running}

该工具还应该将以下 npm 脚本注入到 `package.json` 中：

```json
{
  ...
  "scripts": {
    "docs:dev": "vitepress dev docs",
    "docs:build": "vitepress build docs",
    "docs:preview": "vitepress preview docs"
  },
  ...
}
```

`docs:dev` 脚本将启动具有即时热更新的本地开发服务器。使用以下命令运行它：

::: code-group

```sh [npm]
$ npm run docs:dev
```

```sh [pnpm]
$ pnpm run docs:dev
```

```sh [yarn]
$ yarn docs:dev
```

```sh [bun]
$ bun run docs:dev
```

:::

除了 npm 脚本，还可以直接调用 VitePress：

::: code-group

```sh [npm]
$ npx vitepress dev docs
```

```sh [pnpm]
$ pnpm vitepress dev docs
```

```sh [yarn]
$ yarn vitepress dev docs
```

```sh [bun]
$ bun vitepress dev docs
```

:::

更多的命令行用法请参见 [CLI 参考](../reference/cli)。

开发服务应该会运行在 `http://localhost:5173` 上。在浏览器中访问 URL 以查看新站点的运行情况吧！

## 下一步 {#what-s-next}

- 想要进一步了解 Markdown 文件是怎么映射到对应的 HTML，请继续阅读[路由指南](./realworld)。

- 要了解有关可以在页面上执行的操作的更多信息，例如编写 Markdown 内容或使用 Vue
  组件，请参见指南的“编写”部分。一个很好的起点是了解 [Markdown 扩展](./markdown)。

- 要探索默认文档主题提供的功能，请查看[默认主题配置参考](../reference/default-theme-config)。

- 如果想进一步自定义站点的外观，参见[扩展默认主题](./extending-default-theme)或者[构建自定义主题](./custom-theme)。

- 文档成形以后，务必阅读[部署指南](./deploy)。
