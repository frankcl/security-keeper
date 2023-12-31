package xin.manong.security.keeper.sso.client.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xin.manong.security.keeper.common.util.SessionUtils;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.view.request.UserSearchRequest;
import xin.manong.security.keeper.model.view.response.ViewUser;
import xin.manong.security.keeper.sso.client.common.Constants;
import xin.manong.security.keeper.sso.client.config.AppClientConfig;
import xin.manong.security.keeper.sso.client.core.HTTPExecutor;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务支持
 *
 * @author frankcl
 * @date 2024-01-04 16:03:31
 */
@Component
public class UserServiceSupport {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceSupport.class);

    private static final int CACHE_CAPACITY = 300;

    protected Cache<String, Long> rolesCache;
    protected Cache<String, Long> permissionsCache;
    @Resource
    protected AppClientConfig appClientConfig;

    public UserServiceSupport() {
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .removalListener(n -> logger.info("roles are removed from cache for user[{}]", n.getKey()));
            rolesCache = builder.build();
        }
        {
            CacheBuilder<String, Long> builder = CacheBuilder.newBuilder()
                    .concurrencyLevel(1)
                    .maximumSize(CACHE_CAPACITY)
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .removalListener(n -> logger.info("permissions are removed from cache for user[{}]", n.getKey()));
            permissionsCache = builder.build();
        }
    }

    /**
     * 获取用户角色列表
     * 优先从http session中获取，session不存在查询服务获取
     *
     * @param user 用户信息
     * @param httpRequest HTTP请求
     * @return 角色列表
     */
    public List<Role> getUserRoles(User user, HttpServletRequest httpRequest) {
        if (httpRequest == null) throw new IllegalArgumentException("http request is null");
        List<Role> roles = SessionUtils.getRoles(httpRequest);
        if (roles != null && rolesCache.getIfPresent(user.id) != null) return roles;
        roles = getUserRoles(user);
        SessionUtils.setRoles(httpRequest, roles);
        rolesCache.put(user.id, System.currentTimeMillis());
        return roles;
    }

    /**
     * 获取用户权限列表
     * 优先从http session中获取，session不存在查询服务获取
     *
     * @param user 用户信息
     * @param httpRequest HTTP请求
     * @return 权限列表
     */
    public List<Permission> getUserPermissions(User user, HttpServletRequest httpRequest) {
        if (httpRequest == null) throw new IllegalArgumentException("http request is null");
        List<Permission> permissions = SessionUtils.getPermissions(httpRequest);
        if (permissions != null && permissionsCache.getIfPresent(user.id) != null) return permissions;
        permissions = getUserPermissions(user);
        SessionUtils.setPermissions(httpRequest, permissions);
        permissionsCache.put(user.id, System.currentTimeMillis());
        return permissions;
    }

    /**
     * 搜索用户信息
     *
     * @param searchRequest 搜索请求
     * @return 成功返回分页用户信息，否则返回null
     */
    public Pager<ViewUser> searchUsers(UserSearchRequest searchRequest) {
        String requestURL = String.format("%s%s", appClientConfig.serverURL,
                Constants.SERVER_PATH_SEARCH_USER);
        Map<String, Object> requestBody = JSON.parseObject(JSON.toJSONString(searchRequest));
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        Pager<JSONObject> pager = HTTPExecutor.execute(httpRequest, Pager.class);
        if (pager == null) {
            logger.error("search users failed");
            return null;
        }
        Pager<ViewUser> returnPager = new Pager<>();
        returnPager.current = pager.current;
        returnPager.size = pager.size;
        returnPager.total = pager.total;
        returnPager.records = new ArrayList<>();
        for (JSONObject record : pager.records) returnPager.records.add(JSON.toJavaObject(record, ViewUser.class));
        return returnPager;
    }

    /**
     * 获取用户角色列表
     *
     * @param user 用户信息
     * @return 角色列表
     */
    public List<Role> getUserRoles(User user) {
        String requestURL = String.format("%s%s", appClientConfig.serverURL,
                Constants.SERVER_PATH_GET_APP_USER_ROLES);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_APP_ID, appClientConfig.appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appClientConfig.appSecret);
        paramMap.put(Constants.PARAM_USER_ID, user.id);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        List<JSONObject> records = HTTPExecutor.execute(httpRequest, List.class);
        if (records == null || records.isEmpty()) {
            logger.error("get roles failed or roles not existed for user[{}] and app[{}]",
                    user.id, appClientConfig.appId);
            return new ArrayList<>();
        }
        List<Role> roles = new ArrayList<>();
        for (JSONObject record : records) roles.add(JSON.toJavaObject(record, Role.class));
        return roles;
    }

    /**
     * 获取用户权限列表
     *
     * @param user 用户信息
     * @return 权限列表
     */
    public List<Permission> getUserPermissions(User user) {
        List<Role> roles = getUserRoles(user);
        if (roles == null || roles.isEmpty()) return new ArrayList<>();
        List<String> roleIds = roles.stream().map(role -> role.id).collect(Collectors.toList());
        String requestURL = String.format("%s%s", appClientConfig.serverURL,
                Constants.SERVER_PATH_GET_ROLE_PERMISSIONS);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.PARAM_ROLE_IDS, roleIds);
        requestBody.put(Constants.PARAM_APP_ID, appClientConfig.appId);
        requestBody.put(Constants.PARAM_APP_SECRET, appClientConfig.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, requestBody);
        List<JSONObject> records = HTTPExecutor.execute(httpRequest, List.class);
        if (records == null || records.isEmpty()) {
            logger.error("get permissions failed or permissions not existed for user[{}] and app[{}]",
                    user.id, appClientConfig.appId);
            return new ArrayList<>();
        }
        List<Permission> permissions = new ArrayList<>();
        for (JSONObject record : records) permissions.add(JSON.toJavaObject(record, Permission.class));
        return permissions;
    }
}
