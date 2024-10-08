package xin.manong.hylian.server.service;

import xin.manong.hylian.server.model.UserProfile;

/**
 * token服务接口定义
 *
 * @author frankcl
 * @date 2023-09-01 16:27:05
 */
public interface TokenService {

    /**
     * 验证token
     *
     * @param token 令牌
     * @return 有效返回true，否则返回false
     */
    boolean verifyToken(String token);

    /**
     * 构建token
     *
     * @param userProfile 用户信息
     * @param expiredTime 过期时间，单位：毫秒
     * @return 成功返回token，否则返回null
     */
    String buildToken(UserProfile userProfile, Long expiredTime);

    /**
     * 根据token获取ticket
     *
     * @param token 令牌
     * @return 存在返回ticket，否则返回null
     */
    String getTicket(String token);

    /**
     * 添加token和ticket映射
     *
     * @param token 令牌
     * @param ticket 票据
     */
    void putToken(String token, String ticket);

    /**
     * 移除token和ticket映射
     *
     * @param token 令牌
     */
    void removeToken(String token);

    /**
     * 根据ID移除token和ticket映射
     *
     * @param tokenId 令牌ID
     */
    void removeTokenWithId(String tokenId);
}
