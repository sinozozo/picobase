package com.picobase.console.web;

import cn.hutool.core.util.StrUtil;
import com.picobase.PbUtil;
import com.picobase.console.model.RecordAuthResponse;
import com.picobase.console.model.RecordPasswordLogin;
import com.picobase.console.web.interceptor.LoadCollection;
import com.picobase.exception.BadRequestException;
import com.picobase.interceptor.InterceptorFunc;
import com.picobase.interceptor.Interceptors;
import com.picobase.logic.authz.PbLoginModel;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.logic.mapper.ExternalAuthMapper;
import com.picobase.logic.mapper.RecordMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.ExternalAuthModel;
import com.picobase.model.RecordModel;
import com.picobase.model.event.RecordAuthWithPasswordEvent;
import com.picobase.model.event.TimePosition;
import com.picobase.util.PbConstants;
import com.picobase.validator.Errors;
import com.picobase.validator.Is;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.picobase.util.PbConstants.JwtExtraFieldCollectionId;


@RestController
@RequestMapping("/api/collections/{collectionIdOrName}")
public class RecordAuthCollection {

    private RecordMapper recordMapper;
    private ExternalAuthMapper externalAuthMapper;

    public RecordAuthCollection(RecordMapper recordMapper, ExternalAuthMapper externalAuthMapper) {
        this.recordMapper = recordMapper;
        this.externalAuthMapper = externalAuthMapper;
    }

    @LoadCollection(optCollectionTypes = {PbConstants.CollectionType.Auth})
    @PostMapping("/auth-with-password")
    public RecordAuthResponse authWithPassword() {
        CollectionModel collection = PbUtil.getCurrentCollection();
        RecordPasswordLogin form = PbUtil.createObjFromRequest(RecordPasswordLogin.class).get();
        /**
         *  定义  RecordAuth 登录拦截器
         */
        InterceptorFunc<RecordModel, RecordAuthResponse> interceptorFunc1 = next -> recordModel -> {
            PbUtil.post(new RecordAuthWithPasswordEvent(collection, form.getIdentity(), form.getPassword(), recordModel, TimePosition.BEFORE));
            //执行登录
            var r = next.run(recordModel);

            PbUtil.post(new RecordAuthWithPasswordEvent(collection, form.getIdentity(), form.getPassword(), recordModel, TimePosition.AFTER));

            return r;
        };

        Errors errs = form.validate();
        if (errs != null) {
            throw new BadRequestException(errs);
        }

        var authOptions = collection.authOptions();
        var isEmail = Is.EmailFormat.validate(form.getIdentity()) == null;
        RecordModel authRecord = null;
        if (isEmail) {
            if (authOptions.isAllowEmailAuth()) {
                authRecord = recordMapper.findAuthRecordByEmail(collection, form.getIdentity());
            }

        } else if (authOptions.isAllowUsernameAuth()) {
            authRecord = recordMapper.findAuthRecordByUsername(collection, form.getIdentity());
        }


        return Interceptors.run(authRecord, (record) -> {
            if (record == null || !record.validatePassword(form.getPassword())) {
                throw new BadRequestException("Failed to authenticate.");
            }
            //执行登录
            PbUtil.login(record.getId(), new PbLoginModel().setExtra(JwtExtraFieldCollectionId, collection.getId())); // authRecord 登录，token结构中要包含 Collection信息
            PbTokenInfo tokenInfo = PbUtil.getTokenInfo();

            return new RecordAuthResponse(tokenInfo.getTokenValue(), record, null);
        }, interceptorFunc1);


    }


    @GetMapping("/records/{id}/external-auths")
    @LoadCollection(optCollectionTypes = {PbConstants.CollectionType.Auth})
    public List<ExternalAuthModel> list(@PathVariable String id) {

        if (StrUtil.isEmpty(id)) {
            throw new BadRequestException("");
        }
        CollectionModel collection = PbUtil.getCurrentCollection();
        RecordModel record = recordMapper.findRecordById(collection.getId(), id).orElseThrow(() -> new BadRequestException(""));

        List<ExternalAuthModel> allExternalAuthsByRecord = externalAuthMapper.findAllExternalAuthsByRecord(record);

        return allExternalAuthsByRecord;
    }
}
