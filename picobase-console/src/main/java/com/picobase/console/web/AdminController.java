package com.picobase.console.web;

import com.picobase.PbUtil;
import com.picobase.console.PbConsoleManager;
import com.picobase.console.model.AdminLogin;
import com.picobase.console.model.AdminLoginResult;
import com.picobase.exception.BadRequestException;
import com.picobase.exception.NotFoundException;
import com.picobase.interceptor.InterceptorFunc;
import com.picobase.interceptor.Interceptors;
import com.picobase.logic.PbAdminUtil;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.model.AdminModel;
import com.picobase.model.AdminUpsert;
import com.picobase.model.event.*;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import com.picobase.secure.BCrypt;
import com.picobase.util.CommonHelper;
import com.picobase.validator.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static com.picobase.persistence.dbx.expression.Expression.newExpr;
import static com.picobase.util.PbConstants.InnerAdminId;

@RestController
@RequestMapping("/api/admins")
public class AdminController {


    @PostMapping(value = "/auth-with-password")
    public AdminLoginResult authWithPassword() {

        AdminLogin form = PbUtil.createObjFromRequest(AdminLogin.class).get();

        /**
         *  定义  admin 登录拦截器
         */
        InterceptorFunc<AdminModel, AdminLoginResult> interceptorFunc1 = next -> adminModel -> {
            PbUtil.post(new AdminAuthWithPasswordEvent(form.getIdentity(), form.getPassword(), adminModel, TimePosition.BEFORE));

            //执行登录
            var r = next.run(adminModel);

            // 发送 TimePosition.AFTER 的 AdminAuthWithPasswordEvent 事件
            PbUtil.post(new AdminAuthWithPasswordEvent(form.getIdentity(), form.getPassword(), adminModel, TimePosition.AFTER));

            // 发送 AdminAuthRequestEvent 事件
            PbUtil.post(new AdminAuthRequestEvent(r.getToken(), adminModel));
            return r;
        };


        // 情况1、 不需要校验 ， 直接给前端设置一个登录态
        if (!PbConsoleManager.getConfig().getAuth()) {

            return Interceptors.run(new AdminModel(), (admin) -> {
                // 执行登录
                PbAdminUtil.login(InnerAdminId);


                PbTokenInfo tokenInfo = PbAdminUtil.getTokenInfo();

                return new AdminLoginResult().setToken(tokenInfo.getTokenValue()).setAdmin(admin);
            }, interceptorFunc1);

        }

        // 情况2、 需要校验 ， 配置文件或DB中配置了 admin 账号和密码

        //校验格式
        Errors errs = form.validate();
        if (errs != null) {
            throw new BadRequestException(errs);
        }

        AdminModel adminModel;

        if (CommonHelper.isNotEmpty(PbConsoleManager.getConfig().getIdentity())) {
            // 2.1 配置文件中配置了 账号密码
            adminModel = new AdminModel().setEmail(PbConsoleManager.getConfig().getIdentity()).setPasswordHash(PbConsoleManager.getConfig().getPassword());
            adminModel.setId(InnerAdminId);


        } else {
            // 2.2 配置文件中没有配置 账号密码 从数据库中读取
            adminModel = PbUtil.findOne(AdminModel.class, newExpr("email=:email", Map.of("email", form.getIdentity())));

        }


        return Interceptors.run(adminModel, (admin) -> {

            if (CommonHelper.isNotEmpty(PbConsoleManager.getConfig().getIdentity())) {
                //配置文件校验
                if (admin.getEmail().equals(form.getIdentity()) && admin.getPasswordHash().equals(form.getPassword())) {
                    // 执行登录
                    PbAdminUtil.login(InnerAdminId);


                    PbTokenInfo tokenInfo = PbAdminUtil.getTokenInfo();

                    return new AdminLoginResult().setToken(tokenInfo.getTokenValue()).setAdmin(admin);
                }
            } else {
                //数据库校验
                if (admin != null && BCrypt.checkpw(form.getPassword(), admin.getPasswordHash())) {
                    // 执行登录
                    PbAdminUtil.login(admin.getId());


                    PbTokenInfo tokenInfo = PbAdminUtil.getTokenInfo();

                    return new AdminLoginResult().setToken(tokenInfo.getTokenValue()).setAdmin(admin);
                }
            }

            throw new BadRequestException("identity or password is incorrect");

        }, interceptorFunc1);


    }


