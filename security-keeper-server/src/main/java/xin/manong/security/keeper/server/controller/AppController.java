package xin.manong.security.keeper.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xin.manong.security.keeper.common.util.AppSecretUtils;
import xin.manong.security.keeper.model.App;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.server.converter.Converter;
import xin.manong.security.keeper.server.request.AppRequest;
import xin.manong.security.keeper.server.request.AppUpdateRequest;
import xin.manong.security.keeper.server.service.AppService;
import xin.manong.security.keeper.server.service.request.AppSearchRequest;
import xin.manong.weapon.base.util.RandomID;
import xin.manong.weapon.spring.web.ws.aspect.EnableWebLogAspect;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 应用控制器
 *
 * @author frankcl
 * @date 2023-09-05 11:58:49
 */
@RestController
@Controller
@Path("/app")
@RequestMapping("/app")
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @Resource
    protected AppService appService;

    /**
     * 获取应用信息
     *
     * @param id 应用ID
     * @return 应用信息
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    @GetMapping("get")
    @EnableWebLogAspect
    public App get(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        App app = appService.get(id);
        if (app == null) {
            logger.error("app[{}] is not found", id);
            throw new NotFoundException(String.format("应用[%s]不存在", id));
        }
        return app;
    }

    /**
     * 增加应用信息
     *
     * @param appRequest 应用信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add")
    @PostMapping("add")
    @EnableWebLogAspect
    public boolean add(@RequestBody AppRequest appRequest) {
        if (appRequest == null) {
            logger.error("add app is null");
            throw new BadRequestException("增加应用信息为空");
        }
        appRequest.check();
        App app = Converter.convert(appRequest);
        app.id = RandomID.build();
        app.check();
        return appService.add(app);
    }

    /**
     * 更新应用信息
     *
     * @param appRequest 应用信息
     * @return 成功返回true，否则返回false
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update")
    @PostMapping("update")
    @EnableWebLogAspect
    public boolean update(@RequestBody AppUpdateRequest appRequest) {
        if (appRequest == null) {
            logger.error("update app is null");
            throw new BadRequestException("更新应用信息为空");
        }
        appRequest.check();
        App app = Converter.convert(appRequest);
        return appService.update(app);
    }

    /**
     * 随机生成应用秘钥
     *
     * @return 应用秘钥
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("randomSecret")
    @GetMapping("randomSecret")
    @EnableWebLogAspect
    public String randomSecret() {
        return AppSecretUtils.buildSecret();
    }

    /**
     * 删除应用信息
     *
     * @param id 应用ID
     * @return 删除成功返回true，否则返回false
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete")
    @DeleteMapping("delete")
    @EnableWebLogAspect
    public boolean delete(@QueryParam("id") @RequestParam("id") String id) {
        if (StringUtils.isEmpty(id)) {
            logger.error("app id is empty");
            throw new BadRequestException("应用ID为空");
        }
        return appService.delete(id);
    }

    /**
     * 搜索应用
     *
     * @param searchRequest 搜索请求
     * @return 应用分页列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("search")
    @PostMapping("search")
    @EnableWebLogAspect
    public Pager<App> search(@RequestBody AppSearchRequest searchRequest) {
        return appService.search(searchRequest);
    }
}
