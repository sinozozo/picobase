package com.picobase;

import com.picobase.context.PbHolder;
import com.picobase.context.model.PbStorage;
import com.picobase.event.IEventReceiver;
import com.picobase.event.PbEvent;
import com.picobase.exception.PbException;
import com.picobase.listener.PbEventCenter;
import com.picobase.log.PbLog;
import com.picobase.logic.PbRecordLogic;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbLoginModel;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.model.CollectionModel;
import com.picobase.model.QueryParam;
import com.picobase.model.RecordModel;
import com.picobase.persistence.dbx.PbDbxBuilder;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.MappingOptions;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import com.picobase.scheduler.PbSchedulerBus;
import com.picobase.search.PbProvider;
import com.picobase.session.PbSession;
import com.picobase.validator.Errors;
import com.picobase.validator.FieldRules;
import com.picobase.validator.Validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.picobase.util.PbConstants.STORAGE_KEY_COLLECTION;

/**
 * PB 工具类 （面相使用者的门面类）
 */
public final class PbUtil {
    /**
     * 多账号体系下的类型标识
     */
    public static final String TYPE = "authRecord";
    private static final PbLog log = PbManager.getLog();
    /**
     * 底层使用的 PbAuthZLogic 对象
     */
    public static PbAuthZLogic pbAzLogic = new PbAuthZLogic(TYPE);
    public static PbRecordLogic pbRecordLogic = new PbRecordLogic();


    /**
     * 安全的重置 PbAuthZLogic 对象
     *
     * <br> 1、更改此账户的 PbAuthZLogic 对象
     * <br> 2、put 到全局 PbAuthZLogic 集合中
     * <br> 3、发送日志
     *
     * @param newLogic /
     */
    public static void setPbAuthZLogic(PbAuthZLogic newLogic) {
        // 1、重置此账户的 PbAuthZLogic 对象
        pbAzLogic = newLogic;

        // 2、添加到全局 PbAuthZLogic 集合中
        //    以便可以通过 PbManager.getPbAuthZLogic(type) 的方式来全局获取到这个 PbAuthZLogic
        PbManager.putPbAuthZLogic(newLogic);

        // 3、$$ 发布事件：更新了 pbAzLogic 对象

        PbEventCenter.doSetPbAuthZLogic(pbAzLogic);
    }
    // 会话查询

    /**
     * 判断当前会话是否已经登录
     *
     * @return 已登录返回 true，未登录返回 false
     */
    public static boolean isLogin() {
        return pbAzLogic.isLogin();
    }

    /**
     * 判断指定账号是否已经登录
     *
     * @return 已登录返回 true，未登录返回 false
     */
    public static boolean isLogin(Object loginId) {
        return pbAzLogic.isLogin(loginId);
    }

    // ------------------- 登录相关操作 -------------------

    // --- 登录

    /**
     * 会话登录
     *
     * @param id 账号id，建议的类型：（long | int | String）
     */
    public static void login(Object id) {
        pbAzLogic.login(id);
    }

    /**
     * 会话登录，并指定登录设备类型
     *
     * @param id     账号id，建议的类型：（long | int | String）
     * @param device 设备类型
     */
    public static void login(Object id, String device) {
        pbAzLogic.login(id, device);
    }

    /**
     * 会话登录，并指定是否 [记住我]
     *
     * @param id              账号id，建议的类型：（long | int | String）
     * @param isLastingCookie 是否为持久Cookie，值为 true 时记住我，值为 false 时关闭浏览器需要重新登录
     */
    public static void login(Object id, boolean isLastingCookie) {
        pbAzLogic.login(id, isLastingCookie);
    }

    /**
     * 会话登录，并指定此次登录 token 的有效期, 单位:秒
     *
     * @param id      账号id，建议的类型：（long | int | String）
     * @param timeout 此次登录 token 的有效期, 单位:秒
     */
    public static void login(Object id, long timeout) {
        pbAzLogic.login(id, timeout);
    }

    /**
     * 会话登录，并指定所有登录参数 Model
     *
     * @param id         账号id，建议的类型：（long | int | String）
     * @param loginModel 此次登录的参数Model
     */
    public static void login(Object id, PbLoginModel loginModel) {
        pbAzLogic.login(id, loginModel);
    }

    /**
     * 创建指定账号 id 的登录会话数据
     *
     * @param id 账号id，建议的类型：（long | int | String）
     * @return 返回会话令牌
     */
    public static String createLoginSession(Object id) {
        return pbAzLogic.createLoginSession(id);
    }

