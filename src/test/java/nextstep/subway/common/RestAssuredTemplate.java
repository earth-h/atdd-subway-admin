package nextstep.subway.common;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyExtractionOptions;
import nextstep.subway.dto.RequestDTO;
import org.springframework.http.MediaType;

public final class RestAssuredTemplate {

    private RestAssuredTemplate() {
    }

    public static ExtractableResponse<Response> get(String path) {
        return RestAssured.given().log().all()
                .when().get(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> post(String path, RequestDTO request) {
        return RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> delete(String path) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> put(String path, RequestDTO request) {
        return RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put(path)
                .then().log().all()
                .extract();
    }
}