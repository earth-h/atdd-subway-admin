package nextstep.subway.section;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nextstep.subway.BaseAcceptanceTest;
import nextstep.subway.dto.SectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@DisplayName("구간 관련 기능")
@Sql({"classpath:section_acceptance_test.sql"})
public class SectionAcceptanceTest extends BaseAcceptanceTest {
    private final long GANGNAM = 1L;
    private final long YANGJAE = 2L;
    private final long JUNGJA = 4L;

    private final long SINBUNDANGSUN = 1L;

    private final int DISTANCE_FROM_GANGNAME_TO_JUNGJA = 30;


    /**
     * When 하행종점역이 아닌 기존 역을 상행선으로 새로운 구간을 등록하면
     * Then 새로운 구간이 등록된다
     * Then 구간 목록 조회 시 생성한 구간을 찾을 수 있다
     * Then 기존 구간의 길이가 새로 생성된 구간의 길이만큼 줄어든다
     */
    @Test
    void registerSectionWithExistingUpStation() {
        // when
        final Long distance = 10L;
        final ExtractableResponse<Response> registerResponse = 구간을_등록한다(GANGNAM, YANGJAE, distance);

        // then
        assertThat(registerResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        final List<SectionResponse> sections = 구간_목록을_조회한다().jsonPath().getList(".", SectionResponse.class);
        assertThat(sections).contains(registerResponse.body().as(SectionResponse.class));

        // then
        final SectionResponse modifiedSection = sections.stream()
                .filter(sectionResponse -> sectionResponse.getDownStationName().equals("정자역"))
                .findFirst()
                .get();
        assertThat(modifiedSection.getDistance()).isEqualTo(DISTANCE_FROM_GANGNAME_TO_JUNGJA - 10);
    }

    private ExtractableResponse<Response> 구간을_등록한다(final long upStationId,
                                                   final long downStationId,
                                                   final long distance) {
        final Map<String, Object> params = new HashMap<>();
        params.put("upStationId", upStationId);
        params.put("downStationId", downStationId);
        params.put("distance", distance);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(params)
                .when().post("/lines/{id}/sections", SINBUNDANGSUN)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 구간_목록을_조회한다() {
        return RestAssured
                .given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/lines/{id}/sections", SINBUNDANGSUN)
                .then().log().all()
                .extract();
    }

    private void 구간이_등록되었다(final ExtractableResponse<Response> registerResponse) {
        assertThat(registerResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        final List<SectionResponse> sections = 구간_목록을_조회한다().jsonPath().getList(".", SectionResponse.class);
        assertThat(sections).contains(registerResponse.body().as(SectionResponse.class));
    }
}
