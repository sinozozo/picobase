package com.picobase.console.web;

import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.PbAdminUtil;
import com.picobase.console.PbConsoleManager;
import com.picobase.console.error.BadRequestException;
import com.picobase.console.error.NotFoundException;
import com.picobase.console.event.*;
import com.picobase.console.interceptor.InterceptorFunc;
import com.picobase.console.interceptor.Interceptors;
import com.picobase.console.mapper.AdminMapper;
import com.picobase.console.model.dto.AdminLogin;
import com.picobase.console.model.dto.AdminLoginResult;
import com.picobase.console.model.dto.AdminUpsert;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.model.AdminModel;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.model.MapperContext;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import com.picobase.search.PbProvider;
import com.picobase.secure.BCrypt;
import com.picobase.util.CommonHelper;
import com.picobase.validator.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private AdminMapper mapper;


    public AdminController(PbMapperManager mapperManager) {
        this.mapper = mapperManager.findMapper(AdminModel.class);
    }


    @PostMapping(value = "/auth-with-password")
    public AdminLoginResult authWithPassword() {
        //从 request 中获取登录凭证
        AdminLogin form = PbUtil.bindRequest(AdminLogin.class).get();

        AdminAuthWithPasswordEvent event1 = new AdminAuthWithPasswordEvent();
        AdminAuthRequestEvent event2 = new AdminAuthRequestEvent();
        event1.identity = form.getIdentity();
        event1.password = form.getPassword();

        /**
         *  定义  admin 登录拦截器
         */
        InterceptorFunc<AdminModel, AdminLoginResult> interceptorFunc1 = next -> adminModel -> {
            // 发送 TimePosition.BEFORE 的 AdminAuthWithPasswordEvent 事件
            event1.adminModel = adminModel;
            event1.timePosition = TimePosition.BEFORE;
            PbUtil.post(event1);

            //执行登录
            var r = next.run(adminModel);

            // 发送 TimePosition.AFTER 的 AdminAuthWithPasswordEvent 事件
            event1.timePosition = TimePosition.AFTER;
            PbUtil.post(event1);

            // 发送 AdminAuthRequestEvent 事件
            event2.admin = adminModel;
            event2.token = r.getToken();
            PbUtil.post(event2);
            return r;
        };


        // 情况1、 不需要校验 ， 直接给前端设置一个登录态
        if (!PbConsoleManager.getConfig().getAuth()) {

            return Interceptors.run(new AdminModel(), (admin) -> {
                // 执行登录
                PbManager.getPbAuthZLogic("pbAdmin").login("pbAdmin");
                PbTokenInfo tokenInfo = PbManager.getPbAuthZLogic("pbAdmin").getTokenInfo();

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
            adminModel.setId("PbAdminFromConfig");


        } else {
            // 2.2 配置文件中没有配置 账号密码 从数据库中读取


            MapperContext context = new MapperContext();
            context.putWhereParameter("email", form.getIdentity());
            SelectQuery query = mapper.findAdminByEmail(context);

            adminModel = query.build().one(AdminModel.class);

        }


        return Interceptors.run(adminModel, (admin) -> {

            if (CommonHelper.isNotEmpty(PbConsoleManager.getConfig().getIdentity())) {
                //配置文件校验
                if (admin.getEmail().equals(form.getIdentity()) && admin.getPasswordHash().equals(form.getPassword())) {
                    // 执行登录
                    PbAdminUtil.login("pbAdmin");
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
        Page<AdminModel> admins = new PbProvider(fieldResolver).query(mapper.modelQuery()).parseAndExec(AdminModel.class);

        AdminsListEvent event = new AdminsListEvent();
        event.page = admins;
        PbUtil.post(event);

        return admins;
    }

    @GetMapping("/{id}")
    public AdminModel view(@PathVariable String id) {
        SelectQuery sq = mapper.findAdminById(new MapperContext().putWhereParameter("id", id));
        AdminModel one = sq.build().one(AdminModel.class);
        if (one == null) {
            throw new NotFoundException();
        }
        AdminViewEvent event = new AdminViewEvent();
        event.admin = one;
        PbUtil.post(event);
        return one;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        AdminModel admin = mapper.findAdminById(new MapperContext().putWhereParameter("id", id)).build().one(AdminModel.class);
        if (admin == null) {
            throw new NotFoundException();
        }

        AdminDeleteEvent event = new AdminDeleteEvent();
        event.admin = admin;
        event.timePosition = TimePosition.BEFORE;
        PbUtil.post(event);

        mapper.deleteAdmin(new MapperContext().putWhereParameter("id", admin.getId())).execute();

        event.timePosition = TimePosition.AFTER;
        PbUtil.post(event);

    }

    @PostMapping
    public AdminModel create() {
        Optional<AdminUpsert> adminUpsertOptional = PbUtil.bindRequest(AdminUpsert.class);
        if (adminUpsertOptional.isEmpty()) {
            throw new BadRequestException("Failed to load the submitted data due to invalid formatting.");
        }

        InterceptorFunc<AdminModel, AdminModel> interceptorFunc1 = next -> adminModel -> {
            AdminCreateEvent event = new AdminCreateEvent();
            //前置拦截
            event.admin = adminModel;
            event.timePosition = TimePosition.BEFORE;
            PbUtil.post(event);

            AdminModel model = next.run(adminModel);

            //后置拦截
            event.admin = model; //更换为数据库save后的 model
            event.timePosition = TimePosition.AFTER;
            PbUtil.post(event);
            return model;
        };

        AdminUpsert form = adminUpsertOptional.get();
        Errors errors = form.validate(null);
        if(errors!=null){
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
            mapper.saveAdmin(adminModel).execute();
            return adminModel;
        }, interceptorFunc1);
    }


    @PatchMapping("/{id}")
    public AdminModel update(@PathVariable String id) {
        AdminModel originalAdmin = mapper.findAdminById(new MapperContext().putWhereParameter("id", id)).build().one(AdminModel.class);
        if (originalAdmin == null) {
            throw new NotFoundException();
        }

        Optional<AdminUpsert> adminUpsertOptional = PbUtil.bindRequest(AdminUpsert.class);
        if (adminUpsertOptional.isEmpty()) {
            throw new BadRequestException("Failed to load the submitted data due to invalid formatting.");
        }

        InterceptorFunc<AdminModel, AdminModel> interceptorFunc1 = next -> adminModel -> {
            AdminUpdateEvent event = new AdminUpdateEvent();
            //前置拦截
            event.admin = adminModel;
            event.timePosition = TimePosition.BEFORE;
            PbUtil.post(event);

            AdminModel model = next.run(adminModel);

            //后置拦截
            event.admin = model; //更换为数据库 update 后的 model
            event.timePosition = TimePosition.AFTER;
            PbUtil.post(event);
            return model;
        };

        AdminUpsert form = adminUpsertOptional.get();

        Errors errors = form.validate(originalAdmin);
        if(errors!=null){
            throw new BadRequestException(errors);
        }


        originalAdmin.setEmail(form.getEmail());
        originalAdmin.setAvatar(form.getAvatar());
        originalAdmin.setPasswordHash(BCrypt.hashpw(form.getPassword()));
        originalAdmin.refreshUpdated();

        return Interceptors.run(originalAdmin, (adminModel) -> {
            mapper.updateAdmin(adminModel).execute();
            return adminModel;
        }, interceptorFunc1);

    }

}