    /**
     * 创建指定账号 id 的登录会话数据
     *
     * @param id         账号id，建议的类型：（long | int | String）
     * @param loginModel 此次登录的参数Model
     * @return 返回会话令牌
     */
    public static String createLoginSession(Object id, PbLoginModel loginModel) {
        return pbAzLogic.createLoginSession(id, loginModel);
    }

    // --- 注销

    /**
     * 在当前客户端会话注销
     */
    public static void logout() {
        pbAzLogic.logout();
    }

    /**
     * 会话注销，根据账号id
     *
     * @param loginId 账号id
     */
    public static void logout(Object loginId) {
        pbAzLogic.logout(loginId);
    }

    /**
     * 会话注销，根据账号id 和 设备类型
     *
     * @param loginId 账号id
     * @param device  设备类型 (填 null 代表注销该账号的所有设备类型)
     */
    public static void logout(Object loginId, String device) {
        pbAzLogic.logout(loginId, device);
    }

    /**
     * 获取当前会话的 token 参数信息
     *
     * @return token 参数信息
     */
    public static PbTokenInfo getTokenInfo() {
        return pbAzLogic.getTokenInfo();
    }


    /**
     * 获取当前会话账号id，如果未登录，则抛出异常
     *
     * @return 账号id
     */
    public static Object getLoginId() {
        return pbAzLogic.getLoginId();
    }

    /**
     * 获取当前会话账号id, 如果未登录，则返回默认值
     *
     * @param <T>          返回类型
     * @param defaultValue 默认值
     * @return 登录id
     */
    public static <T> T getLoginId(T defaultValue) {
        return pbAzLogic.getLoginId(defaultValue);
    }

    /**
     * 获取当前会话账号id, 如果未登录，则返回null
     *
     * @return 账号id
     */
    public static Object getLoginIdDefaultNull() {
        return pbAzLogic.getLoginIdDefaultNull();
    }

    /**
     * 获取当前会话账号id, 并转换为 String 类型
     *
     * @return 账号id
     */
    public static String getLoginIdAsString() {
        return pbAzLogic.getLoginIdAsString();
    }

    /**
     * 获取当前会话账号id, 并转换为 int 类型
     *
     * @return 账号id
     */
    public static int getLoginIdAsInt() {
        return pbAzLogic.getLoginIdAsInt();
    }

    /**
     * 获取当前会话账号id, 并转换为 long 类型
     *
     * @return 账号id
     */
    public static long getLoginIdAsLong() {
        return pbAzLogic.getLoginIdAsLong();
    }

    /**
     * 获取指定 token 对应的账号id，如果未登录，则返回 null
     *
     * @param tokenValue token
     * @return 账号id
     */
    public static Object getLoginIdByToken(String tokenValue) {
        return pbAzLogic.getLoginIdByToken(tokenValue);
    }

    /**
     * 获取当前 Token 的扩展信息（此函数只在jwt模式下生效）
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    public static Object getExtra(String key) {
        return pbAzLogic.getExtra(key);
    }

    /**
     * 获取指定 Token 的扩展信息（此函数只在jwt模式下生效）
     *
     * @param tokenValue 指定的 Token 值
     * @param key        键值
     * @return 对应的扩展数据
     */
    public static Object getExtra(String tokenValue, String key) {
        return pbAzLogic.getExtra(tokenValue, key);
    }


    /**
     * 对象结构的校验
     *
     * @param obj        待校验对象
     * @param fieldRules 校验规则
     * @return 校验结果
     */
    public static Errors validate(Object obj, FieldRules... fieldRules) {
        return Validation.validateObject(obj, fieldRules);
    }

    /**
     * 将 request 数据(path params, query params and the request body)绑定到对象上 （支持提交的数据格式为 form 或 json）
     * 注意: 因为底层引用均为原始 HttpServletRequest， 针对请求体 Body 的绑定只支持一次绑定， 不支持多次绑定，
     * 如需多次读取Body体， 需自行持有PbRequest 对象,并调用 getCachedContent 方法
     *
     * @param dto 待进行数据绑定的对象
     * @see PbHolder#getRequest()
     * @see com.picobase.context.model.PbRequest#getCachedContent
     */
    public static <T> Optional<T> createObjFromRequest(Class<T> dto) {
        Optional<T> result = PbManager.getPbContext().createObjFromRequest(dto);
        return result;
    }


    public static void bindRequestTo(Object obj) {
        PbManager.getPbContext().bindRequestTo(obj);
    }

