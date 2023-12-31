package xin.manong.security.keeper.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Permission;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.model.RolePermission;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.request.AllRolePermissionRequest;
import xin.manong.security.keeper.server.request.RolePermissionRequest;
import xin.manong.security.keeper.server.request.RoleRequest;
import xin.manong.security.keeper.server.request.RoleUpdateRequest;
import xin.manong.security.keeper.server.service.AppService;
import xin.manong.security.keeper.server.service.PermissionService;
import xin.manong.security.keeper.server.service.RolePermissionService;
import xin.manong.security.keeper.server.service.RoleService;
import xin.manong.security.keeper.server.service.request.RolePermissionSearchRequest;
import xin.manong.security.keeper.server.service.request.RoleSearchRequest;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色控制器
 *
 * @author frankcl
 * @date 2023-10-13 16:47:25
 */
@RestController
@Controller
@Path("/role")
@RequestMapping("/role")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Resource
    protected RoleService roleService;
    @Resource
    protected PermissionService permissionService;
    @Resource
    protected RolePermissionService rolePermissionService;
    @Resource
    protected AppService appService;

    /**
     * 获取角色信息
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public Role get(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
        Role role = roleService.get(id);
        if (role == null) {
            logger.error("role[{}] is not found", id);
            throw new NotFoundException(String.format("角色[%s]不存在", id));
        }
        return role;
    }

    /**
     * 增加角色信息
     *
     * @param roleRequest 角色信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody RoleRequest roleRequest) {
        if (roleRequest == null) {
            logger.error("add role is null");
            throw new BadRequestException("增加角色信息为空");
        }
        roleRequest.check();
        Role role = Converter.convert(roleRequest);
        role.id = RandomID.build();
        role.check();
        return roleService.add(role);
    }

    /**
     * 更新角色信息
     *
     * @param roleUpdateRequest 更新角色信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public boolean update(@RequestBody RoleUpdateRequest roleUpdateRequest) {
        if (roleUpdateRequest == null) {
            logger.error("update role is null");
            throw new BadRequestException("更新角色信息为空");
        }
        roleUpdateRequest.check();
        Role role = Converter.convert(roleUpdateRequest);
        return roleService.update(role);
    }

    /**
     * 增加角色权限关系
     *
     * @param request 角色权限关系
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addRolePermission")
    @PostMapping("addRolePermission")
    @EnableWebLogAspect
    public boolean addRolePermission(@RequestBody RolePermissionRequest request) {
        if (request == null) {
            logger.error("role permission is null");
            throw new BadRequestException("角色权限关系为空");
        }
        request.check();
        RolePermission rolePermission = Converter.convert(request);
        rolePermission.check();
        return rolePermissionService.add(rolePermission);
    }

    /**
     * 删除角色权限关系
     *
     * @param id 关系ID
     * @return 成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removeRolePermission")
    @DeleteMapping("removeRolePermission")
    @EnableWebLogAspect
    public boolean removeRolePermission(@QueryParam("id") @RequestParam("id") Long id) {
        if (id == null) {
            logger.error("role permission id is null");
            throw new BadRequestException("角色权限关系ID为空");
        }
        return rolePermissionService.delete(id);
    }

    /**
     * 删除角色信息
     *
     * @param id 权限ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("role id is empty");
            throw new BadRequestException("角色ID为空");
        }
        return roleService.delete(id);
    }

    /**
     * 获取角色权限列表
     *
     * @param request 角色权限请求
     * @return 权限列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getRolePermissions")
    @PostMapping("getRolePermissions")
    @EnableWebLogAspect
    public List<Permission> getRolePermissions(AllRolePermissionRequest request) {
        if (request == null) {
            logger.error("role permission request is null");
            throw new BadRequestException("角色权限请求为空");
        }
        request.check();
        appService.verifyApp(request.appId, request.appSecret);
        RolePermissionSearchRequest searchRequest = new RolePermissionSearchRequest();
        searchRequest.roleIds = request.roleIds;
        searchRequest.current = Constants.DEFAULT_CURRENT;
        searchRequest.size = request.size;
        Pager<RolePermission> pager = rolePermissionService.search(searchRequest);
        if (pager == null || pager.records == null) return new ArrayList<>();
        List<String> permissionIds = pager.records.stream().map(r -> r.permissionId).collect(Collectors.toSet()).
                stream().collect(Collectors.toList());
        return permissionService.batchGet(permissionIds);
    }

    /**
     * 搜索角色
     *
     * @param searchRequest 搜索请求
     * @return 角色分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<Role> search(@RequestBody RoleSearchRequest searchRequest) {
        return roleService.search(searchRequest);
    }
}
