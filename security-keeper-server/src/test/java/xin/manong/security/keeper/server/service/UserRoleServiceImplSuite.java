package xin.manong.security.keeper.server.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xin.manong.security.keeper.model.Pager;
import xin.manong.security.keeper.model.UserRole;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.server.service.request.UserRoleSearchRequest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-10-16 14:11:31
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class UserRoleServiceImplSuite {

    @Resource
    protected UserRoleService userRoleService;

    @Test
    @Transactional
    @Rollback
    public void testUserRoleOperations() {
        List<Long> ids = new ArrayList<>();
        {
            UserRole userRole = new UserRole();
            userRole.userId = "user_xxx";
            userRole.roleId = "role_xxx";
            userRole.appId = "test_app";
            Assert.assertTrue(userRoleService.add(userRole));
            ids.add(userRole.id);
        }
        {
            UserRole userRole = new UserRole();
            userRole.userId = "user_xxx";
            userRole.roleId = "role_yyy";
            userRole.appId = "test_app";
            Assert.assertTrue(userRoleService.add(userRole));
            ids.add(userRole.id);
        }
        {
            UserRole userRole = new UserRole();
            userRole.userId = "user_yyy";
            userRole.roleId = "role_xxx";
            userRole.appId = "test_app";
            Assert.assertTrue(userRoleService.add(userRole));
            ids.add(userRole.id);
        }
        {
            UserRoleSearchRequest searchRequest = new UserRoleSearchRequest();
            searchRequest.userId = "user_xxx";
            searchRequest.appId = "test_app";
            Pager<UserRole> pager = userRoleService.search(searchRequest);
            Assert.assertTrue(pager != null);
            Assert.assertEquals(2L, pager.total.longValue());
            Assert.assertEquals(Constants.DEFAULT_PAGE_SIZE, pager.size.longValue());
            Assert.assertEquals(2, pager.records.size());
            Assert.assertEquals("user_xxx", pager.records.get(0).userId);
            Assert.assertEquals("role_yyy", pager.records.get(0).roleId);
            Assert.assertEquals("user_xxx", pager.records.get(1).userId);
            Assert.assertEquals("role_xxx", pager.records.get(1).roleId);
        }
        {
            UserRoleSearchRequest searchRequest = new UserRoleSearchRequest();
            searchRequest.roleId = "role_xxx";
            searchRequest.appId = "test_app";
            Pager<UserRole> pager = userRoleService.search(searchRequest);
            Assert.assertTrue(pager != null);
            Assert.assertEquals(2L, pager.total.longValue());
            Assert.assertEquals(Constants.DEFAULT_PAGE_SIZE, pager.size.longValue());
            Assert.assertEquals(2, pager.records.size());
            Assert.assertEquals("user_yyy", pager.records.get(0).userId);
            Assert.assertEquals("role_xxx", pager.records.get(0).roleId);
            Assert.assertEquals("user_xxx", pager.records.get(1).userId);
            Assert.assertEquals("role_xxx", pager.records.get(1).roleId);
        }
        {
            for (Long id : ids) Assert.assertTrue(userRoleService.delete(id));
        }
    }
}
