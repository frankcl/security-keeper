package xin.manong.security.keeper.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;

/**
 * 租户信息
 *
 * @author frankcl
 * @since 2023-08-29 17:28:22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("tenant")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tenant extends BaseModel {

    private static final Logger logger = LoggerFactory.getLogger(Tenant.class);

    @TableId(value = "id")
    @JSONField(name = "id")
    @JsonProperty("id")
    public String id;

    @TableField("name")
    @JSONField(name = "name")
    @JsonProperty("name")
    public String name;

    @TableField("vendor_id")
    @JSONField(name = "vendor_id")
    @JsonProperty("vendor_id")
    public String vendorId;

    /**
     * 检测有效性
     * 无效抛出异常
     */
    public void check() {
        if (StringUtils.isEmpty(vendorId)) {
            logger.error("vendor id is empty");
            throw new BadRequestException("供应商ID为空");
        }
        if (StringUtils.isEmpty(id)) {
            logger.error("tenant id is empty");
            throw new BadRequestException("租户ID为空");
        }
        if (StringUtils.isEmpty(name)) {
            logger.error("tenant name is empty");
            throw new BadRequestException("租户名为空");
        }
    }
}
