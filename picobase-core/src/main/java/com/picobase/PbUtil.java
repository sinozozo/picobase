package com.picobase;

import com.picobase.context.PbHolder;
import com.picobase.event.IEventReceiver;
import com.picobase.event.PbEvent;
import com.picobase.exception.PbException;
import com.picobase.listener.PbEventCenter;
import com.picobase.log.PbLog;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.persistence.dbx.Expression;
import com.picobase.persistence.dbx.PbDbxBuilder;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.Page;
import com.picobase.persistence.resolver.FieldResolver;
import com.picobase.search.PbProvider;
import com.picobase.validator.Errors;
import com.picobase.validator.FieldRules;
import com.picobase.validator.Validation;

import javax.management.Query;
import java.util.Map;
import java.util.Optional;

/**
 * PB 工具类 （面相使用者的门面类）
 */
public final class PbUtil {
    private static final PbLog log = PbManager.getLog();
    private PbUtil() {
    }

    /**
     * 多账号体系下的类型标识
     */
    public static final String TYPE = "user";

    /**
     * 底层使用的 PbAuthZLogic 对象
     */
    public static PbAuthZLogic pbAzLogic = new PbAuthZLogic(TYPE);


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


    /**
     * 获取当前会话的 token 参数信息
     *
     * @return token 参数信息
     */
    public static PbTokenInfo getTokenInfo() {
        return pbAzLogic.getTokenInfo();
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
     *  将 request 数据(path params, query params and the request body)绑定到对象上 （支持提交的数据格式为 form 或 json）
     *  注意: 因为底层引用均为原始 HttpServletRequest， 针对请求体 Body 的绑定只支持一次绑定， 不支持多次绑定，
     *    如需多次读取Body体， 需自行持有PbRequest 对象,并调用 getCachedContent 方法
     *
     * @see PbHolder#getRequest()
     *  @see com.picobase.context.model.PbRequest#getCachedContent
     *  @param dto 待进行数据绑定的对象
     */
    public static <T> Optional<T> createObjFromRequest(Class<T> dto){
        Optional<T> result = PbManager.getPbContext().createObjFromRequest(dto);
        if(result.isPresent()) {
            log.debug("bindRequest: {}", result.get());
        }
        return  result;
    }


    public static void bindRequestTo(Object obj){
        PbManager.getPbContext().bindRequestTo(obj);
    }

    /**
     * post 事件
     * @param event
     */
    public static void post(PbEvent event){
        PbManager.getPbEventBus().post(event);
    }

    /**
     * 注册事件接收器， 一般情况下无需用户自行注册事件接收器，Pb启动后会自行扫描EventReceiver注解的类并注册
     *
     * @param eventType 事件class
     * @param receiver  事件接收器
     */
    public static void registerEventReceiver(Class<? extends PbEvent> eventType, IEventReceiver receiver){
        PbManager.getPbEventBus().registerEventReceiver(eventType,receiver);
    }

    /**
     * Pb线程池异步执行任务
     * @param runnable
     */
    public static void asyncExecute(Runnable runnable){
        PbManager.getPbEventBus().asyncExecute(runnable);
    }

    /**
     * Pb线程池异步执行任务
     * @param executorHash 指定特定线程执行
     * @param runnable 任务
     *
     */
    public static void asyncExecute(int executorHash, Runnable runnable){
        PbManager.getPbEventBus().asyncExecute(executorHash, runnable);
    }

    /**
     * 提供 sql 构建和执行的能力
     * @return PbDbxBuilder
     */
    public static PbDbxBuilder getPbDbxBuilder(){
        return PbManager.getPbDbxBuilder();
    }

    /**
     * 获取 mapper
     * @param clazz mapper 对应的 model class
     * @return mapper
     * @param <R> Mapper
     * @param <T> Model
     */
    public static <R,T> R findMapper(Class<T> clazz) {
        return PbManager.getPbMapperManager().findMapper(clazz);
    }

    public static <T> Page<T> query(FieldResolver resolver, SelectQuery query, Class<T> model){
        return new PbProvider(resolver).query(query).parseAndExec(model);
    }

    public static <T> Page<T> query( SelectQuery query, Class<T> model){
        return query(FieldResolver.newSimpleFieldResolver("*"), query, model);
    }

    public static <T> Page<T> query(Class<T> model){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(model);
        SelectQuery query = mapper.modelQuery();
        if (query == null) {
            throw new PbException("{} 未实现modelQuery方法",model.getSimpleName());
        }
        return query(FieldResolver.newSimpleFieldResolver("*"), query, model);
    }

    public static <T> Page<T> query(FieldResolver resolver,Class<T> model){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(model);
        SelectQuery query = mapper.modelQuery();
        if (query == null) {
            throw new PbException("{} 未实现modelQuery方法",model.getSimpleName());
        }
        return query(resolver, query, model);
    }

    public static int update(Object data, Expression where){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(data.getClass());
        return mapper.update(data, where).execute();
    }



    public static int updateById(Object id,Object data){
        return update(data,Expression.newExpr("id=:id", Map.of("id", id)));
    }

    public static int delete(Class c,Expression where){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(c);
        return mapper.delete(where).execute();
    }

    public static int deleteById(Object id ,Class model){
        return delete(model,Expression.newExpr("id=:id", Map.of("id", id)));
    }

    public static int insert(Object data){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(data.getClass());
        return mapper.insert(data).execute();
    }

    public static <T> T findOne(Class<T> c,Expression where){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(c);
        return mapper.findBy(where).one(c);
    }
    public static <T> T findById(Class<T> c,Object id ){
        PbMapper mapper = PbManager.getPbMapperManager().findMapper(c);
        return mapper.findBy(Expression.newExpr("id=:id", Map.of("id", id))).one(c);
    }

}
