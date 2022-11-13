package llb.tdd.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: UriTemplateTest
 * @date 2022-11-12 上午7:52
 * @ProjectName tdd
 * @Version V1.0
 */
public class PathTemplateTest {
    @ParameterizedTest
    @CsvSource({"/users,/orders", "/users/{id:[0-9]+},/users/id", "/users,/unit/users"})
    public void should_return_empty_if_path_not_matched(String pattern, String path) {
        PathTemplate template = new PathTemplate(pattern);
        assertTrue(template.match(path).isEmpty());
    }

    @Test
    public void should_return_match_result_if_path_matched() {
        PathTemplate template = new PathTemplate("/users");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users", result.getMatched());
        assertEquals("/1", result.getRemaining());
        assertTrue(result.getMatchedPathParameters().isEmpty());
    }

    @Test
    public void should_return_match_result_if_path_with_variable_matched() {
        PathTemplate template = new PathTemplate("/users/{id}");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users/1", result.getMatched());
        assertNull(result.getRemaining());
        assertFalse(result.getMatchedPathParameters().isEmpty());
        assertEquals("1", result.getMatchedPathParameters().get("id"));
    }

    @Test
    public void should_return_extract_result_if_matched_given_pattern() {
        PathTemplate template = new PathTemplate("/users/{id:[0-9]+}");
        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("1", result.getMatchedPathParameters().get("id"));
    }

    @ParameterizedTest
    @CsvSource({"/users/1234,/users/1234,/users/{id}", "/users/1234567890/order,/{resources}/1234567890/{action},/users/{id}/order", "/users/1,/users/{id:[0-9]+},/users/{1}"})
    public void first_pattern_should_smaller_than_second(String path, String smallerTemplate, String largerTemplate) {
        UriTemplate smaller = new PathTemplate(smallerTemplate);
        UriTemplate larger = new PathTemplate(largerTemplate);

        UriTemplate.MatchResult lhs = smaller.match(path).get();
        UriTemplate.MatchResult rhs = larger.match(path).get();

        assertTrue(lhs.compareTo(rhs) < 0);
        assertTrue(rhs.compareTo(lhs) > 0);
    }

    @Test
    public void should_throw_illegal_argument_exception_if_variable_redefined() {
        assertThrows(IllegalArgumentException.class, () -> new PathTemplate("/users/{id:[0-9]+}/{id}"));
    }

    @Test
    public void should_compare_equal_match_result() {
        UriTemplate template = new PathTemplate("/users/{1}");
        UriTemplate.MatchResult result = template.match("/users/1").get();
        assertEquals(0, result.compareTo(result));
    }

}
