package xin.manong.security.keeper.sso.client.core;

import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.Tenant;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.Vendor;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.weapon.base.common.Context;

import javax.servlet.http.HttpServletRequest;

/**
 * 线程上下文管理器
 *
 * @author frankcl
 * @date 2023-09-27 14:53:07
 */
public class ContextManager {

    private final static ThreadLocal<Context> THREAD_LOCAL_CONTEXT = new ThreadLocal<>();

    /**
     * 获取用户信息
     *
     * @return 成功返回用户信息，否则返回null
     */
    public static User getUser() {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return null;
        return (User) context.get(Constants.CURRENT_USER);
    }

    /**
     * 获取租户信息
     *
     * @return 成功返回租户信息，否则返回null
     */
    public static Tenant getTenant() {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return null;
        return (Tenant) context.get(Constants.CURRENT_TENANT);
    }

    /**
     * 获取供应商信息
     *
     * @return 成功返回供应商信息，否则返回null
     */
    public static Vendor getVendor() {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) return null;
        return (Vendor) context.get(Constants.CURRENT_VENDOR);
    }

    /**
     * 填充线程上下文
     * 1. 填充用户信息
     * 2. 填充租户信息
     * 3. 填充供应商信息
     *
     * @param httpRequest HTTP请求
     */
    public static void fillContext(HttpServletRequest httpRequest) {
        Context context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) {
            context = new Context();
            THREAD_LOCAL_CONTEXT.set(context);
        }
        context.put(Constants.CURRENT_USER, SessionUtils.getUser(httpRequest));
        context.put(Constants.CURRENT_TENANT, SessionUtils.getTenant(httpRequest));
        context.put(Constants.CURRENT_VENDOR, SessionUtils.getVendor(httpRequest));
    }

    /**
     * 清除线程上下文
     */
    public static void sweepContext() {
        if (THREAD_LOCAL_CONTEXT.get() == null) return;
        THREAD_LOCAL_CONTEXT.remove();
    }
}
