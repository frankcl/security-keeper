package xin.manong.security.keeper.server.service;

import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.User;
import xin.manong.security.keeper.model.view.request.UserSearchRequest;

/**
 * 用户信息服务接口定义
 *
 * @author frankcl
 * @date 2023-08-29 17:46:50
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 成功返回用户信息，否则返回null
     */
    User get(String id);

    /**
     * 根据用户名获取用户信息
     *
     * @param userName 用户名
     * @return 成功返回用户信息，否则返回null
     */
    User getByUserName(String userName);

    /**
     * 添加用户信息
     *
     * @param user 用户信息
     * @return 成功返回true，否则返回false
     */
    boolean add(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 成功返回true，否则返回false
     */
    boolean update(User user);

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     * @return 成功返回true，否则返回false
     */
    boolean delete(String id);

    /**
     * 搜索用户信息
     *
     * @param searchRequest 搜索请求
     * @return 用户分页列表
     */
    Pager<User> search(UserSearchRequest searchRequest);
}
