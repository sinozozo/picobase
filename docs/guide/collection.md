---
outline: deep
---

# Collection {#collection}

## 概述 {#overview}

**Collection** 是 PicoBase 中的一个重要概念，控制台所维护的也是一系列 Collection，其代表了你应用程序的数据实体(领域实体)
。假设拿博客系统举例，博客系统中的用户、文章、评论等领域模型在
PicoBase中都叫做
Collection。在数据库层面这些都是物理存在的实体表。在底层 Collection 会包含一系列元数据（**Schema**）信息，这些信息维护在
**pb_collection** 表中。

Collection 中单独的一条记录叫做 **record** ，可以简单的理解为实体表中的一条数据，如某一条用户数据、评论数据等。

PicoBase 中可以为 Collection 定义一系列字段 **fields**,如下: {#fields}

| Field          |                         举例值                         |       支持的 modifiers |
|:---------------|:---------------------------------------------------:|--------------------:|
| `text`         |                  `""`,`"example"`                   |                     |
| `editor`       |              `""`, `"<p>example</p>"`               |                     |
| `number`       |                `0`,`-1`,`1.5`,`100`                 |     `+`(增加),`-`(减少) |
| `bool`         |                   `false`,`true`                    |                     |
| `email`        |            `""`,`"zozo@scmobility.com" `            |                     |
| `url`          |            `""`,`"https://picobase.cn"`             |                     |
| `date`         |            `""`,`"2024-06-01 00:00:00"`             |                     |
| `select`(单选)   |                  `""`,`"optionA"`                   |                     |
| `select`(多选)   |            `[]`,`["optionA","optionB"]`             | `+`(追加操作),`-`(移除操作) |
| `relation`(单选) |              `""`,`"JJ2YRU30FBG8MqX"`               |                     |
| `relation`(多选) |   `[]`, `["JJ2YRU30FBG8MqX", "eP2jCr1h3NGtsbz"]`    | `+`(追加操作),`-`(移除操作) |
| `file`(单选)     |          `""`, `"example123_Ab24ZjL.png"`           |                     |
| `field`(多选)    | `[]`, `["file1_Ab24ZjL.png", "file2_Frq24ZjL.txt"]` |           `-`(移除操作) |
| `json`         |                   _任意 json 格式数据_                    |                     |

你可以通过访问 Console 控制台、 [Web Api](./webapi)、[客户端SDKs](./client-sdks) 的方式创建 collections 和 records

Collection panel 如下：
![collection-panel.png](..%2Fpublic%2Fcollection%2Fcollection-panel.png)

有三种类型的 Collection 可以选择创建 Base collection、View collection 和 Auth collection。

## Base collection {#basecollection}

**Base collection** 是 PicoBase 中默认的 collection 类型，可以存储任意类型的数据，如文章、产品、评论等。
该类型的 collection 会自动生成三个系统字段，分别是 `id`、`created` 和 `updated`。仅 `id` 可以被设置，且必须是长度为 15
位的字符。

## View collection {#viewcollection}

**View collection** 是一个只读类型的 collection，这个 collection 中的数据来自一段 SQL 查询。你可以应用该 collection
实现聚合、统计等自定义查询。如下面的例子，创建了一个包含 3 个字段的只读 collection，分别是 `id`, `name`,
`totalComments`。

```sql
SELECT posts.id,
       posts.name,
       count(comments.id) as totalComments
FROM posts
         LEFT JOIN comments on comments.postId = posts.id
GROUP BY posts.id
```

::: warning 注意
View collection 不可以接收实时事件，因为该 collection 没有 create/update/delete 操作。
:::

## Auth collection {#authcollection}

**Auth collection** 拥有 Base collection 的所有特性，可以用来帮助你的应用实现多种认证。

每个 Auth collection 包含以下系统字段：`id`,`created`,`updated`,`username`,`email`,`emailVisibility`,`verified`。

系统中可以有多套认证逻辑，如的系统可以有用户、会员、客户端、管理员等。这些都可以用 Auth collection 表示，每个 collection
都有各自的认证逻辑（邮件+密码/账号+密码，或者 OAuth2 认证）和所管理的数据（api endpoints 接口）访问权限。

用户可以创建各种各样的访问控制方式：

- 基于角色组 **Role(Group)**

  举例：你可以在自己的 Auth collection 中附加一个名为“角色”的下拉选择字段，选项包括："普通用户"和"超级用户"
  。然后在其他一些 Base collection 中，你可以定义规则，只允许"超级用户"进行操作。

  `@request.auth.role = "superUser"`

- 基于关系 **Relation(Ownership)**

  举例：假设你有两个 Collection 一个是 base 类型的`文章collection`，一个是 auth 类型的`用户collection`
  ，你可以在`文章collection` 中创建一个指向`用户collection`的作者字段。目的是实现一个功能，只让作者能够访问自己的文章。你可以定义如下规则：
  `@request.auth.id != "" && author = @request.auth.id`

  也支持关系的嵌套，如：

  `someRelField.anotherRelField.author = @request.auth.id`

- 特殊管理 **Managed**
  除了默认的“list”、“view”、“create”、“update”、“delete” API
  规则之外，Auth collection 还有一个特殊的“Managed” API 规则，可以允许一个用户（甚至可以来自不同的
  collection）完全管理另一个用户的数据（例如，更改他们的电子邮件、密码等）

- 混合 **Mixed**
  可以根据你的使用情况构建一个混合方法。多个规则可以用括号`()`分组，并结合使用 `&&`（与）和 `||`（或）运算符。

  `@request.auth.id != "" && (@request.auth.role = "superUser" || author = @request.auth.id)`