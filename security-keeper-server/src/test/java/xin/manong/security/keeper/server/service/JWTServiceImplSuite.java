package xin.manong.security.keeper.server.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.manong.security.keeper.server.ApplicationTest;
import xin.manong.security.keeper.server.common.Constants;
import xin.manong.security.keeper.model.Profile;
import xin.manong.weapon.base.util.RandomID;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author frankcl
 * @date 2023-08-31 20:00:24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationTest.class })
public class JWTServiceImplSuite {

    @Resource
    protected JWTService jwtService;

    @Test
    public void testJWTOperations() {
        Profile profile = new Profile();
        profile.id = RandomID.build();
        profile.userId = "aaa";
        profile.tenantId = "bbb";
        profile.vendorId = "ccc";
        Date expiresAt = new Date(System.currentTimeMillis() + 86400000L);
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(Constants.JWT_HEADER_CATEGORY, Constants.JWT_CATEGORY_TOKEN);
        String token = jwtService.buildJWT(profile, expiresAt, Constants.ALGORITHM_HS256, headerMap);
        Assert.assertFalse(StringUtils.isEmpty(token));
        DecodedJWT decodedJWT = jwtService.decodeJWT(token);
        Assert.assertTrue(decodedJWT != null);
        Claim claim = decodedJWT.getHeaderClaim(Constants.JWT_HEADER_CATEGORY);
        Assert.assertTrue(claim != null);
        Assert.assertEquals(Constants.JWT_CATEGORY_TOKEN, claim.asString());
        Assert.assertTrue(jwtService.verify(decodedJWT));
        Profile decodedProfile = jwtService.decodeProfile(token);
        Assert.assertTrue(decodedProfile != null);
        Assert.assertEquals(profile.id, decodedProfile.id);
        Assert.assertEquals(profile.userId, decodedProfile.userId);
        Assert.assertEquals(profile.tenantId, decodedProfile.tenantId);
        Assert.assertEquals(profile.vendorId, decodedProfile.vendorId);
    }
}
