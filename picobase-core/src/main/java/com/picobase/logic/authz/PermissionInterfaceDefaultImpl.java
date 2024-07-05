/*
 * Copyright 2020-2099 sa-token.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.picobase.logic.authz;

import java.util.ArrayList;
import java.util.List;

/**
 * 对 {@link PbPermissionInterface} 接口默认的实现类
 * <p>
 * 如果开发者没有实现 PbPermissionInterface 接口，则框架会使用此默认实现类，所有方法都返回空集合，即：用户不具有任何权限和角色。
 */
public class PermissionInterfaceDefaultImpl implements PbPermissionInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

}
