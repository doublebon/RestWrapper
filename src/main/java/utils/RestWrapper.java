package utils;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static io.restassured.RestAssured.given;

/*
* baseApiUrl - basic url. Example: ip.jsontest.com
* urlPath    - endpoint url. If full url is http://date.jsontest.com/service set .urlPath("/service")
* query      - if full url is http://md5.jsontest.com/?text=example_text set .query("text","example_text")
* pathParams - If full url is http://date.jsontest.com/{service} set .pathParams("service","your_service_replace_value")
* secureConnect - Use for switch between http and https. By default: "https"
*
* Use sample:
* RestWrapper.builder()
*     .baseApiUrl(baseUrl)
*     .urlPath(endpoint)
*     .headers("authorization", "TOKEN")
*     .build()
*     .*requestType* //.get() .post() .put() .delete()
* */
@Builder
public class RestWrapper {
    @NonNull private String baseApiUrl;
    @Builder.Default private String urlPath = "";
    @Singular("headers") private Map<String, String> headers;
    @Singular("query") private Map<String, String> query;
    @Singular("pathParams") private Map<String, String> pathParams;
    @Builder.Default private boolean secureConnect = true;

    private RequestSpecification requestGenerator() {
        this.baseApiUrl = secureConnect ? "https://"+baseApiUrl : "http://"+baseApiUrl;
        RequestSpecification base = given().spec(getSpecsByBaseEndPoint(this.baseApiUrl));
        if (headers != null) base.headers(headers);
        if (query != null) base.queryParams(query);
        if (pathParams != null) base.pathParams(pathParams);
        return base;
    }


    public ValidatableResponse get() {
        return requestGenerator()
                .when()
                .get(this.urlPath).
                        then()
                .spec(responseSpecs);
    }

    /*
     * Use oneFieldJson for data, if u need body like {"field" : "value"}
     * */
    public <T> ValidatableResponse post(T data) {
        return requestGenerator()
                .body(data).
                        when()
                .post(this.urlPath).
                        then()
                .spec(responseSpecs);
    }

    public <T> ValidatableResponse delete(T data) {
        return requestGenerator()
                .body(data).
                        when()
                .delete(this.urlPath).
                        then()
                .spec(responseSpecs);
    }

    public <T> ValidatableResponse put(T data) {
        return requestGenerator()
                .body(data).
                        when()
                .put(this.urlPath).
                        then()
                .spec(responseSpecs);
    }
    /*
    * One response spec for all
    * */
    private static ResponseSpecification responseSpecs = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();

    /*
    * Specs generator:
    * .spec(getSpecsByBaseEndPoint(basePath)) <- use "base" address.
    * .setConfig() <- i'm using custom logger. If you dont need this, just delete this line
    *
    * */
    private static RequestSpecification getSpecsByBaseEndPoint(String baseEndPoint) {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .log(LogDetail.HEADERS)
                .log(LogDetail.BODY)
                .setBaseUri(baseEndPoint)
                .setRelaxedHTTPSValidation()
                .build();
    }

    /*
    * Fast create one field json
    * result: {"field" : "value"}
    * */
    public static String oneFieldJson(String field, String value) {
        BiFunction<String, String, String> fastJson = (f, v) -> String.format("{\"%s\":\"%s\"}", field, value);
        return fastJson.apply(field, value);
    }

    /*
    * Works with Rest-Assured only
    * Parse response json into Class
    * Sample response:
    * { "value" : "field" }
    *
    * Mapping:
    * *Your_Deserialize_Class* yourClassName = RestWrapper.parse(*rest-assured response*, Your_Deserialize_Class.class);
    * */
    public static <T> T parse(ValidatableResponse response, Type type) {
        return response.statusCode(200).extract().body().as(type);
    }

    /*
     * Works with Rest-Assured only
     * Parse array response json into Class
     * Sample response:
     * [
     *  { field:value }, { field2:value2 }...
     * ]
     *
     * Пример:
     * List<*Your_Deserialize_Class*> re = RestWrapper.parseMass(*ответ от рест ашура*, Your_Deserialize_Class[].class);
     * */
    public static <T> List<T> parseMass(ValidatableResponse response, Type type) {
        return Arrays.asList(response.statusCode(200).extract().body().as(type));
    }
}
