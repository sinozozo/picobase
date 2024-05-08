package com.picobase.listener;

import com.picobase.config.PbConfig;
import com.picobase.error.PbErrorCode;
import com.picobase.exception.PbException;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbLoginModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Pb 事件中心 事件发布器
 *
 * <p> 提供侦听器注册、事件发布能力 </p>
 */
public class PbEventCenter {

	// --------- 注册侦听器

	private static List<PbListener> listenerList = new ArrayList<>();

	static {
		// 默认添加控制台日志侦听器
		listenerList.add(new PbListenerForLog());
	}

	/**
	 * 获取已注册的所有侦听器
	 *
	 * @return /
	 */
	public static List<PbListener> getListenerList() {
		return listenerList;
	}

	/**
	 * 重置侦听器集合
	 *
	 * @param listenerList /
	 */
	public static void setListenerList(List<PbListener> listenerList) {
		if (listenerList == null) {
			throw new PbException("重置的侦听器集合不可以为空").setCode(PbErrorCode.CODE_10031);
		}
		PbEventCenter.listenerList = listenerList;
	}

	/**
	 * 注册一个侦听器
	 *
	 * @param listener /
	 */
	public static void registerListener(PbListener listener) {
		if (listener == null) {
			throw new PbException("注册的侦听器不可以为空").setCode(PbErrorCode.CODE_10032);
		}
		listenerList.add(listener);
	}

	/**
	 * 注册一组侦听器
	 *
	 * @param listenerList /
	 */
	public static void registerListenerList(List<PbListener> listenerList) {
		if (listenerList == null) {
			throw new PbException("注册的侦听器集合不可以为空").setCode(PbErrorCode.CODE_10031);
		}
		for (PbListener listener : listenerList) {
			if (listener == null) {
				throw new PbException("注册的侦听器不可以为空").setCode(PbErrorCode.CODE_10032);
			}
		}
		PbEventCenter.listenerList.addAll(listenerList);
	}

	/**
	 * 移除一个侦听器
	 *
	 * @param listener /
	 */
	public static void removeListener(PbListener listener) {
		listenerList.remove(listener);
	}

	/**
	 * 移除指定类型的所有侦听器
	 *
	 * @param cls /
	 */
	public static void removeListener(Class<? extends PbListener> cls) {
		ArrayList<PbListener> listenerListCopy = new ArrayList<>(listenerList);
		for (PbListener listener : listenerListCopy) {
			if (cls.isAssignableFrom(listener.getClass())) {
				listenerList.remove(listener);
			}
		}
	}

	/**
	 * 清空所有已注册的侦听器
	 */
	public static void clearListener() {
		listenerList.clear();
	}

	/**
	 * 判断是否已经注册了指定侦听器
	 *
	 * @param listener /
	 * @return /
	 */
	public static boolean hasListener(PbListener listener) {
		return listenerList.contains(listener);
	}

	/**
	 * 判断是否已经注册了指定类型的侦听器
	 *
	 * @param cls /
	 * @return /
	 */
	public static boolean hasListener(Class<? extends PbListener> cls) {
		for (PbListener listener : listenerList) {
			if (cls.isAssignableFrom(listener.getClass())) {
				return true;
			}
		}
		return false;
	}


	// --------- 事件发布

	/**
	 * 事件发布：有新的全局配置载入到框架中
	 *
	 * @param config /
	 */
	public static void doSetConfig(PbConfig config) {
		for (PbListener listener : listenerList) {
			listener.doSetConfig(config);
		}
	}

	/**
	 * 事件发布：有新的全局组件载入到框架中
	 *
	 * @param compName 组件名称
	 * @param compObj  组件对象
	 */
	public static void doRegisterComponent(String compName, Object compObj) {
		for (PbListener listener : listenerList) {
			listener.doRegisterComponent(compName, compObj);
		}
	}


	/**
	 * 事件发布：创建了一个新的 SaSession
	 *
	 * @param id SessionId
	 */
	public static void doCreateSession(String id) {
		for (PbListener listener : listenerList) {
			listener.doCreateSession(id);
		}
	}

