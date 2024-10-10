package xin.manong.hylian.server.util;

import org.junit.Test;
import xin.manong.hylian.model.User;

import javax.ws.rs.BadRequestException;

/**
 * @author frankcl
 * @date 2024-10-03 20:32:55
 */
public class ModelValidatorTest {

    @Test
    public void testValidateFieldSuccess() {
        ModelValidator.validateField(User.class, "name");
        ModelValidator.validateField(User.class, "password");
    }

    @Test(expected = BadRequestException.class)
    public void testValidateFieldFail() {
        ModelValidator.validateField(User.class, "unknown");
    }
}