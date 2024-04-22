package com.picobase.session;

import com.picobase.PbManager;
import com.picobase.strategy.PbStrategy;
import com.picobase.util.PbConstants;

/**
 * 自定义 PbSession 工具类，快捷的读取、操作自定义 PbSession
 *
 * <p>样例：
 * <pre>
 * 		// 在一处代码写入数据
 * 		PbSession session = PbSessionCustomUtil.getSessionById("role-" + 1001);
 * 		session.set("count", 1);
 *
 * 		// 在另一处代码获取数据
 * 		PbSession session = PbSessionCustomUtil.getSessionById("role-" + 1001);
 * 		int count = session.getInt("count");
 * 		System.out.println("count=" + count);
 * </pre>
 */
public class PbSessionCustomUtil {

	private PbSessionCustomUtil() {
	}

	/**
	 * 添加上指定前缀，防止恶意伪造数据
	 */
	public static String sessionKey = "custom";

	/**
	 * 拼接Key: 在存储自定义 PbSession 时应该使用的 key
	 *
	 * @param sessionId 会话id
	 * @return sessionId
	 */
	public static String splicingSessionKey(String sessionId) {
		return PbManager.getConfig().getTokenName() + ":" + sessionKey + ":session:" + sessionId;
	}

	/**
	 * 判断：指定 key 的 PbSession 是否存在
	 *
	 * @param sessionId PbSession 的 id
	 * @return 是否存在
	 */
	public static boolean isExists(String sessionId) {
		return PbManager.getPbCache().getSession(splicingSessionKey(sessionId)) != null;
	}

	/**
	 * 获取指定 key 的 PbSession 对象, 如果此 PbSession 尚未在 DB 创建，isCreate 参数代表是否则新建并返回
	 *
	 * @param sessionId PbSession 的 id
	 * @param isCreate  如果此 PbSession 尚未在 DB 创建，是否新建并返回
	 * @return PbSession 对象
	 */
	public static PbSession getSessionById(String sessionId, boolean isCreate) {
		PbSession session = PbManager.getPbCache().getSession(splicingSessionKey(sessionId));
		if (session == null && isCreate) {
			session = PbStrategy.instance.createSession.apply(splicingSessionKey(sessionId));
			session.setType(PbConstants.SESSION_TYPE__CUSTOM);
			PbManager.getPbCache().setSession(session, PbManager.getConfig().getTimeout());
		}
		return session;
	}

	/**
	 * 获取指定 key 的 PbSession, 如果此 PbSession 尚未在 DB 创建，则新建并返回
	 *
	 * @param sessionId PbSession 的 id
	 * @return PbSession 对象
	 */
	public static PbSession getSessionById(String sessionId) {
		return getSessionById(sessionId, true);
	}

	/**
	 * 删除指定 key 的 PbSession
	 *
	 * @param sessionId PbSession 的 id
	 */
	public static void deleteSessionById(String sessionId) {
		PbManager.getPbCache().deleteSession(splicingSessionKey(sessionId));
	}

}
