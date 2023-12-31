package xin.manong.security.keeper.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.Role;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.security.keeper.server.service.request.RoleSearchRequest;

import javax.annotation.Resource;

/**
 * @author frankcl
 * @date 2023-10-13 15:49:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class RoleServiceImplSuite {

    @Resource
    protected RoleService roleService;

    @Test
    @Transactional
    @Rollback
    public void testRoleOperations() {
        {
            Role role = new Role();
            role.id = "xxx";
            role.name = "测试角色";
            role.appId = "app_xxx";
            Assert.assertTrue(roleService.add(role));
        }
        {
            Role role = roleService.get("xxx");
            Assert.assertTrue(role != null);
            Assert.assertEquals("测试角色", role.name);
            Assert.assertEquals("app_xxx", role.appId);
            Assert.assertTrue(role.createTime != null);
            Assert.assertTrue(role.updateTime != null);
        }
        {
            Role role = new Role();
            role.id = "xxx";
            role.name = "测试角色1";
            Assert.assertTrue(roleService.update(role));
        }
        {
            Role role = roleService.get("xxx");
            Assert.assertTrue(role != null);
            Assert.assertEquals("测试角色1", role.name);
            Assert.assertEquals("app_xxx", role.appId);
            Assert.assertTrue(role.createTime != null);
            Assert.assertTrue(role.updateTime != null);
        }
        {
            RoleSearchRequest searchRequest = new RoleSearchRequest();
            searchRequest.appId = "app_xxx";
            searchRequest.name = "测试";
            Pager<Role> pager = roleService.search(searchRequest);
            Assert.assertTrue(pager != null && pager.records.size() == 1);
        }
        {
            Assert.assertTrue(roleService.delete("xxx"));
        }
    }
}
