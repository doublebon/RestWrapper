package support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.log4j.Log4j;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static io.restassured.RestAssured.given;

@Builder
public class RestWrapper {

    @NonNull private String baseUrl;
    @Builder.Default private String urlPath = "";
    @Builder.Default private String urlPort = "";
    @Singular("headers") private Map<String, String> headers;
    @Singular("query") private Map<String, String> query;
    @Singular("pathParams") private Map<String, String> pathParams;
    @Singular("multiPart") private Map<String, ?> multiPart;
    @Builder.Default private boolean isSecureConnect = true;
    @Builder.Default private boolean disableBodyLogs = false;
    @Builder.Default private EnumContentType contentType = EnumContentType.JSON;
    @Singular("proxy") private Map<String, Integer> proxy;

    private RequestSpecification requestGenerator() {
        String protocol = parseProtocol(baseUrl, isSecureConnect);
        String port = !urlPort.isEmpty() ? ":"+urlPort : urlPort;

        this.baseUrl =
                protocol +
                baseUrl.replaceAll("https://","").replaceAll("http://","") +
                port;

        RequestSpecification base = given().spec(getSpecsByBaseEndPoint(this.baseUrl));
        base.contentType(contentType.getStringContentType());

        if (!proxy.isEmpty()) {
            for (Map.Entry<String, Integer> entry : proxy.entrySet()) {
                if(entry.getKey() != null && !entry.getKey().isEmpty())
                    base.proxy(entry.getKey(),entry.getValue());
            }
        }

        if (!multiPart.isEmpty()) {
            for (Map.Entry<String, ?> entry : multiPart.entrySet()) {
                if(entry.getValue() != null)
                    base.multiPart(entry.getKey(), entry.getValue());
            }
            base.log().params();
        }

        if (!headers.isEmpty()){
            base.headers(removeNullMapValues(headers)); }

        if (!query.isEmpty()){
            base.queryParams(removeNullMapValues(query)); }

        if (!pathParams.isEmpty()){
            base.pathParams(removeNullMapValues(pathParams)); }

        return base;
    }


    public ValidatableResponse get() {
        return requestGenerator()
                .when()
                .get(this.urlPath)
                .then()
                .spec(disableBodyLogs? responseNoBodySpecs : responseSpecs);
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
                .spec(disableBodyLogs? responseNoBodySpecs : responseSpecs);
    }

    public <T> ValidatableResponse delete(T data) {
        return requestGenerator()
                .body(data).
                        when()
                .delete(this.urlPath).
                        then()
                .spec(disableBodyLogs? responseNoBodySpecs : responseSpecs);
    }

    public <T> ValidatableResponse put(T data) {
        return requestGenerator()
                .body(data).
                        when()
                .put(this.urlPath).
                        then()
                .spec(disableBodyLogs? responseNoBodySpecs : responseSpecs);
    }

    public <T> ValidatableResponse patch(T data) {
        return requestGenerator()
                .body(data).
                        when()
                .patch(this.urlPath).
                        then()
                .spec(disableBodyLogs? responseNoBodySpecs : responseSpecs);
    }

    //Этот спек один на всех, его можно не генерить
    private static ResponseSpecification responseSpecs = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();

    //Этот спек для того, чтобы не логировать тело ответа
    private static ResponseSpecification responseNoBodySpecs = new ResponseSpecBuilder()
            .log(LogDetail.STATUS)
            .build();

    /*
     * Вместо отдельных переменных сделал что-то типо генератора спеков
     * дабы меньше было хлама, вставлять надо в функции типа "send"
     * .spec(getSpecsByBaseEndPoint(EndPoints.baseSessionPath)) <- В параметр бросаешь БАЗОВЫЙ адрес
     * перед этим новый адрес добавляешь в EndPoint
     * */
    private static RequestSpecification getSpecsByBaseEndPoint(String baseEndPoint) {
        return new RequestSpecBuilder()
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .log(LogDetail.HEADERS)
                .log(LogDetail.BODY)
                .setBaseUri(baseEndPoint)
                .setRelaxedHTTPSValidation()
                .build();
    }

    //Для быстрого создания однострочного json, чтобы не плодить 1000 классов - оберток
    public static String oneFieldJson(String field, String value) {
        BiFunction<String, String, String> fastJson = (f, v) -> String.format("{\"%s\":\"%s\"}", field, value);
        return fastJson.apply(field, value);
    }

    /* (работает только с RestAssured)
     * Парсит json ответа в класс типа type
     *
     * Пример:
     * RespAuthUser re = RestWrapper.parse(*ответ от рест ашура*, *В какой класс распарсить* прим. RespAuthUser.class);
     * */
    public static <T> T parse(ValidatableResponse response, Type type) {
        return response.statusCode(200).extract().body().as(type);
    }

    public static <T> T parseOneField(ValidatableResponse response, String nodeName) {
        return response.statusCode(200).extract().jsonPath().get(nodeName);
    }

    /* (работает только с RestAssured)
     * Используется для json ответа вида:
     * [
     *  { field:value }, { field2:value2 }...
     * ]
     *
     * Пример:
     * List<ReportInfo> re = RestWrapper.parseMass(*ответ от рест ашура*, ReportInfo[].class);
     * */
    public static <T> List<T> parseMass(ValidatableResponse response, Type type) {
        return Arrays.asList(response.statusCode(200).extract().body().as(type));
    }


    private Map<String, String> removeNullMapValues(Map<String, String> map){
        Map<String, String> temp = new HashMap<>(map);
        temp.values().remove(null);
        return temp;
    }

    public enum EnumContentType{
        JSON("application/json"),
        MULTIPART("multipart/form-data");

        private final String stringContentType;

        EnumContentType(final String contentType){
            this.stringContentType = contentType;
        }

        public String getStringContentType(){
            return stringContentType;
        }
    }

    private String parseProtocol(String baseUrl, boolean isSecureConnect){
        String protocolFromUrl = baseUrl.split("://",2)[0].toLowerCase();
        if(protocolFromUrl.contains("http"))
            return protocolFromUrl+"://";
        else
            return isSecureConnect? "https://" : "http://";
    }
}