    /**
     * post 事件
     *
     * @param event
     */
    public static void post(PbEvent event) {
        PbManager.getPbEventBus().post(event);
    }

    /**
     * 注册事件接收器， 一般情况下无需用户自行注册事件接收器，Pb启动后会自行扫描EventReceiver注解的类并注册
     *
     * @param eventType 事件class
     * @param receiver  事件接收器
     */
    public static void registerEventReceiver(Class<? extends PbEvent> eventType, IEventReceiver receiver) {
        PbManager.getPbEventBus().registerEventReceiver(eventType, receiver);
    }

    /**
     * Pb线程池异步执行任务
     *
     * @param runnable
     */
    public static void asyncExecute(Runnable runnable) {
        PbManager.getPbEventBus().asyncExecute(runnable);
    }

    /**
     * Pb线程池异步执行任务
     *
     * @param executorHash 指定特定线程执行
     * @param runnable     任务
     */
    public static void asyncExecute(int executorHash, Runnable runnable) {
        PbManager.getPbEventBus().asyncExecute(executorHash, runnable);
    }

    /**
     * 提供 sql 构建和执行的能力
     *
     * @return PbDbxBuilder
     */
    public static PbDbxBuilder getPbDbxBuilder() {
        return PbManager.getPbDbxBuilder();
    }

    /**
     * 获取 mapper
     *
     * @param clazz mapper 对应的 model class
     * @param <R>   Mapper
     * @param <T>   Model
     * @return mapper
     */
    public static <R, T> R findMapper(Class<T> clazz) {
        return PbManager.getPbMapperManager().findMapper(clazz);
    }

    public static <T> Page<T> queryPage(FieldResolver resolver, SelectQuery query, Class<T> model) {
        return new PbProvider(resolver).query(query).parseAndExec(model);
    }

    public static <T> Page<T> queryPage(SelectQuery query, Class<T> model) {
        return queryPage(FieldResolver.newSimpleFieldResolver("*"), query, model);
    }

    public static <T> Page<T> queryPage(Class<T> model) {
        PbMapper mapper = findMapper(model);
        SelectQuery query = mapper.modelQuery();
        if (query == null) {
            throw new PbException("{} 未实现modelQuery方法", model.getSimpleName());
        }
        return queryPage(FieldResolver.newSimpleFieldResolver("*"), query, model);
    }

    public static <T> Page<T> queryPage(FieldResolver resolver, Class<T> model) {
        PbMapper mapper = findMapper(model);
        SelectQuery query = mapper.modelQuery();
        if (query == null) {
            throw new PbException("{} 未实现modelQuery方法", model.getSimpleName());
        }
        return queryPage(resolver, query, model);
    }

    public static <T> List<T> queryList(Class<T> model) {
        return queryList(((PbMapper) PbUtil.findMapper(model)).modelQuery(), model);
    }

    public static <T> List<T> queryList(SelectQuery query, Class<T> model) {
        return new PbProvider(FieldResolver.newSimpleFieldResolver("*")).skipTotal(true).query(query).parseAndExec(model).getItems();
    }


    public static int update(Object data, Expression where, String... includeFields) {
        PbMapper mapper = findMapper(data.getClass());
        return mapper.updateQuery(data, where, includeFields).execute();
    }

    public static int update(Object data, Expression where, MappingOptions options) {
        PbMapper mapper = findMapper(data.getClass());
        return mapper.updateQuery(data, where, options).execute();
    }


    public static int updateById(Object id, Object data) {
        return update(data, Expression.newExpr("id=:id", Map.of("id", id)));
    }

    public static int delete(Class c, Expression where) {
        PbMapper mapper = (PbMapper) findMapper(c);
        return mapper.delete(where).execute();
    }

    public static int deleteById(Object id, Class model) {
        return delete(model, Expression.newExpr("id=:id", Map.of("id", id)));
    }

    public static int save(Object data, String... includeFields) {
        PbMapper mapper = findMapper(data.getClass());
        return mapper.insertQuery(data, includeFields).execute();
    }

    public static int save(Object data, MappingOptions options) {
        PbMapper mapper = findMapper(data.getClass());
        return mapper.insertQuery(data, options).execute();
    }

    public static <T> T findOne(Class<T> c, Expression where) {
        PbMapper mapper = findMapper(c);
        return mapper.findBy(where).one(c);
    }

    public static <T> T findById(Class<T> c, Object id) {
        PbMapper mapper = findMapper(c);
        return mapper.findBy(Expression.newExpr("id=:id", Map.of("id", id))).one(c);
    }