	/**
	 * 事件发布：一个 SaSession 注销了
	 *
	 * @param id SessionId
	 */
	public static void doLogoutSession(String id) {
		for (PbListener listener : listenerList) {
			listener.doLogoutSession(id);
		}
	}

	/**
	 * 事件发布：xx 账号被顶下线
	 *
	 * @param loginType  账号类别
	 * @param loginId    账号id
	 * @param tokenValue token值
	 */
	public static void doReplaced(String loginType, Object loginId, String tokenValue) {
		for (PbListener listener : listenerList) {
			listener.doReplaced(loginType, loginId, tokenValue);
		}
	}

	/**
	 * 事件发布：xx 账号被解封
	 *
	 * @param loginType 账号类别
	 * @param loginId   账号id
	 * @param service   指定服务
	 */
	public static void doUntieDisable(String loginType, Object loginId, String service) {
		for (PbListener listener : listenerList) {
			listener.doUntieDisable(loginType, loginId, service);
		}
	}

	/**
	 * 事件发布：xx 账号被封禁
	 *
	 * @param loginType   账号类别
	 * @param loginId     账号id
	 * @param service     指定服务
	 * @param level       封禁等级
	 * @param disableTime 封禁时长，单位: 秒
	 */
	public static void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
		for (PbListener listener : listenerList) {
			listener.doDisable(loginType, loginId, service, level, disableTime);
		}
	}

	/**
	 * 事件发布：xx 账号完成二级认证
	 *
	 * @param loginType  账号类别
	 * @param tokenValue token值
	 * @param service    指定服务
	 * @param safeTime   认证时间，单位：秒
	 */
	public static void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
		for (PbListener listener : listenerList) {
			listener.doOpenSafe(loginType, tokenValue, service, safeTime);
		}
	}

	/**
	 * 事件发布：xx 账号关闭二级认证
	 *
	 * @param loginType  账号类别
	 * @param service    指定服务
	 * @param tokenValue token值
	 */
	public static void doCloseSafe(String loginType, String tokenValue, String service) {
		for (PbListener listener : listenerList) {
			listener.doCloseSafe(loginType, tokenValue, service);
		}
	}

	/**
	 * 事件发布：xx 账号登录
	 *
	 * @param loginType  账号类别
	 * @param loginId    账号id
	 * @param tokenValue 本次登录产生的 token 值
	 * @param loginModel 登录参数
	 */
	public static void doLogin(String loginType, Object loginId, String tokenValue, PbLoginModel loginModel) {
		for (PbListener listener : listenerList) {
			listener.doLogin(loginType, loginId, tokenValue, loginModel);
		}
	}

	/**
	 * 事件发布：xx 账号注销
	 *
	 * @param loginType  账号类别
	 * @param loginId    账号id
	 * @param tokenValue token值
	 */
	public static void doLogout(String loginType, Object loginId, String tokenValue) {
		for (PbListener listener : listenerList) {
			listener.doLogout(loginType, loginId, tokenValue);
		}
	}

	/**
	 * 事件发布：xx 账号被踢下线
	 *
	 * @param loginType  账号类别
	 * @param loginId    账号id
	 * @param tokenValue token值
	 */
	public static void doKickout(String loginType, Object loginId, String tokenValue) {
		for (PbListener listener : listenerList) {
			listener.doKickout(loginType, loginId, tokenValue);
		}
	}

	/**
	 * 事件发布：指定 Token 续期成功
	 *
	 * @param tokenValue token 值
	 * @param loginId    账号id
	 * @param timeout    续期时间
	 */
	public static void doRenewTimeout(String tokenValue, Object loginId, long timeout) {
		for (PbListener listener : listenerList) {
			listener.doRenewTimeout(tokenValue, loginId, timeout);
		}
	}

	/**
	 * 事件发布：有新的 PbAuthZLogic 载入到框架中
	 * @param logic /
	 */
	public static void doSetPbAuthZLogic(PbAuthZLogic logic) {
		for (PbListener listener : listenerList) {
			listener.doSetPbAuthZLogic(logic);
		}
	}
}
