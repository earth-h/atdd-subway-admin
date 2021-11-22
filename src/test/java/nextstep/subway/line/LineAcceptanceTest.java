package nextstep.subway.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.section.domain.SectionType;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private static final String API_URL = "/lines";
    private static final String STATION_API_URL = "/stations";

    @Test
    @DisplayName("상행, 하행 정보를 포함한 라인 생성")
    void create() {
        //given
        //노선 생성 Request
        //상행역, 하행역 존재
        Map<SectionType, Long> idsMap = given_상행역하행역존재한다(new Station("서울역"), new Station("용산역"));

        // 이름, 색상, 상행역id, 하행역id, 거리
        LineRequest lineRequest =
            new LineRequest("1호선", "Blue", idsMap.get(SectionType.UP), idsMap.get(SectionType.DOWN), 10);

        //when
        //지하철 노선 생성 (상행, 하행 역 정보 포함) 요청
        ExtractableResponse<Response> response = 저장한다(lineRequest, API_URL);

        //then
        //지하철 노선 생성됨
        assertAll(() -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(response.body().jsonPath().getList("stations", StationResponse.class).size())
                .isEqualTo(2);
        });
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        given_서울역용산역구간_1호선저장되어있다();
        // 역 등록되어 있음
        Map<SectionType, Long> idsMap = given_상행역하행역존재한다(new Station("강남역"), new Station("역삼역"));

        // when
        // 지하철_노선_생성_요청
        ExtractableResponse<Response> response =
            저장한다(new LineRequest("1호선", "Orange", idsMap.get(SectionType.UP), idsMap.get(SectionType.DOWN), 10), API_URL);

        // then
        // 지하철_노선_생성_실패됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getList() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> line1 = given_서울역용산역구간_1호선저장되어있다();

        Map<SectionType, Long> idsMap = given_상행역하행역존재한다(new Station("강남역"), new Station("역삼역"));
        ExtractableResponse<Response> line2 = 저장한다(new LineRequest("2호선", "green", idsMap.get(SectionType.UP), idsMap.get(SectionType.DOWN), 10), API_URL);

        // when
        // 지하철_노선_목록_조회_요청
        ExtractableResponse<Response> response = 조회한다(API_URL);

        // then
        // 지하철_노선_목록_응답됨
        // 지하철_노선_목록_포함됨
        List<Long> expectedLineIds = getIdsByResponse(Arrays.asList(line1, line2), Long.class);
        List<Long> resultLineIds = response.jsonPath().getList("id", Long.class);

        assertAll(() -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(resultLineIds).containsAll(expectedLineIds);
        });
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getOne() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> givenData = given_서울역용산역구간_1호선저장되어있다();

        // when
        // 지하철_노선_조회_요청
        ExtractableResponse<Response> response = 조회한다(givenData.header("Location"));

        // then
        // 지하철_노선_응답됨
        assertAll(() -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.body().jsonPath().get("name")
                .equals(givenData.body().jsonPath().get("name"))).isTrue();
        });
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void update() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> givenData = given_서울역용산역구간_1호선저장되어있다();

        // when
        // 지하철_노선_수정_요청
        ExtractableResponse<Response> response =
            수정한다(new LineRequest("3호선", "Orange"), givenData.header("Location"));

        // then
        // 지하철_노선_수정됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void delete() {
        // given
        // 지하철_노선_등록되어_있음
        ExtractableResponse<Response> givenData = given_서울역용산역구간_1호선저장되어있다();

        // when
        // 지하철_노선_제거_요청
        ExtractableResponse<Response> response = 삭제한다(givenData.header("Location"));

        // then
        // 지하철_노선_삭제됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private Map<SectionType, Long> given_상행역하행역존재한다(Station upStation, Station downStation) {
        ExtractableResponse<Response> upResponse = 저장한다(upStation, STATION_API_URL);
        ExtractableResponse<Response> downResponse = 저장한다(downStation, STATION_API_URL);

        Map<SectionType, Long> idsMap = new HashMap<>();
        idsMap.put(SectionType.UP, getLongIdByResponse(upResponse));
        idsMap.put(SectionType.DOWN, getLongIdByResponse(downResponse));

        return idsMap;
    }

    private ExtractableResponse<Response> given_서울역용산역구간_1호선저장되어있다() {
        Map<SectionType, Long> ids = given_상행역하행역존재한다(new Station("서울역"), new Station("용산역"));

        // 이름, 색상, 상행역id, 하행역id, 거리
        LineRequest lineRequest =
            new LineRequest("1호선", "Blue", ids.get(SectionType.UP), ids.get(SectionType.DOWN), 10);

        return 저장한다(lineRequest, API_URL);
    }

}
