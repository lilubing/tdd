package llb.tdd.di;

import org.junit.jupiter.api.Test;

import java.util.Optional;

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
        UriTemplateString template = new UriTemplateString("/users");

        assertTrue(template.match("/orders").isEmpty());
    }

    @Test
    public void should_return_match_result_if_path_matched() {
        UriTemplateString template = new UriTemplateString("/users");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users", result.getMatched());
        assertEquals("/1", result.getRemaining());
        assertTrue(result.getMatchedPathParameters().isEmpty());
    }

    // TODO path match with variables
    @Test
    public void should_return_match_result_if_path_with_variable_matched(){
        UriTemplateString template = new UriTemplateString("/users/{id}");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users/1",result.getMatched()) ;
        assertNull(result.getRemaining()) ;
        assertFalse(result.getMatchedPathParameters().isEmpty());
        assertEquals("1",result.getMatchedPathParameters().get("id") );
    }

    @Test
    public void should_return_empty_if_not_matched_given_pattern(){
        UriTemplateString template = new UriTemplateString("/users/{id:[0-9]+}");

        assertTrue(template.match("/users/id").isEmpty());
    }

    @Test
    public void should_return_extract_result_if_matched_given_pattern(){
        UriTemplateString template = new UriTemplateString("/users/{id:[0-9]+}");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        /*assertEquals("/users/1",result.getMatched()) ;
        assertNull(result.getRemaining()) ;
        assertFalse(result.getPathParameters().isEmpty());*/
        assertEquals("1",result.getMatchedPathParameters().get("id") );
    }
    @Test
    public void should_throw_illegal_argument_exception_if_variable_redefined(){
        assertThrows(IllegalArgumentException.class ,()-> {
            new UriTemplateString("/users/{id:[0-9]+}/{id}");
        });
    }
    // TODO comparing result, with match literal, variables, and specific variables
}
