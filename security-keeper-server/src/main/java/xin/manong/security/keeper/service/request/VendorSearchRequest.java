package xin.manong.security.keeper.service.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 供应商搜索请求
 *
 * @author frankcl
 * @date 2023-03-21 16:41:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorSearchRequest extends SearchRequest {

    /**
     * 供应商名
     */
    @JsonProperty("name")
    public String name;
}
