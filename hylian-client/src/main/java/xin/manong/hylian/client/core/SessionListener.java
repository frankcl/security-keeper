package xin.manong.hylian.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.hylian.client.common.Constants;
import xin.manong.hylian.client.config.HylianClientConfig;
import xin.manong.hylian.client.common.SessionConstants;
import xin.manong.weapon.base.http.HttpRequest;
import xin.manong.weapon.base.http.RequestFormat;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * session监听器
 *
 * @author frankcl
 * @date 2023-09-14 10:42:33
 */
@WebListener
public class SessionListener implements HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);

    private static final int MAX_SESSION_IDLE_TIME_SECONDS = 1800;

    private final HylianClientConfig hylianClientConfig;

    public SessionListener(HylianClientConfig hylianClientConfig) {
        this.hylianClientConfig = hylianClientConfig;
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        if (session == null) return;
        ReentrantLock lock = new ReentrantLock();
        session.setAttribute(SessionConstants.LOCK, lock);
        SessionManager.put(session);
        session.setMaxInactiveInterval(MAX_SESSION_IDLE_TIME_SECONDS);
        logger.info("create session[{}] success", session.getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        if (session == null) return;
        removeActivity(session.getId());
        SessionManager.remove(session);
        logger.info("destroy session[{}] success", session.getId());
    }

    /**
     * 移除服务端应用登录信息
     *
     * @param sessionId 会话ID
     */
    private void removeActivity(String sessionId) {
        String requestURL = String.format("%s%s", hylianClientConfig.serverURL, Constants.SERVER_PATH_REMOVE_ACTIVITY);
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.PARAM_SESSION_ID, sessionId);
        body.put(Constants.PARAM_APP_ID, hylianClientConfig.appId);
        body.put(Constants.PARAM_APP_SECRET, hylianClientConfig.appSecret);
        HttpRequest httpRequest = HttpRequest.buildPostRequest(requestURL, RequestFormat.JSON, body);
        Boolean success = HTTPExecutor.executeAndUnwrap(httpRequest, Boolean.class);
        if (success != null && success) return;
        logger.warn("remove app login failed for session[{}] and app[{}]", sessionId, hylianClientConfig.appId);
    }
}
