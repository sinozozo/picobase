# PicoBase 是什么？ {#what-is-picobase}

PicoBase 是一个基于 java 语言和 Mysql 数据库的类 [BaaS 应用](https://cloud.tencent.com/developer/article/1045253) (
后端即服务)
，专为快速构建业务应用而设计，它为移动应用和Web应用提供了后端基础设施和功能,开发者可以通过PicoBase平台快速构建和部署应用程序的后端部分，而无需自行搭建和维护后端基础设施，同时也可基于 PicoBase 进行融合开发，实现业务的灵活扩展。


<div class="tip custom-block" style="padding-top: 8px">

只是想尝试一下？跳到[快速开始](./getting-started)。

</div>

::: details 了解下 BaaS
BaaS（Backend as a
Service）应用是一种利用第三方提供的云服务平台来处理后端服务的应用程序。这种应用模型允许开发人员专注于构建前端用户界面和业务逻辑，而无需花费大量精力在后端基础设施的搭建和管理上。通过使用
BaaS 服务，开发人员可以快速构建应用程序，减少开发时间和成本，并利用云服务提供商提供的弹性、可扩展的基础设施。

BaaS 应用通常提供用户管理、数据存储、推送通知、文件存储、身份验证等功能，使开发人员能够快速集成这些功能到他们的应用程序中，而无需自行开发和维护相应的后端服务。常见的BaaS服务提供商包括
Firebase、AWS Amplify、Parse 等。
:::

## 开发模式 {#development-model}

开发者可以将 PicoBase 作为SDK使用，用于扩展 PicoBase 满足不了的强业务类开发场景，PicoBase 作为后端基础服务，为开发者提供 Collection 管控，以及提供绝大部分开箱即用的 REST API ,满足基本的增删改查、数据校验、访问规则控制、实时数据事件等业务需求。

![develope-model.png](..%2Fpublic%2Fwhat-is-picobase%2Fdevelope-model.png)

用户引入PicoBase 依赖后，启动项目，PicoBase 会暴露 RestAPI 接口用于提供AdminUI的接入，以及可以用于业务应用的Record操作的接口。

- F0 : 业务应用接口，由业务应用自行定义，在 Service 层可调用 PicoBase 能力。
- F1 : 主要用于开发人员在开发阶段定义模型，对 Collection 进行维护，定义 HTTP 接口的访问规则。
- F2 : 暴露开发人员定义的模型数据，用于业务应用直接访问和操纵这些模型中的 Record 数据。



## 为什么选择 PicoBase {#why-choose-picobase}

PicoBase 的目的是为了让开发者少写或者不写代码实现业务。在熟练掌握的情况下，可以在数小时内完成传统 javaEE 技术栈万行代码量的应用开发。🎉

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

但 PocketBase 其内嵌 sqlite，作者并不愿意将其底层迁移到大型数据库，它追求小而美，并不适合做大型项目应用。
同时国内并没有类似的产品或框架，很多开源项目也都是基于nodejs 或 python 和文档型数据库开发，所以萌生了构造 PicoBase 的想法。

PicoBase 基于 java 和 Mysql 为实现企业级应用而生，同时最小化投入原则，其也复用了 PocketBase 的众多优秀设计和组件，完全兼容其
API ，所以 PocketBase 的 Dashboard UI 和客户端 SDK 组件可直接复用，同时 PicoBase 也扩展了很多 PocketBase 所不具备的能力。随着后续
PocketBase 的持续迭代，PicoBase也始终与其保持同步。

## 一些开源的 BaaS 平台 {#open-source-baas}

- [Parser](https://parseplatform.org/)： 是一个开源的移动应用后端服务平台，它提供了用于构建移动应用的后端基础设施，包括用户管理、数据存储、推送通知等功能。开发人员可以使用
  Parse 平台来快速搭建和部署移动应用的后端，简化开发流程并加快应用上线速度。 基于 Nodejs + MongoDB/PostgreSql，licence
  apache 2.0，star 38K，是一个较为成熟的项目。

- [Kuzzle](https://kuzzle.io/) 是一个开源的实时后端服务平台，用于构建现代应用程序的后端。Kuzzle
  提供了诸如数据存储、实时通信、身份验证、地理位置跟踪等功能，使开发人员能够快速构建功能丰富且高性能的应用程序。基于 Nodejs +
  Elasticsearch，licence apache 2.0，star 1.3K，成熟度一般。

