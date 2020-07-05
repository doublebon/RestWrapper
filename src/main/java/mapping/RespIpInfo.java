package mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RespIpInfo {

    @JsonProperty("ip")
    private String ip;
}
