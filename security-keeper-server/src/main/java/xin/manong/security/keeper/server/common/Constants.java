package xin.manong.security.keeper.server.common;

/**
 * 常量定义
 *
 * @author frankcl
 * @date 2023-09-01 13:48:24
 */
public class Constants {

    public static final int DEFAULT_CURRENT = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int LOCAL_CACHE_CAPACITY_CODE = 200;

    public static final Long CACHE_CODE_EXPIRED_TIME_MS = 60000L;
    public static final Long CACHE_TOKEN_EXPIRED_TIME_MS = 600000L;
    public static final Long CACHE_TICKET_EXPIRED_TIME_MS = 1800000L;
    public static final Long COOKIE_TICKET_EXPIRED_TIME_MS = 86400000L;

    public static final String CODE_CACHE_PREFIX = "__SK_CODE_";
    public static final String TOKEN_CACHE_PREFIX = "__SK_TOKEN_";
    public static final String TICKET_CACHE_PREFIX = "__SK_TICKET_";
    public static final String TICKET_TOKEN_PREFIX = "__SK_TICKET_TOKEN_";

    public static final String COOKIE_TICKET = "TICKET";

    public static final String PATH_HOME = "/home/index";
    public static final String PATH_LOGIN = "/home/login";
    public static final String PATH_LOGOUT = "/logout";

    public static final String PARAM_CODE = "code";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_TENANT = "tenant";
    public static final String PARAM_VENDOR = "vendor";
    public static final String PARAM_USER_NAME = "user_name";
    public static final String PARAM_LOGOUT_URL = "logout_url";
    public static final String PARAM_REDIRECT_URL = "redirect_url";
    public static final String PARAM_SESSION_ID = "session_id";

    public static final String JWT_HEADER_ALGORITHM = "alg";
    public static final String JWT_HEADER_CATEGORY = "category";
    public static final String JWT_CATEGORY_TICKET = "ticket";
    public static final String JWT_CATEGORY_TOKEN = "token";
    public static final String JWT_CLAIM_PROFILE = "profile";

    public static final String ALGORITHM_HS256 = "HS256";
    public static final String CHARSET_UTF8 = "UTF-8";
}
