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

        Optional<UriTemplate.MatchResult> result = template.match("/orders");

        assertTrue(result.isEmpty());
    }

    @Test
    public void should_return_match_result_if_path_matched() {
        UriTemplateString template = new UriTemplateString("/users");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users", result.getMatched());
        assertEquals("/1", result.getRemaining());
    }

    // TODO path match with variables
    @Test
    public void should_return_match_result_if_path_with_variable_matched(){
        UriTemplateString template = new UriTemplateString("/users/{id}");

        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("/users/1",result.getMatched()) ;
        assertNull(result.getRemaining()) ;
//        assertFalse(result.getPathParameters().isEmpty());
//        assertEquals("1",result.getPathParameters().get("id") );
    }

    // TODO path match with variables with specific pattern
    // TODO throw exception if variable redefined
    // TODO comparing result, with match literal, variables, and specific variables
}
