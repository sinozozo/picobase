---
outline: deep
---

# 实体关系{#model-relation}

## 概述{#model-relation-overview}

为更好的理解 PicoBase 对关系模型的支持，我们在这里假设如下模型:

![relation-example.png](..%2Fpublic%2Fmodel-relation%2Frelation-example.png)

其中包含四个实体：`User`、`Post`、`Comment` 和 `Tag`。注意其中的 relation field 类型的字段。

**Relation fields 遵循与任何其他 Collection fields 相同的规则，开发者可以通过直接更新字段值来设置/修改 - Relation fields 存储的是 Record 的 Id 信息，并且支持一对一和一对多的关联关系。**

举例，下面是创建了一个新的 `Post` 并且关联了两个 `Tag` 的示例。

::: code-group

```javascript

import PocketBase from 'pocketbase';

const pb = new PocketBase('http://127.0.0.1:8090');

...

const post = await pb.collection('posts').create({
    'title': 'Lorem ipsum...',
    'tags':  ['TAG_ID1', 'TAG_ID2'],
});

```

```dart
import 'package:pocketbase/pocketbase.dart';

final pb = PocketBase('http://127.0.0.1:8090');

...

final post = await pb.collection('posts').create(body: {
    'title': 'Lorem ipsum...',
    'tags':  ['TAG_ID1', 'TAG_ID2'],
});
```

:::

## 追加关系{#model-relation-append}

使用 `+` 操作符，向单个或多个关系字段追加绑定信息。

::: code-group

```javascript
import PocketBase from 'pocketbase';

const pb = new PocketBase('http://127.0.0.1:8090');

...

const post = await pb.collection('posts').update('POST_ID', {
    // append single tag
    'tags+': 'TAG_ID1',

    // append multiple tags at once
    'tags+': ['TAG_ID1', 'TAG_ID2'],
})
```

```dart

import 'package:pocketbase/pocketbase.dart';

final pb = PocketBase('http://127.0.0.1:8090');

...

final post = await pb.collection('posts').update('POST_ID', body: {
    // append single tag
    'tags+': 'TAG_ID1',

    // append multiple tags at once
    'tags+': ['TAG_ID1', 'TAG_ID2'],
})
```

:::

## 移除关系{#model-relation-remove}

使用 `-` 操作符， 从单个或多个关系字段移除绑定信息。

::: code-group

```javascript
import PocketBase from 'pocketbase';

const pb = new PocketBase('http://127.0.0.1:8090');

...

const post = await pb.collection('posts').update('POST_ID', {
    // remove single tag
    'tags-': 'TAG_ID1',

    // remove multiple tags at once
    'tags-': ['TAG_ID1', 'TAG_ID2'],
})
```

```dart

import 'package:pocketbase/pocketbase.dart';

final pb = PocketBase('http://127.0.0.1:8090');

...

final post = await pb.collection('posts').update('POST_ID', body: {
    // remove single tag
    'tags-': 'TAG_ID1',

    // remove multiple tags at once
    'tags-': ['TAG_ID1', 'TAG_ID2'],
})

```

:::

## 展开 Relations {#model-relation-expand}

开发者可以直接展开 Http 响应数据中的 Relation fields，只需要新增一个 `expand` query param 即可。例如 `api/collections/comments/records?expand=user,post.tags`

<div class="tip custom-block" style="padding-top: 8px">
某个 relation field 能否展开，依赖当前访问用户是否拥有对该 Relation  Collection 的 View 访问权限(即，要满足 Relation Collection 的 View Rule),
expand 参数指向的 Relation Collection 支持 filter 和 sort 查询参数，并且可以通过 a`.`b 这种访问方式，但最深支持6级嵌套。 
</div>

例如， expand `comments` Collection 的 user field。 可以这样写：

::: code-group

```javascript
await pb.collection("comments").getList(1, 30, { expand: "user" })
```

```dart
await pb.collection("comments").getList(perPage: 30, expand: "user")
```

:::

Response:

```json

{
    "page": 1,
    "perPage": 30,
    "totalPages": 1,
    "totalItems": 20,
    "items": [
        {
            "id": "lmPJt4Z9CkLW36z",
            "collectionId": "BHKW36mJl3ZPt6z",
            "collectionName": "comments",
            "created": "2022-01-01 01:00:00.456Z",
            "updated": "2022-01-01 02:15:00.456Z",
            "post": "WyAw4bDrvws6gGl",
            "user": "FtHAW9feB5rze7D",
            "message": "Example message...",
            "expand": {
                "user": {
                    "id": "FtHAW9feB5rze7D",
                    "collectionId": "srmAo0hLxEqYF7F",
                    "collectionName": "users",
                    "created": "2022-01-01 00:00:00.000Z",
                    "updated": "2022-01-01 00:00:00.000Z",
                    "username": "users54126",
                    "verified": false,
                    "emailVisibility": false,
                    "name": "John Doe"
                }
            }
        },
        ...
    ]
}

```

## 反向 Relations {#model-relation-back}

反向 Relations 的意思是，要查询的主 Collection 中并没有关联字段，但是开发者需要经过自身在其他 Collection 中的关联字段进行查询。

语法构成如下：`referenceCollection_via_relField` ,如 comments**_via_**post。

如， 开发者要查询文章 Collections，条件为文章列表的评论中至少包含一条 `hello` 的评论。

::: code-group

```javascript
await pb.collection("posts").getList(1, 30, {
    filter: "comments_via_post.message ?~ 'hello'"
    expand: "comments_via_post.user",
})
```

```dart
await pb.collection("posts").getList(
    perPage: 30,
    filter: "comments_via_post.message ?~ 'hello'"
    expand: "comments_via_post.user",
)
```

:::

Response:
```json
{
    "page": 1,
    "perPage": 30,
    "totalPages": 2,
    "totalItems": 45,
    "items": [
        {
            "id": "WyAw4bDrvws6gGl",
            "collectionId": "1rAwHJatkTNCUIN",
            "collectionName": "posts",
            "created": "2022-01-01 01:00:00.456Z",
            "updated": "2022-01-01 02:15:00.456Z",
            "title": "Lorem ipsum dolor sit...",
            "expand": {
                "comments_via_post": [
                    {
                        "id": "lmPJt4Z9CkLW36z",
                        "collectionId": "BHKW36mJl3ZPt6z",
                        "collectionName": "comments",
                        "created": "2022-01-01 01:00:00.456Z",
                        "updated": "2022-01-01 02:15:00.456Z",
                        "post": "WyAw4bDrvws6gGl",
                        "user": "FtHAW9feB5rze7D",
                        "message": "lorem ipsum...",
                        "expand": {
                            "user": {
                                "id": "FtHAW9feB5rze7D",
                                "collectionId": "srmAo0hLxEqYF7F",
                                "collectionName": "users",
                                "created": "2022-01-01 00:00:00.000Z",
                                "updated": "2022-01-01 00:00:00.000Z",
                                "username": "users54126",
                                "verified": false,
                                "emailVisibility": false,
                                "name": "John Doe"
                            }
                        }
                    },
                    {
                        "id": "tu4Z9CkLW36mPJz",
                        "collectionId": "BHKW36mJl3ZPt6z",
                        "collectionName": "comments",
                        "created": "2022-01-01 01:10:00.123Z",
                        "updated": "2022-01-01 02:39:00.456Z",
                        "post": "WyAw4bDrvws6gGl",
                        "user": "FtHAW9feB5rze7D",
                        "message": "hello...",
                        "expand": {
                            "user": {
                                "id": "FtHAW9feB5rze7D",
                                "collectionId": "srmAo0hLxEqYF7F",
                                "collectionName": "users",
                                "created": "2022-01-01 00:00:00.000Z",
                                "updated": "2022-01-01 00:00:00.000Z",
                                "username": "users54126",
                                "verified": false,
                                "emailVisibility": false,
                                "name": "John Doe"
                            }
                        }
                    },
                    ...
                ]
            }
        },
        ...
    ]
}
```

:::warning 提示

- 默认情况下，即使反向关系字段本身被标记为单一关系，反向关系引用仍会被解析为动态多关系字段。
这是因为Main Record 可能有多个单一反向关系引用（如上面的示例中，comments_via_post 展开为数组，尽管原始的 comments.post 字段是单一关系）。
只有一种情况下，反向关系将被视为单一关系字段的情形是当在关系字段上定义了唯一索引约束。

- 反向关系展开受每个关系字段最多1000条记录的限制。如果开发者需要获取更多的反向关联记录，更好的方法是向反向关联的 Collection 发送单独的分页 getList() 请求，以避免传输大量的 JSON 数据并减少内存使用。
:::