    public static void setCollectionToStorage(CollectionModel collection) {
        // 1、获取当前请求的 Storage 存储器
        PbStorage storage = PbHolder.getStorage();
        storage.set(STORAGE_KEY_COLLECTION, collection);
    }

    /**
     * 获取当前请求线程下的 Collection，需配合LoadCollection拦截器使用
     *
     * @return CollectionModel
     */
    public static CollectionModel getCurrentCollection() {
        return (CollectionModel) PbHolder.getStorage().get(STORAGE_KEY_COLLECTION);
    }

    /**
     * 获取当前已登录账号的 Account-Session，如果该 PbSession 尚未创建，则新建并返回
     *
     * @return Session对象
     */
    public static PbSession getSession() {
        return pbAzLogic.getSession();
    }


    // 高级查询 ----------------------------

    public static <T> List<T> rQueryList(Class<T> tClass, QueryParam queryParam, MappingOptions options) {
        return pbRecordLogic.rQueryList(tClass, queryParam, options);
    }

    public static <T> List<T> rQueryList(Class<T> tClass, QueryParam queryParam, String... includeFields) {
        return pbRecordLogic.rQueryList(tClass, queryParam, includeFields);
    }

    public static <T> List<T> rQueryList(Class<T> tClass, String queryParam, String... includeFields) {
        return pbRecordLogic.rQueryList(tClass, queryParam, includeFields);
    }

    public static <T> List<T> rQueryList(Class<T> tClass, String queryParam, MappingOptions options) {
        return pbRecordLogic.rQueryList(tClass, queryParam, options);
    }

    public static <T> Page<T> rQueryPage(Class<T> tClass, QueryParam queryParam, MappingOptions options) {
        return pbRecordLogic.rQueryPage(tClass, queryParam, options);
    }

    public static <T> Page<T> rQueryPage(Class<T> tClass, String queryParam, String... includeFields) {
        return pbRecordLogic.rQueryPage(tClass, queryParam, includeFields);
    }

    public static <T> Page<T> rQueryPage(Class<T> tClass, QueryParam queryParam, String... includeFields) {
        return pbRecordLogic.rQueryPage(tClass, queryParam, includeFields);
    }

    public static <T> Page<T> rQueryPage(Class<T> tClass, String queryParams, MappingOptions options) {
        return pbRecordLogic.rQueryPage(tClass, queryParams, options);
    }

    public static Page<RecordModel> rQueryPage(CollectionModel collection, QueryParam qp, MappingOptions options) {
        return pbRecordLogic.rQueryPage(collection, qp, options);
    }

    public static <T> T rFindOne(String recordId, Class<T> tClass, String queryParams, String... includeFields) {
        return pbRecordLogic.rFindOne(recordId, tClass, queryParams, includeFields);
    }

    public static <T> T rFindOne(String recordId, Class<T> tClass, QueryParam queryParams, String... includeFields) {
        return pbRecordLogic.rFindOne(recordId, tClass, queryParams, includeFields);
    }

    public static <T> T rFindOne(String recordId, Class<T> tClass, QueryParam queryParams, MappingOptions options) {
        return pbRecordLogic.rFindOne(recordId, tClass, queryParams, options);
    }

    public static <T> T rFindOne(String recordId, Class<T> tClass, String queryParams, MappingOptions options) {
        return pbRecordLogic.rFindOne(recordId, tClass, queryParams, options);
    }

    public static RecordModel rFindOne(String recordId, CollectionModel collection, QueryParam queryParams, MappingOptions options) {
        return pbRecordLogic.rFindOne(recordId, collection, queryParams, options);
    }

    public static void download(String collectionNameOrId, String recordId, String filename) {
        pbRecordLogic.download(collectionNameOrId, recordId, filename);
    }

    /**
     * 不断执行的周期循环任务
     * nb: 单个线程执行任务，不要执行阻塞耗时任务
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long period, TimeUnit unit) {
        return PbSchedulerBus.scheduleAtFixedRate(runnable, period, unit);
    }

    /**
     * cron表达式执行的任务
     * nb: 单个线程执行任务，不要执行阻塞耗时任务
     */
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return PbSchedulerBus.schedule(runnable, delay, unit);
    }

    /**
     * cron表达式执行的任务
     * nb: 单个线程执行任务，不要执行阻塞耗时任务
     */
    public static void scheduleCron(Runnable runnable, String cron) {
        PbSchedulerBus.scheduleCron(runnable, cron);
    }
}
