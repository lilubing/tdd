package llb.tdd.di;

import org.junit.jupiter.api.Test;

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
public class UriTemplateTest {
    @Test
    public void should_return_empty_if_path_not_matched() {
        PathTemplate template = new PathTemplate("/users");

        assertTrue(template.match("/orders").isEmpty());
    }

    @Test
    public void should_return_match_result_if_path_matched() {
        PathTemplate template = new PathTemplate("/users");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users", result.getMatched());
        assertEquals("/1", result.getRemaining());
        assertTrue(result.getMatchedPathParameters().isEmpty());
    }

    // TODO path match with variables
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
    public void should_return_empty_if_not_matched_given_pattern() {
        PathTemplate template = new PathTemplate("/users/{id:[0-9]+}");

        assertTrue(template.match("/users/id").isEmpty());
    }

    @Test
    public void should_return_extract_result_if_matched_given_pattern() {
        PathTemplate template = new PathTemplate("/users/{id:[0-9]+}");
        UriTemplate.MatchResult result = template.match("/users/1").get();

        /*assertEquals("/users/1",result.getMatched()) ;
        assertNull(result.getRemaining()) ;
        assertFalse(result.getPathParameters().isEmpty());*/
        assertEquals("1", result.getMatchedPathParameters().get("id"));
    }

    @Test
    public void should_throw_illegal_argument_exception_if_variable_redefined() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PathTemplate("/users/{id:[0-9]+}/{id}");
        });
    }
    // TODO comparing result, with match literal, variables, and specific variables

    @Test
    public void should_compare_for_match_literal() {
        assertSmaller("/users/1234", "/users/1234", "/users/{id}");
    }

    @Test
    public void should_compare_match_variables_if_matched_literal_same() {
        assertSmaller("/users/1234567890/order", "/{resources}/1234567890/{action}", "/users/{id}/order");
    }

    @Test
    public void should_compare_specific_variable_if_matched_literal_variables_same() {
        assertSmaller("/users/1", "/users/{id:[0-9]+}", "/users/{1}");
    }

    @Test
    public void should_compare_equal_match_result() {
        UriTemplate smaller = new PathTemplate("/users/{1}");
        UriTemplate.MatchResult result = smaller.match("/users/1").get();
        assertEquals(0, result.compareTo(result));
    }

    private static void assertSmaller(String path, String smallerTemplate, String largerTemplate) {
        UriTemplate smaller = new PathTemplate(smallerTemplate);
        UriTemplate larger = new PathTemplate(largerTemplate);

        UriTemplate.MatchResult lhs = smaller.match(path).get();
        UriTemplate.MatchResult rhs = larger.match(path).get();

        assertTrue(lhs.compareTo(rhs) < 0);
        assertTrue(rhs.compareTo(lhs) > 0);
    }
}
