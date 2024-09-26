package xin.manong.hylian.client.core;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.common.util.HTTPUtils;
import xin.manong.hylian.common.util.SessionUtils;
import xin.manong.hylian.model.Tenant;
import xin.manong.hylian.model.User;
import xin.manong.hylian.model.Vendor;
import xin.manong.hylian.client.common.Constants;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;
import xin.manong.weapon.spring.web.WebResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Hylian盾：负责安全检测和登录认证
 *
 * @author frankcl
 * @date 2023-09-04 14:26:59
 */
public class HylianShield {

    private static final Logger logger = LoggerFactory.getLogger(HylianShield.class);

    private final String appId;
    private final String appSecret;
    private String serverURL;

    public HylianShield(String appId,
                        String appSecret,
                        String serverURL) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.serverURL = serverURL;
        if (!this.serverURL.endsWith("/")) this.serverURL += "/";
    }

    /**
     * 安全登录检测
     *
     * @param httpRequest HTTP请求
     * @param httpResponse HTTP响应
     * @return 检测通过返回true，否则返回false
     */
    public boolean shelter(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) throws IOException {
        String path = HTTPUtils.getRequestPath(httpRequest);
        if (path != null && path.equals(Constants.CLIENT_PATH_LOGOUT)) {
            httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s", serverURL,
                    Constants.SERVER_PATH_LOGOUT, Constants.PARAM_APP_ID, appId,
                    Constants.PARAM_APP_SECRET, appSecret));
            return false;
        } else if (path != null && path.equals(Constants.CLIENT_PATH_SWEEP)) {
            sweepSession(httpRequest);
            return false;
        }
        if (checkToken(httpRequest)) {
            String token = SessionUtils.getToken(httpRequest);
            if (refreshUser(token, httpRequest) &&
                    refreshTenant(token, httpRequest) &&
                    refreshVendor(token, httpRequest) &&
                    refreshToken(token, httpRequest)) return true;
        }
        SessionUtils.removeResources(httpRequest);
        String code = httpRequest.getParameter(Constants.PARAM_CODE);
        if (StringUtils.isEmpty(code)) {
            String redirectURL = HTTPUtils.getRequestURL(httpRequest);
            httpResponse.sendRedirect(String.format("%s%s?%s=%s&%s=%s&%s=%s", serverURL,
                    Constants.SERVER_PATH_APPLY_CODE, Constants.PARAM_APP_ID, appId,
                    Constants.PARAM_APP_SECRET, appSecret, Constants.PARAM_REDIRECT_URL,
                    URLEncoder.encode(redirectURL, Constants.CHARSET_UTF8)));
            return false;
        }
        String token = acquireToken(code, httpRequest);
        if (StringUtils.isEmpty(token)) {
            logger.error("acquire token failed");
            throw new NotAuthorizedException("获取令牌失败");
        }
        SessionUtils.setToken(httpRequest, token);
        String requestURL = HTTPUtils.getRequestURL(httpRequest);
        requestURL = HTTPUtils.removeQueries(requestURL, new HashSet<String>() {{ add(Constants.PARAM_CODE); }});
        httpResponse.sendRedirect(requestURL);
        return false;
    }

    /**
     * 清理登录session
     *
     * @param httpRequest HTTP请求
     */
    private void sweepSession(HttpServletRequest httpRequest) {
        Map<String, String> queryMap = HTTPUtils.getRequestQueryMap(httpRequest);
        String sessionId = queryMap.getOrDefault(Constants.PARAM_SESSION_ID, null);
        SessionManager.invalidate(sessionId);
    }

    /**
     * 检测token
     * 1. session中是否存在token
     *
     * @param httpRequest HTTP请求
     * @return 有效返回true，否则返回false
     */
    private boolean checkToken(HttpServletRequest httpRequest) {
        String token = SessionUtils.getToken(httpRequest);
        return StringUtils.isNotEmpty(token);
    }

    /**
     * 根据安全码获取token
     *
     * @param code 安全码
     * @param httpRequest HTTP请求
     * @return 成功返回token，否则返回null
     */
    private String acquireToken(String code, HttpServletRequest httpRequest) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_ACQUIRE_TOKEN);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_CODE, code);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        paramMap.put(Constants.PARAM_SESSION_ID, SessionUtils.getSessionID(httpRequest));
        HttpRequest request = HttpRequest.buildGetRequest(requestURL, paramMap);
        return HTTPExecutor.executeAndUnwrap(request, String.class);
    }

    /**
     * 根据token向服务端获取用户信息
     *
     * @param token 令牌
     * @return 成功返回用户信息，否则返回null
     */
    private User getUser(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_USER);
        Map<String, Object> paramMap = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<User> response = HTTPExecutor.execute(httpRequest, User.class);
        if (response == null || (!response.status && response.code == 404)) {
            throw new NotAuthorizedException("用户不存在");
        }
        return response.data;
    }

    /**
     * 根据token向服务端获取租户信息
     *
     * @param token 令牌
     * @return 成功返回租户信息，否则返回null
     */
    private Tenant getTenant(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_TENANT);
        Map<String, Object> paramMap = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<Tenant> response = HTTPExecutor.execute(httpRequest, Tenant.class);
        if (response == null || (!response.status && response.code == 404)) {
            throw new NotAuthorizedException("租户不存在");
        }
        return response.data;
    }

    /**
     * 根据token向服务端获取供应商信息
     *
     * @param token 令牌
     * @return 成功返回供应商信息，否则返回null
     */
    private Vendor getVendor(String token) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_GET_VENDOR);
        Map<String, Object> paramMap = buildTokenRequest(token);
        HttpRequest httpRequest = HttpRequest.buildGetRequest(requestURL, paramMap);
        WebResponse<Vendor> response = HTTPExecutor.execute(httpRequest, Vendor.class);
        if (response == null || (!response.status && response.code == 404)) {
            throw new NotAuthorizedException("供应商不存在");
        }
        return response.data;
    }

    /**
     * 刷新token
     *
     * @param token 原token
     * @param httpServletRequest HTTP请求
     * @return 刷新成功返回true，否则返回false
     */
    private boolean refreshToken(String token, HttpServletRequest httpServletRequest) {
        String requestURL = String.format("%s%s", serverURL, Constants.SERVER_PATH_REFRESH_TOKEN);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.appId = appId;
        request.appSecret = appSecret;
        request.token = token;
        Map<String, Object> body = JSON.parseObject(JSON.toJSONString(request));
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, body);
        String newToken = HTTPExecutor.executeAndUnwrap(httpRequest, String.class);
        if (StringUtils.isEmpty(newToken)) return false;
        SessionUtils.setToken(httpServletRequest, newToken);
        return true;
    }

    /**
     * 刷新session用户信息
     *
     * @param token 令牌
     * @param httpRequest HTTP请求
     * @return 成功返回true，否则返回false
     */
    private boolean refreshUser(String token, HttpServletRequest httpRequest) {
        if (SessionUtils.getUser(httpRequest) != null) return true;
        User user = getUser(token);
        if (user == null) {
            logger.error("get user failed for token[{}]", token);
            return false;
        }
        SessionUtils.setUser(httpRequest, user);
        return true;
    }

    /**
     * 刷新session租户信息
     *
     * @param token 令牌
     * @param httpRequest HTTP请求
     * @return 成功返回true，否则返回false
     */
    private boolean refreshTenant(String token, HttpServletRequest httpRequest) {
        if (SessionUtils.getTenant(httpRequest) != null) return true;
        Tenant tenant = getTenant(token);
        if (tenant == null) {
            logger.error("get tenant failed for token[{}]", token);
            return false;
        }
        SessionUtils.setTenant(httpRequest, tenant);
        return true;
    }

    /**
     * 刷新session供应商信息
     *
     * @param token 令牌
     * @param httpRequest HTTP请求
     * @return 成功返回true，否则返回false
     */
    private boolean refreshVendor(String token, HttpServletRequest httpRequest) {
        if (SessionUtils.getVendor(httpRequest) != null) return true;
        Vendor vendor = getVendor(token);
        if (vendor == null) {
            logger.error("get vendor failed for token[{}]", token);
            return false;
        }
        SessionUtils.setVendor(httpRequest, vendor);
        return true;
    }

    /**
     * 构建token请求
     *
     * @param token 令牌
     * @return token请求
     */
    private Map<String, Object> buildTokenRequest(String token) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Constants.PARAM_TOKEN, token);
        paramMap.put(Constants.PARAM_APP_ID, appId);
        paramMap.put(Constants.PARAM_APP_SECRET, appSecret);
        return paramMap;
    }
}