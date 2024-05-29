# PicoBase 是什么？ {#what-is-picobase}

PicoBase 是一个基于 java 语言和 Mysql 数据库的类 [Baas 应用](https://cloud.tencent.com/developer/article/1045253) (
后端即服务)
，专为快速构建业务应用而设计，提供开发者在构建业务应用程序时的底层基础通用能力。

<div class="tip custom-block" style="padding-top: 8px">

只是想尝试一下？跳到[快速开始](./getting-started)。

</div>

## 使用场景 {#use-cases}

- **开箱即用的数据管理类服务（中小应用）**

  PicoBase
  前端开发者在构建应用程序时不需要关注后端基础设施的搭建和维护的问题。通过部署 PicoBase
  服务，前端开发者可以专注于前端界面和用户体验的开发，而无需处理复杂的后端逻辑、服务器管理和数据库配置等任务。这大大简化了应用开发过程，加快了开发速度，并减轻了开发团队的负担。

  如下能力开箱即用：

  ::: info 一些特性
    1. 数据存储：允许开发者在应用中存储和管理数据，通常支持结构化数据、文件存储等功能。
    2. 用户身份认证：提供用户注册、登录、密码重置等身份验证功能，保护数据安全。
    3. 实时数据库：支持实时同步数据更新和查询，用于构建实时应用或即时通讯应用。
    4. 文件存储：提供存储和管理文件（如图片、音频、视频等）的能力，用于应用中的多媒体内容。
    5. 定时任务：允许开发者设置定时任务来执行特定的后端任务，如定时清理数据、生成报告等。
    6. 前端 SDK：提供 JavaScript 和 Dart 的 SDK， 提升前端开发效率。

  :::

- **融合到现有 java 技术栈（各类服务端应用）**

  后端开发者可以将 PicoBase 作为一个 SDK 直接引入，PicoBase 除能直接对外暴露原有数据管理能力及接口外，还提供更加灵活的 API
  赋能开发者，项目只需引入一个依赖，即可获得，如灵活的用户登录、前端数据绑定、数据校验、同异步的事件总线、实体级联关系的处理、底层的数据库操作、缓存操作等一众能力。

## 开发体验 {#developer-experience}

PicoBase 旨在为开发者提供出色和高效的开发体验。

- **[模型所见即所得](./collection)**：引入 PicoBase 后，默认用户可以直接访问内置的 Console 模块的 DashBoard UI
  用于数据模型 [Collection](./collection) 的管理，无需过多关注数据库和应用模型层实体，做到分钟级别暴露数据接口。

- **[底层通用组件集](./components)**：丰富的开发组件集，PbJsonTemplate、PbCache、PbUtil、PbEventBus。

<div class="tip custom-block" style="padding-top: 8px">
    PicoBase 将开发者关心的组件和能力以静态的方式暴露出来，以 spring boot 融合为例，用户无需关注底层实现，甚至不用理会依赖注入，所有组件开箱即用。
  </div>

![An image](/pbmanager.png)

::: details 点我查看代码

  ```java
        // 方式一、一行代码实现数据绑定(无视Json、Form提交）
        AdminLogin form=PbUtil.createObjFromRequest(AdminLogin.class).get();
        // 方式二、绑定到对象上（数据覆盖）
        PbUtil.bindRequestTo(form);

        // 一行代码实现登录
        PbUtil.login(InnerAdminId);

        // 一行代码实现发送同步或异步事件
        PbUtil.post(new LoginEvent(InnerAdminId));

        // 一行代码实现分页查询（分页、排序、基于sql语法的级联关系查询、fields数据清洗、expand数据丰富）
        Page<AdminModel> admins=PbUtil.queryPage(AdminModel.class);

        //更加人性化的数据校验组件
        PbUtil.validate(this,
        field(CollectionUpsert::getId,when(isNew,
        length(DEFAULT_ID_LENGTH,DEFAULT_ID_LENGTH),match(ID_REGEX_P),by(uniqueId(this.collection.tableName())))
        .else_(in(this.collection.getId()))),
        field(CollectionUpsert::isSystem,by(ensureNoSystemFlagChange(isNew))),
        field(CollectionUpsert::getType,required,in(Base,Auth,View),by(ensureNoTypeChange(isNew))),
        field(CollectionUpsert::getName,required,length(1,255),match(COLLECTION_NAME_P),by(ensureNoSystemNameChange(isNew)),by(checkUniqueName()),by(checkForVia())),
        field(CollectionUpsert::getSchema,by(checkMinSchemaFields()),by(ensureNoSystemFieldsChange()),by(ensureNoFieldsTypeChange()),by(checkRelationFields()),when(isAuth,by(ensureNoAuthFieldName())));
  ```

:::

## 性能 {#performance}

TODO

## PocketBase 又是什么？ {#what-about-pocketbase}

PicoBase 灵感来源于 [PocketBase](https://pocketbase.io/)。PocketBase 是一个基于 Golang
的嵌入式实时数据库（sqlite），提供了实时订阅、内建认证管理、方便的
Dashboard UI，以及简单的 REST-ish API。

PocketBase 其内嵌 sqlite，作者并不愿意将其底层迁移到大型数据库，它追求小而美，所以并不适合做大型应用。

PicoBase 基于 java 和 Mysql 为实现企业级应用而生，同时最小化投入原则，其也复用了 PocketBase 的众多优秀设计和组件，完全兼容其
API ，所以 PocketBase 的 Dashboard UI 和客户端 SDK 组件可直接复用，同时 PicoBase 也扩展了很多它所不具备的能力。随着后续
PocketBase 的持续迭代，PicoBase也始终与其保持同步。

