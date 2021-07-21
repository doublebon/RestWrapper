package functional.samples;

import io.restassured.response.ValidatableResponse;
import mapping.Headers;
import org.testng.annotations.Test;
import support.RestWrapper;

public class SampleTest {

    @Test
    public void sampleIpRequestAndParse(){
        ValidatableResponse response = RestWrapper.builder()
                .baseUrl("ip.jsontest.com")
                .query("sample","123")
                //.proxy("proxy",port) if you need it
                .isSecureConnect(false) //set http protocol
                .build()
                .get();
        String ipInfo = RestWrapper.parseOneField(response, "ip");
        System.out.println(ipInfo);
    }

    /*
     * Send request to endpoint url
     * */
    @Test
    public void checkRequestStatusCode(){
        RestWrapper.builder()
                .baseUrl("echo.jsontest.com")
                .urlPath("MyEndpoint")
                .isSecureConnect(false)
                .build()
                .get()
                .statusCode(200); //check status code (by RestAssured)
    }

    /*
     * You can set multiple headers, query and path params. Exactly as headers at test below.
     * */
    @Test
    public void sampleHeadersRequest(){
        ValidatableResponse response = RestWrapper.builder()
                .baseUrl("headers.jsontest.com")
                .headers("MyHeader1", "Sample host")
                .headers("MyHeader2","12345")
                .isSecureConnect(false)
                .build()
                .get();
        Headers headers = RestWrapper.parse(response, Headers.class);
        System.out.println(String.format("My first header: %s \nMy second header: %s", headers.getMyHeader1(), headers.getMyHeader2()));
    }

    @Test
    public void samplePathParams(){
        RestWrapper.builder()
                .baseUrl("echo.jsontest.com")
                .urlPath("/{first_end}/{second_end}")
                .pathParams("first_end","myFirst")
                .pathParams("second_end", "mySecond")
                .isSecureConnect(false)
                .build()
                .get().statusCode(200);
    }

}
