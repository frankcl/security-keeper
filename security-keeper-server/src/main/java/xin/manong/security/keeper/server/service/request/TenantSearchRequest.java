package xin.manong.security.keeper.server.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import xin.manong.security.keeper.model.view.request.SearchRequest;

/**
 * 租户搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantSearchRequest extends SearchRequest {

    /**
     * 租户名
     */
    @JsonProperty("name")
    public String name;
    /**
     * 供应商ID
     */
    @JsonProperty("vendor_id")
    public String vendorId;
}
