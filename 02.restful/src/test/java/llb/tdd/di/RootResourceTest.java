package llb.tdd.di;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: RootResourceTest
 * @date 2022-11-13 上午8:12
 * @ProjectName tdd
 * @Version V1.0
 */
public class RootResourceTest {
    @Test
    public void should_get_uri_template_from_path_annotation() {
        ResourceRouter.RootResource resource = new RootResourceClass(Messages.class);
        UriTemplate template = resource.getUriTemplate();

        assertTrue(template.match("/messages/hello").isPresent());
    }

    //TODO find resource method, matches the http request and http method
    @ParameterizedTest
    @CsvSource(textBlock = """
            GET,    /messages/hello,        Messages.hello,         GET and URI match
            GET,    /messages/ah,           Messages.ah,            GET and URI match
            POST,   /messages/hello,        Messages.postHello,     POST and URI match
            PUT,    /messages/hello,        Messages.putHello,      PUT and URI match
            DELETE, /messages/hello,        Messages.deleteHello,   DELETE and URI match
            PATCH,  /messages/hello,        Messages.patchHello,    PATCH and URI match
            HEAD,   /messages/hello,        Messages.headHello,     HEAD and URI match
            OPTIONS,/messages/hello,        Messages.optionsHello,  OPTIONS and URI match
            GET,    /messages/topics/1234,  Messages.topics1234,    GET with multiply choices
            GET,    /messages,              Messages.get,           GET with resource method without Path
            """)
    public void should_match_resource_method(String httpMethod, String path, String resourceMethod) {
        ResourceRouter.RootResource resource = new RootResourceClass(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(path).get();

        ResourceRouter.ResourceMethod method = resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, Mockito.mock(UriInfoBuilder.class)).get();

        assertEquals(resourceMethod, method.toString());
    }

    //TODO if sub resource locator matches uri, using it to tdo follow up matching

    //TODO if no method / sub resource locator matches, return 404
    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            GET,    /missing-messages/hello,        URI not match
            POST,   /missing-messages/,             http match not match
            """)
    public void should_return_empty_if_not_match(String httpMethod, String uri, String context) {
        ResourceRouter.RootResource resource = new RootResourceClass(MissingMessages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(uri).get();

        assertTrue(resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, Mockito.mock(UriInfoBuilder.class)).isEmpty());
    }

    //TODO if resource class does not have a path annotation, throw illegal argument
    //TODO Head and Options special case

    @Path("/missing-messages")
    static class MissingMessages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "message";
        }
    }

    @Path("/messages")
    static class Messages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "message";
        }

        @GET
        @Path("/ah")
        @Produces(MediaType.TEXT_PLAIN)
        public String ah() {
            return "ah";
        }

        @GET
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String hello() {
            return "hello";
        }

        @POST
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String postHello() {
            return "hello";
        }

        @PUT
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String putHello() {
            return "hello";
        }
        @DELETE
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String deleteHello() {
            return "hello";
        }
        @PATCH
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String patchHello() {
            return "hello";
        }
        @HEAD
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String headHello() {
            return "hello";
        }
        @OPTIONS
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String optionsHello() {
            return "hello";
        }

        @GET
        @Path("/topics/{id}")
        @Produces(MediaType.TEXT_PLAIN)
        public String topicsId() {
            return "topicsId";
        }

        @GET
        @Path("/topics/1234")
        @Produces(MediaType.TEXT_PLAIN)
        public String topics1234() {
            return "topics1234";
        }
    }
}
