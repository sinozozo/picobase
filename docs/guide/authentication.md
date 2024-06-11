---
outline: deep
---

# 用户认证 {#authentication}

PicoBase 中使用 JWT 用于接口的身份认证，Http header 的格式为：`Authorization:  {TOKEN}`。
开发者也可以直接使用客户端 SDK 实现，代码示例如下：

- [Admin 身份认证](#admin-identity-authentication)
- [User 身份认证](#user-identity-authentication)
- [OAuth2.0 认证](#oauth2.0-authentication)

## Admin 身份认证{#admin-identity-authentication}

开发者可以使用 Admin 用户身份访问 API，Admin身份下的用户拥有最高权限，可以访问任意 API。


### 服务端{#admin-server}
对于 Admin 的身份认证，服务端使用 `PbAdminUtil` 来进行身份认证。
```java
    //服务端执行登录，获取 token
    // 执行登录
    PbAdminUtil.login(adminId);//adminId 为当前用户的id信息
    //获取 TOKEN
    PbTokenInfo tokenInfo = PbAdminUtil.getTokenInfo();
    
    //登录后从当前请求中获取 Admin 登录id
    String adminId = (String) PbAdminUtil.getLoginIdDefaultNull();
```
### 客户端{#admin-client}

::: code-group



```javascript
import PocketBase from 'pocketbase';

const pb = new PocketBase('http://127.0.0.1:8090');

...

const authData = await pb.admins.authWithPassword('test@example.com', '1234567890');

// after the above you can also access the auth data from the authStore
console.log(pb.authStore.isValid);
console.log(pb.authStore.token);
console.log(pb.authStore.model.id);

// "logout" the last authenticated account
pb.authStore.clear();
```

```dart
import 'package:pocketbase/pocketbase.dart';

final pb = PocketBase('http://127.0.0.1:8090');

...

final authData = await pb.admins.authWithPassword('test@example.com', '1234567890');

// after the above you can also access the auth data from the authStore
print(pb.authStore.isValid);
print(pb.authStore.token);
print(pb.authStore.model.id);

// "logout" the last authenticated account
pb.authStore.clear();
```

:::

## User 身份认证{#user-identity-authentication}

开发者可以使用 User 身份访问 API， User 身份是开发者创建的 [Auth Collection](./collection#authcollection),开发者可以配置这类 Collection 的 [API 访问规则](./api-rules-and-filters)。

### 服务端{#user-server}

对于 User 的身份认证，服务端使用 `PbUtil` 来进行身份认证。

```java 
    //服务端执行登录，获取 token
    // 执行登录
    PbUtil.login(userId);//userId 为当前用户的id信息
    //获取 TOKEN
    PbTokenInfo tokenInfo = PbUtil.getTokenInfo();
    
    //登录后从当前请求中获取 User 登录id
    String userId = (String) PbUtil.getLoginIdDefaultNull();
```




### 客户端{#user-client}

::: code-group
```javascript

import PocketBase from 'pocketbase';

const pb = new PocketBase('https://pocketbase.io');

...

const authData = await pb.collection('users').authWithPassword('YOUR_USERNAME_OR_EMAIL', '1234567890');

// after the above you can also access the auth data from the authStore
console.log(pb.authStore.isValid);
console.log(pb.authStore.token);
console.log(pb.authStore.model.id);

// "logout" the last authenticated model
pb.authStore.clear();

```

```dart

import 'package:pocketbase/pocketbase.dart';

final pb = PocketBase('https://pocketbase.io');

...

final authData = await pb.collection('users').authWithPassword('YOUR_USERNAME_OR_EMAIL', '1234567890');

// after the above you can also access the auth data from the authStore
print(pb.authStore.isValid);
print(pb.authStore.token);
print(pb.authStore.model.id);

// "logout" the last authenticated model
pb.authStore.clear();

```

:::

## OAuth2.0 认证{#oauth2.0-authentication}

//TODO 
