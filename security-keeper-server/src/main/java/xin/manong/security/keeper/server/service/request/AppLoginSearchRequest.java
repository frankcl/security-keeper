package xin.manong.security.keeper.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.security.keeper.model.view.request.SearchRequest;

/**
 * 应用登录搜索请求
 *
 * @author frankcl
 * @date 2023-09-05 14:16:42
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLoginSearchRequest extends SearchRequest {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    public String userId;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    public String appId;

    /**
     * session ID
     */
    @JsonProperty("session_id")
    public String sessionId;
}
