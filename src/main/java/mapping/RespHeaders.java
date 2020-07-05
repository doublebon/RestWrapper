package mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespHeaders {
    @JsonProperty("X-Cloud-Trace-Context")
    private String xCloudTraceContext;
    @JsonProperty("Accept")
    private String accept;
    @JsonProperty("Upgrade-Insecure-Requests")
    private String upgradeInsecureRequests;
    @JsonProperty("User-Agent")
    private String userAgent;
    @JsonProperty("Host")
    private String host;
    @JsonProperty("DNT")
    private String dNT;
    @JsonProperty("Accept-Language")
    private String acceptLanguage;
    @JsonProperty("MyHeader1")
    private String myHeader1;
    @JsonProperty("MyHeader2")
    private String myHeader2;


}
