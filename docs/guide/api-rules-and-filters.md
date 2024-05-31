# API访问规则和过滤 {#api-rules-and-filters}

## API 访问规则 {#api-rules}

PicoBase 中针对 Collection 的访问控制可以在其规则面板中定义，同时也可以提供数据过滤的支持。也就是说你可以定义什么情况下可以访问这些
Collection API，甚至是你希望访问这些 API 希望哪些数据能够被看到。如： 你希望`只有文章的发布者可以看到自己未删除的文章`。

**访问规则限定**：`只有文章发布者可以看到自己的文章`。

**数据范围限定**：`能够查看到未删除的文章`：。

以上都可以通过 PicoBase 中进行简单的配置实现，而无需任何额外的编码。

API 访问规则的管理界面如下：

![api-rules.png](..%2Fpublic%2Fapi-rules-and-filters%2Fapi-rules.png)

每个 Collection 都有 5 个访问规则，对应特定的 API 动作。

- `listRule`
- `viewRule`
- `createRule`
- `updateRule`
- `deleteRule`

[Auth collection](./collection#authcollection) 存在一个额外的配置项 `options.manageRule`,允许用户管理其他 collection
的能力，如修改其他 collection 的邮件，密码等。

每个规则有三种形式的设定：

- **“locked”** - `null`，默认值，表示该API只能被 Picobase 的管理员访问。
- **空字符串** - 表示拥有任意用户角色的用户可以访问。
- **任意非空字符串** - 表示仅被认证的用户，且满足访问规则定义表达式才能够执行该请求。

::: info Collection 中的角色
Picobase 中存在两种角色： `admin` 和 `user`。 `admin` 可以理解成超管，可以管理任何 collection，包括其它的 `admin`
。且系统默认提供了一套`admin`的管理功能（登录界面、admin 管理界面）。`user`
泛指开发人员自己定义的角色，可以是普通用户、app、设备等一系列需要鉴权的角色。同样系统提供多种认证方式给到`user`
角色，OAuth2.0、identity/email+password。
:::

::: warning **PicoBase 中的 API 规则也充当数据过滤器！**

换句话说，例如，您可以通过使用简单的过滤表达式，比如 status = "active"（其中"status"
是 collection 中定义的字段），来仅允许列出 `status = "active"` 的记录。

如果请求不符合listRule，则API将返回200空项响应，如果不符合createRule则返回400，如果不符合viewRule、updateRule和deleteRule则返回404。所有规则在被
`“locked”`（即仅限 admin ）且请求客户端不是 admin 时将返回403。

当授权 admin 执行操作时，API规则将被忽略（admin 可以访问一切）！
:::

## 过滤语法 {#filters-syntax}

进入 collection 的管理页面，在 Fields tab 的右侧可以看到 API Rules 的控制面板。

![filters-syntax.png](..%2Fpublic%2Fapi-rules-and-filters%2Ffilters-syntax.png)

API 规则配置面板支持自动提示，通常情况下一般使用三组 fields 进行配置。

- 自定义的 Collection schema [fields](./collection#fields)

  所有 Relation field 中的嵌套字段也同样支持，如 `someRelField.status != "pending"`

- **@request.***

  该配置表示访问当前 request 数据，如 query parameters， body/form data,用户认证信息等。
    - `@request.context` - 当前规则使用的 context (`@request.context != "oauth2"`)

      _当前支持的 context 值， `default`，`oauth2`，`realtime`, `protectedFile`._

    - `@request.method` - 当前请求的 Method 信息 (`@request.method = "GET"`)
    - `@request.headers.*` - 当前请求的 Headers 信息 (`@request.headers.x_token = "test"`)

      header keys 会被小写化，中划线转为下划线。(比如 "X-Token" is "x_token").

    - `@request.query.*` - 当前请求的 query 信息 (`@request.query.page = "1"`)
    - `@request.data.*` - 当前请求的 body 中的 data 信息 (`@request.data.title != ""`)
    - `@request.auth.*` - 当前请求的用户认证信息 (`@request.auth.id != ""`)

- **@collection.***

  这个 filter 可以用于针对与当前 collection 没有直接关联的其他 collection（即没有指向它的关联字段），但它们都共享一个公共字段值，比如一个类别ID：

  ```sql
  @collection.news.categoryId ?= categoryId && @collection.news.author ?= @request.auth.id
  ```

语法遵循 **OPERAND** **OPERATOR** **OPERAND** 规则

- OPERAND - 值或变量标识符，类似 sql 中的 `name` = `"zhangsan"` , `1` = `1`, `false` = `true`，看语法标注部分进行理解。

  值或变量标识符可以是 **变量名**，**某个数值**，**某个单引号双引号包围的字符串**，**null**， **true**，**false**。

- OPERATOR 操作符
    - `=` 等于
    - `!=` 不等于
    - `>` 大于
    - `>=` 大于等于
    - `<` 小于
    - `<=` 小于等于
    - `~` like 操作，表示模糊匹配或包含。如果没有指定，自动在右侧的字符串操作数中加上"%"以进行通配符匹配。
    - `!~` 同上取反
    - `?=` 包含
    - `?!=` 不包含
    - `?>` 包含大于
    - `?>=` 包含大于等于
    - `?<` 包含小于
    - `?<=` 包含小于等于
    - `?~` 包含模糊匹配
    - `?!~` 同上取反

**可以组合多个表达式通过使用 `(...)`,`&&`,`||` 字符。**

也可以使用单行注释语法 `// Example comment.`

## 特殊标识符和修饰符{#special-identifiers-and-modifiers}

### @macros{#macros}

以下 macros 可以应用到数据过滤表达式中：

:::info 宏表达式
@now - 当前时间

@second - 当前时间秒数(0-59)

@minute - 当前时间分钟数(0-59)

@hour - 当前时间小时数(0-23)

@weekday - 当前周几(0-6)

@day - 当前日期（1-31）

@month - 当前月份（1-12）

@year - 当前年（1970-2099）

@todayStart - 当前日期的起始时间

@todayEnd - 当前日期的结束时间

@monthStart - 当前月份的起始时间

@monthEnd - 当前月份的结束时间

@yearStart - 当前年份的起始时间

@yearEnd - 当前年份的结束时间
:::

例如，你可以这样使用：

```
@request.data.publicDate >= @now
```

### :isset 修饰符{#isset-modifier}

该修饰符只针对于 @request.* 字段，用于检查数据是否存在。

```
@request.data.role:isset = false
```

### :length modifier{#length-modifier}

该修饰符可以用来检查多选 fields 的数据长度（file，select，relation）

可以作用于 collection 和 @request.data.* fields。如:

```sql
// check example submitted data: {"someSelectField": ["val1", "val2"]}
@request.data.someSelectField:length > 1

// check existing record field length
someRelationField:length = 2
```

### :each 修饰符{#each-modifier}

该修饰符可作用于多选 fields （file，select，relation），通常用于作为多选数据的遍历。

```
// 检查提交的多选select 是否包含 "create"
@request.data.someSelectField:each ~ "create"

// 检查是否多选字段中以 pb_ 前缀开头
someSelectField:each ~ "pb_%"
```

## 示例 {#examples}

- 允许仅注册用户

  ```
  @request.auth.id != ""

  ```

- 允许注册用户访问且返回状态为 active 和 pending 的数据

  ```
  @request.auth.id != "" && (status = "active" || status = "pending")

  ```

- 只允许已注册用户，这些用户在allowed_users多关联字段值中被列出的（类似白名单）

  ```
  @request.auth.id != "" && allowed_users.id ?= @request.auth.id
  ```

- 只允许访问 zozo 开头的标题数据

  ```
  title ~ "zozo%"
  ```
