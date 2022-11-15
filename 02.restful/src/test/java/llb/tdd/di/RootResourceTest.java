package llb.tdd.di;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    private ResourceContext resourceContext;
    private Messages rootResource;

    @BeforeEach
    public void before() {
        rootResource = new Messages();
        resourceContext = Mockito.mock(ResourceContext.class);
        when(resourceContext.getResource(eq(Messages.class))).thenReturn(rootResource);
    }

    @Test
    public void should_get_uri_template_from_path_annotation() {
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate template = resource.getUriTemplate();

        assertTrue(template.match("/messages/hello").isPresent());
    }

    @ParameterizedTest(name = "{3}")
    @CsvSource(textBlock = """
            GET,    /messages,              Messages.get,       Map to resource methods
            GET,    /messages/1/content,    Message.content,    Map to sub-resource method
            GET,    /messages/1/body,       MessageBody.get,    Map to sub-sub-resource method
            """)
    public void should_match_resource_method_in_root_resource(String httpMethod, String path, String resourceMethod, String context) {
        UriInfoBuilder builder = new StubUriInfoBuilder();
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(path).get();

        ResourceRouter.ResourceMethod method = resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, resourceContext, builder).get();

        assertEquals(resourceMethod, method.toString());
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            GET,    /messages/hello,        No matched resource method
            GET,    /messages/1/header,     No matched sub-resource method
            """)
    public void should_return_empty_if_not_match_in_root_resource(String httpMethod, String uri, String context) {
        UriInfoBuilder builder = new StubUriInfoBuilder();
        builder.addMatchedResource(Messages.class);

        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(uri).get();

        assertTrue(resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, resourceContext, builder).isEmpty());
    }

    @Test
    public void should_add_last_match_resource_to_uri_info_builder() {
        StubUriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();

        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match( "/messages").get();

        resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, resourceContext, uriInfoBuilder);

        assertTrue(uriInfoBuilder.getLastMatchedResource() instanceof  Messages);;
    }

    //TODO if resource class does not have a path annotation, throw illegal argument
    //TODO Head and Options special case

    @Path("/messages")
    static class Messages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "message";
        }

        @Path("/{id:[0-9]+}")
        public Message getById() {
            return new Message();
        }
    }

    static class Message {
        @GET
        @Path("/content")
        @Produces(MediaType.TEXT_PLAIN)
        public String content() {
            return "content";
        }

        @Path("/body")
        public MessageBody body() {
            return new MessageBody();
        }
    }

    static class MessageBody {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "body";
        }
    }
}