    @GetMapping
    public Page<AdminModel> list() {
        var fieldResolver = FieldResolver.newSimpleFieldResolver(
                "id", "created", "updated", "name", "email"
        );
        Page<AdminModel> admins = PbUtil.queryPage(fieldResolver, AdminModel.class);

        PbUtil.post(new AdminsListEvent(admins));

        return admins;
    }

    @GetMapping("/{id}")
    public AdminModel view(@PathVariable String id) {
        AdminModel one = PbUtil.findById(AdminModel.class, id);
        if (one == null) {
            throw new NotFoundException();
        }
        PbUtil.post(new AdminViewEvent(one));
        return one;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        AdminModel admin = PbUtil.findById(AdminModel.class, id);
        if (admin == null) {
            throw new NotFoundException();
        }

        PbUtil.post(new AdminDeleteEvent(admin, TimePosition.BEFORE));

        PbUtil.deleteById(id, AdminModel.class);

        PbUtil.post(new AdminDeleteEvent(admin, TimePosition.AFTER));

    }

    @PostMapping
    public AdminModel create() {
        Optional<AdminUpsert> adminUpsertOptional = PbUtil.createObjFromRequest(AdminUpsert.class);
        if (adminUpsertOptional.isEmpty()) {
            throw new BadRequestException("Failed to load the submitted data due to invalid formatting.");
        }

        InterceptorFunc<AdminModel, AdminModel> interceptorFunc1 = next -> adminModel -> {

            PbUtil.post(new AdminCreateEvent(adminModel, TimePosition.BEFORE));

            AdminModel model = next.run(adminModel);

            //后置拦截
            //更换为数据库save后的 model
            PbUtil.post(new AdminCreateEvent(model, TimePosition.AFTER));
            return model;
        };

        AdminUpsert form = adminUpsertOptional.get();
        Errors errors = form.validate(null);
        if (errors != null) {
            throw new BadRequestException(errors);
        }

        AdminModel admin = new AdminModel();
        admin.setEmail(form.getEmail());
        admin.setAvatar(form.getAvatar());
        admin.setPasswordHash(BCrypt.hashpw(form.getPassword()));
        admin.refreshId();
        admin.refreshCreated();
        admin.refreshUpdated();


        return Interceptors.run(admin, (adminModel) -> {
            PbUtil.save(adminModel);
            return adminModel;
        }, interceptorFunc1);
    }


    @PatchMapping("/{id}")
    public AdminModel update(@PathVariable String id) {
        AdminModel originalAdmin = PbUtil.findById(AdminModel.class, id);
        if (originalAdmin == null) {
            throw new NotFoundException();
        }

        Optional<AdminUpsert> adminUpsertOptional = PbUtil.createObjFromRequest(AdminUpsert.class);
        if (adminUpsertOptional.isEmpty()) {
            throw new BadRequestException("Failed to load the submitted data due to invalid formatting.");
        }

        InterceptorFunc<AdminModel, AdminModel> interceptorFunc1 = next -> adminModel -> {
            //前置拦截
            PbUtil.post(new AdminUpdateEvent(adminModel, TimePosition.BEFORE));

            AdminModel model = next.run(adminModel);

            //后置拦截
            //更换为数据库 update 后的 model
            PbUtil.post(new AdminUpdateEvent(model, TimePosition.AFTER));
            return model;
        };

        AdminUpsert form = adminUpsertOptional.get();

        Errors errors = form.validate(originalAdmin);
        if (errors != null) {
            throw new BadRequestException(errors);
        }


        originalAdmin.setEmail(form.getEmail());
        originalAdmin.setAvatar(form.getAvatar());
        originalAdmin.setPasswordHash(BCrypt.hashpw(form.getPassword()));
        originalAdmin.refreshUpdated();

        return Interceptors.run(originalAdmin, (adminModel) -> {
            PbUtil.updateById(adminModel.getId(), adminModel);
            return adminModel;
        }, interceptorFunc1);

    }

}
