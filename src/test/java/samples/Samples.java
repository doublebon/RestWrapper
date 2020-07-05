package samples;

import io.restassured.response.ValidatableResponse;
import mapping.RespHeaders;
import mapping.RespIpInfo;
import org.testng.annotations.Test;
import utils.RestWrapper;

public class Samples {

    @Test
    private void sampleIpRequest(){
        //Send request and get response.
        ValidatableResponse response = RestWrapper.builder()
                .baseApiUrl("ip.jsontest.com")
                .urlPath("")
                .query("kek","123")
                .secureConnect(false)
                .build()
                .get();
        //Mapping response json into RespIpInfo class
        RespIpInfo ipInfo = RestWrapper.parse(response, RespIpInfo.class);
        System.out.println(ipInfo.getIp());
    }

    /*
    * Send request to endpoint url
    * */
    @Test
    private void sampleUrlPath(){
        RestWrapper.builder()
                .baseApiUrl("echo.jsontest.com")
                .urlPath("MyEndpoint")
                .secureConnect(false)
                .build()
                .get()
                .statusCode(200);
    }

    /*
    * You can set multiple headers, query and path params. Exactly as headers at test below.
    * */
    @Test
    private void sampleHeadersRequest(){
        ValidatableResponse response = RestWrapper.builder()
                .baseApiUrl("headers.jsontest.com")
                .headers("MyHeader1", "Sample host")
                .headers("MyHeader2","12345")
                .secureConnect(false)
                .build()
                .get();
        RespHeaders headers = RestWrapper.parse(response, RespHeaders.class);
        System.out.println(String.format("My first header: %s \nMy second header: %s", headers.getMyHeader1(), headers.getMyHeader2()));
    }

    @Test
    private void samplePathParams(){
        ValidatableResponse response = RestWrapper.builder()
                .baseApiUrl("echo.jsontest.com")
                .urlPath("/{first_end}/{second_end}")
                .pathParams("first_end","myFirst")
                .pathParams("second_end", "mySecond")
                .secureConnect(false)
                .build()
                .get();
    }

}
