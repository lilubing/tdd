package llb.tdd.di;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ResourceDispatcherTest
 * @date 2022-11-10 7:57:15
 * @ProjectName tdd
 * @Version V1.0
 */
public class ResourceDispatcherTest {
	RuntimeDelegate delegate;
	Runtime runtime;

	HttpServletRequest request;
	ResourceContext context;
	UriInfoBuilder builder;

	@BeforeEach
	public void setup() {
		runtime = mock(Runtime.class);

		delegate = mock(RuntimeDelegate.class);
		RuntimeDelegate.setInstance(delegate);

		when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());

		request = mock(HttpServletRequest.class);
		context = mock(ResourceContext.class);
		when(request.getServletPath()).thenReturn("/users/1");
		when(request.getMethod()).thenReturn("GET");
		when(request.getHeaders(eq(HttpHeaders.ACCEPT))).thenReturn(new Vector<>(List.of(MediaType.WILDCARD)).elements());

		builder = mock(UriInfoBuilder.class);
		when(runtime.createUriInfoBuilder(same(request))).thenReturn(builder);
	}

	//TODO 根据与Path匹配结果，降序排列RootResource, 选择第-个的RootResource
	//TODO R1, R2, R1 matched, R2 none R1
	//TODO R1, R2, R1, R2, matched, R1 result < R2 result R1

	@Test
	public void should_use_matched_root_resource() {
		GenericEntity entity = new GenericEntity("matched", String.class);

		ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
				rootResource(matched("/users/1", matched("/1")), returns(entity)),
				rootResource(unmatched("/users/1"))));

		OutboundResponse response = router.dispatch(request, context);

		assertSame(entity, response.getGenericEntity());
		assertEquals(200, response.getStatus());
	}

	@Test
	public void should_sort_matched_root_resource_descending_order() {
		GenericEntity entity1 = new GenericEntity("1", String.class);
		GenericEntity entity2 = new GenericEntity("2", String.class);
		ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
				rootResource(matched("/users/1", matched("/1", 2)), returns(entity2)),
				rootResource(matched("/users/1", matched("/1", 1)), returns(entity1))));

		OutboundResponse response = router.dispatch(request, context);

		assertSame(entity1, response.getGenericEntity());
		assertEquals(200, response.getStatus());
	}

	@Test
	public void should_return_404_if_no_root_resource_matched() {
		ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
				rootResource(unmatched("/users/1"))));

		OutboundResponse response = router.dispatch(request, context);

		assertNull(response.getGenericEntity());
		assertEquals(404, response.getStatus());
	}
	@Test
	public void should_return_404_if_no_resource_method_found() {
		ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
				rootResource(matched("/users/1", matched("/1", 2)))));

		OutboundResponse response = router.dispatch(request, context);

		assertNull(response.getGenericEntity());
		assertEquals(404, response.getStatus());
	}
	//TODO 如果ResourceMethod返回null, 则构造204的Response
	@Test
	public void should_return_204_if_method_return_null() {
		ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
				rootResource(matched("/users/1", matched("/1", 2)), returns(null))));

		OutboundResponse response = router.dispatch(request, context);

		assertNull(response.getGenericEntity());
		assertEquals(204, response.getStatus());
	}

	private ResourceRouter.RootResource rootResource(StubUriTemplate stub) {
		ResourceRouter.RootResource unmatched = Mockito.mock(ResourceRouter.RootResource.class);
		when(unmatched.getUriTemplate()).thenReturn(stub.uriTemplate);
		when(unmatched.match(same(stub.result), eq("GET"), eq(new String[]{MediaType.WILDCARD}), same(context), eq(builder))).thenReturn(Optional.empty());
		return unmatched;
	}

	private static StubUriTemplate unmatched(String path) {
		UriTemplate unmatchedUriTemplate = Mockito.mock(UriTemplate.class);
		when(unmatchedUriTemplate.match(eq(path))).thenReturn(Optional.empty());
		return new StubUriTemplate(unmatchedUriTemplate, null);
	}

	private ResourceRouter.RootResource rootResource(StubUriTemplate stub, ResourceRouter.ResourceMethod method) {
		ResourceRouter.RootResource matched = mock(ResourceRouter.RootResource.class);
		when(matched.getUriTemplate()).thenReturn(stub.uriTemplate);
		when(matched.match(same(stub.result), eq("GET"), eq(new String[]{MediaType.WILDCARD}), same(context), eq(builder))).thenReturn(Optional.of(method));
		return matched;
	}

	private ResourceRouter.ResourceMethod returns(GenericEntity entity) {
		ResourceRouter.ResourceMethod method = Mockito.mock(ResourceRouter.ResourceMethod.class);
		when(method.call(same(context), same(builder))).thenReturn(entity);
		return method;
	}

	private StubUriTemplate matched(String path, UriTemplate.MatchResult result) {
		UriTemplate matchedUriTemplate = Mockito.mock(UriTemplate.class);
		when(matchedUriTemplate.match(eq(path))).thenReturn(Optional.of(result));
		return new StubUriTemplate(matchedUriTemplate, result);
	}

	record StubUriTemplate(UriTemplate uriTemplate, UriTemplate.MatchResult result) {}

	private UriTemplate.MatchResult matched(String path) {
		return new FakeMatchResult(path, 0);
	}

	private UriTemplate.MatchResult matched(String path, int order) {
		return new FakeMatchResult(path, order);
	}

	class FakeMatchResult implements UriTemplate.MatchResult {

		private String remaining;
		private Integer order;

		public FakeMatchResult(String remaining, Integer order) {
			this.remaining = remaining;
			this.order = order;
		}

		@Override
		public String getMatched() {
			return null;
		}

		@Override
		public String getRemaining() {
			return remaining;
		}

		@Override
		public Map<String, String> getMatchedPathParameters() {
			return null;
		}

		@Override
		public int compareTo(UriTemplate.MatchResult o) {
			return order.compareTo(((FakeMatchResult) o).order);
		}
	}

	private class StubResponseBuilder extends Response.ResponseBuilder {
		private int status;
		private Object entity;

		@Override
		public Response build() {
			OutboundResponse response = mock(OutboundResponse.class);
			when(response.getStatus()).thenReturn(status);
			when(response.getEntity()).thenReturn(entity);
			when(response.getGenericEntity()).thenReturn((GenericEntity) entity);
			return response;
		}

		@Override
		public Response.ResponseBuilder clone() {
			return null;
		}

		@Override
		public Response.ResponseBuilder status(int i) {
			return null;
		}

		@Override
		public Response.ResponseBuilder status(int i, String s) {
			this.status = i;
			return this;
		}

		@Override
		public Response.ResponseBuilder entity(Object o) {
			this.entity = o;
			return this;
		}

		@Override
		public Response.ResponseBuilder entity(Object o, Annotation[] annotations) {
			return null;
		}

		@Override
		public Response.ResponseBuilder allow(String... strings) {
			return null;
		}

		@Override
		public Response.ResponseBuilder allow(Set<String> set) {
			return null;
		}

		@Override
		public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
			return null;
		}

		@Override
		public Response.ResponseBuilder encoding(String s) {
			return null;
		}

		@Override
		public Response.ResponseBuilder header(String s, Object o) {
			return null;
		}

		@Override
		public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> multivaluedMap) {
			return null;
		}

		@Override
		public Response.ResponseBuilder language(String s) {
			return null;
		}

		@Override
		public Response.ResponseBuilder language(Locale locale) {
			return null;
		}

		@Override
		public Response.ResponseBuilder type(MediaType mediaType) {
			return null;
		}

		@Override
		public Response.ResponseBuilder type(String s) {
			return null;
		}

		@Override
		public Response.ResponseBuilder variant(Variant variant) {
			return null;
		}

		@Override
		public Response.ResponseBuilder contentLocation(URI uri) {
			return null;
		}

		@Override
		public Response.ResponseBuilder cookie(NewCookie... newCookies) {
			return null;
		}

		@Override
		public Response.ResponseBuilder expires(Date date) {
			return null;
		}

		@Override
		public Response.ResponseBuilder lastModified(Date date) {
			return null;
		}

		@Override
		public Response.ResponseBuilder location(URI uri) {
			return null;
		}

		@Override
		public Response.ResponseBuilder tag(EntityTag entityTag) {
			return null;
		}

		@Override
		public Response.ResponseBuilder tag(String s) {
			return null;
		}

		@Override
		public Response.ResponseBuilder variants(Variant... variants) {
			return null;
		}

		@Override
		public Response.ResponseBuilder variants(List<Variant> list) {
			return null;
		}

		@Override
		public Response.ResponseBuilder links(Link... links) {
			return null;
		}

		@Override
		public Response.ResponseBuilder link(URI uri, String s) {
			return null;
		}

		@Override
		public Response.ResponseBuilder link(String s, String s1) {
			return null;
		}
	}
}
