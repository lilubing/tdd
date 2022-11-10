package llb.tdd.di;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
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
public class ResourceDispatcherBakTest {
	private RuntimeDelegate delegate;
	private Runtime runtime;

	@BeforeEach
	public void before() {
		runtime = Mockito.mock(Runtime.class);
		delegate = Mockito.mock(RuntimeDelegate.class);
		RuntimeDelegate.setInstance(delegate);
		when(delegate.createResponseBuilder()).thenReturn(new Response.ResponseBuilder() {

			private Object entity;
			private int status;

			@Override
			public Response build() {
				OutboundResponse response = Mockito.mock(OutboundResponse.class);
				when(response.getEntity()).thenReturn(entity);
				return response;
			}

			@Override
			public Response.ResponseBuilder clone() {
				return null;
			}

			@Override
			public Response.ResponseBuilder status(int status) {
				return null;
			}

			@Override
			public Response.ResponseBuilder status(int status, String reasonPhrase) {
				this.status = status;
				return this;
			}

			@Override
			public Response.ResponseBuilder entity(Object entity) {
				this.entity = entity;
				return this;
			}

			@Override
			public Response.ResponseBuilder entity(Object entity, Annotation[] annotations) {
				return null;
			}

			@Override
			public Response.ResponseBuilder allow(String... methods) {
				return null;
			}

			@Override
			public Response.ResponseBuilder allow(Set<String> methods) {
				return null;
			}

			@Override
			public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
				return null;
			}

			@Override
			public Response.ResponseBuilder encoding(String encoding) {
				return null;
			}

			@Override
			public Response.ResponseBuilder header(String name, Object value) {
				return null;
			}

			@Override
			public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
				return null;
			}

			@Override
			public Response.ResponseBuilder language(String language) {
				return null;
			}

			@Override
			public Response.ResponseBuilder language(Locale language) {
				return null;
			}

			@Override
			public Response.ResponseBuilder type(MediaType type) {
				return null;
			}

			@Override
			public Response.ResponseBuilder type(String type) {
				return null;
			}

			@Override
			public Response.ResponseBuilder variant(Variant variant) {
				return null;
			}

			@Override
			public Response.ResponseBuilder contentLocation(URI location) {
				return null;
			}

			@Override
			public Response.ResponseBuilder cookie(NewCookie... cookies) {
				return null;
			}

			@Override
			public Response.ResponseBuilder expires(Date expires) {
				return null;
			}

			@Override
			public Response.ResponseBuilder lastModified(Date lastModified) {
				return null;
			}

			@Override
			public Response.ResponseBuilder location(URI location) {
				return null;
			}

			@Override
			public Response.ResponseBuilder tag(EntityTag tag) {
				return null;
			}

			@Override
			public Response.ResponseBuilder tag(String tag) {
				return null;
			}

			@Override
			public Response.ResponseBuilder variants(Variant... variants) {
				return null;
			}

			@Override
			public Response.ResponseBuilder variants(List<Variant> variants) {
				return null;
			}

			@Override
			public Response.ResponseBuilder links(Link... links) {
				return null;
			}

			@Override
			public Response.ResponseBuilder link(URI uri, String rel) {
				return null;
			}

			@Override
			public Response.ResponseBuilder link(String uri, String rel) {
				return null;
			}
		});
	}

	/*//TODO 根据与Path匹配结果，降序排列RootResource, 选择第-个的RootResource
	//TODO R1, R2, R1 matched, R2 none R1
	//TODO R1, R2, R1, R2, matched, R1 result < R2 result R1

	@Test
	public void should use_matched_root_resource() {
		ResourceRouter.RootResource
	}

	//TODO 如果没有匹配的 RootResource, 则构造404Response
	//TODO 如果返回的RootResource中无法匹配剩余Path 则构造404的Response
	//TODO 如果ResourceMethod返回null, 则构造204的Response*/

	@Test
	public void should() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		ResourceContext context = Mockito.mock(ResourceContext.class);

		when(request.getServletPath()).thenReturn("/users");
		when(context.getResource(eq(Users.class))).thenReturn(new Users());

		Router router = new Router(runtime, List.of(new ResourceClass(Users.class)));

		OutboundResponse response = router.dispatch(request,context);
		GenericEntity<String> entity = (GenericEntity<String>) response.getEntity();
		assertEquals("all", entity.getEntity());
	}

	static class Router implements ResourceRouter {

		private Runtime runtime;
		private List<Resource> rootResource;

		public Router(Runtime runtime, List<Resource> rootResource) {
			this.runtime = runtime;
			this.rootResource = rootResource;
		}

		@Override
		public OutboundResponse dispatch(HttpServletRequest request, ResourceContext resourceContext) {
//			ResourceContext builder = runtime.createResourceContext(request);
			ResourceMethod resourceMethod = rootResource.stream().map(root -> root.match(request.getServletPath(), "GET", new String[0], null))
					.filter(Optional::isPresent).findFirst().get().get();

			try {
				GenericEntity<?> entity = resourceMethod.call(resourceContext, null);
				return (OutboundResponse) Response.ok(entity).build();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class ResourceClass implements ResourceRouter.Resource {
		private Pattern pattern;
		private String path;
		private Class<?> resourceClass;
		private Map<URITemplate, ResourceRouter.ResourceMethod> methods = new HashMap<>();

		record URITemplate(Pattern uri, String[] mediaTypes) {}

		public ResourceClass(Class<?> resourceClass) {
			this.resourceClass = resourceClass;
			path = resourceClass.getAnnotation(Path.class).value();
			pattern = Pattern.compile(path + "(/.*)?");

			for (Method method : Arrays.stream(resourceClass.getMethods()).filter(m -> m.isAnnotationPresent(GET.class)).toList()) {
				methods.put(new URITemplate(pattern, method.getAnnotation(Produces.class).value()), new NormalResourceMethod(resourceClass, method));
			}

			for (Method method : Arrays.stream(resourceClass.getMethods()).filter(m -> m.isAnnotationPresent(Path.class)).toList()) {
				Path path = method.getAnnotation(Path.class);
				Pattern pattern = Pattern.compile(this.path + ("(/" + path + ")?"));
				methods.put(new URITemplate(pattern, method.getAnnotation(Produces.class).value()),
						new SubResourceLocator(resourceClass, method, new String[0]));
			}
		}

		@Override
		public Optional<ResourceRouter.ResourceMethod> match(String path, String method, String[] mediaType, UriInfoBuilder builder) {
			if(!pattern.matcher(path).matches()) {
				return Optional.empty();
			}
			return methods.entrySet().stream().filter(e -> e.getKey().uri.matcher(path).matches())
					.map(e -> e.getValue()).findFirst();
		}
	}

	static class NormalResourceMethod implements ResourceRouter.ResourceMethod {
		private Class<?> resourceClass;
		private Method method;

		public NormalResourceMethod(Class<?> resourceClass, Method method) {
			this.resourceClass = resourceClass;
			this.method = method;
		}

		@Override
		public GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder) {
			Object resource = resourceContext.getResource(resourceClass);
			try {
				return new GenericEntity<>(method.invoke(resource), method.getGenericReturnType());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class SubResourceLocator implements ResourceRouter.ResourceMethod {
		private Class<?> resourceClass;
		private Method method;
		private String[] medaType;

		public SubResourceLocator(Class<?> resourceClass, Method method, String[] medaType) {
			this.resourceClass = resourceClass;
			this.method = method;
			this.medaType = medaType;
		}

		@Override
		public GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder) {
			Object resource = resourceContext.getResource(resourceClass);
			try {
				Object subResource = method.invoke(resource);
				return new SubResource(subResource).match(builder.getUnmatchedPath(),
						"GET", medaType, builder).get().call(resourceContext, builder);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class SubResource implements ResourceRouter.Resource {
		private final Class<? extends Object> subResourceClass;
		private Object subResource;
		private Map<ResourceClass.URITemplate, ResourceRouter.ResourceMethod> methods = new HashMap<>();

		public SubResource(Object subResource) {
			this.subResource = subResource;
			this.subResourceClass = subResource.getClass();
		}

		@Override
		public Optional<ResourceRouter.ResourceMethod> match(String path, String method, String[] mediaType, UriInfoBuilder builder) {
			return Optional.empty();
		}
	}

	@Path("/users")
	static class Users {
		@GET
		@Produces(MediaType.TEXT_PLAIN)
		public String asText() {
			return "all";
		}

		@Path("/orders")
		public Orders getOrders() {
			return new Orders();
		}
	}

	static class Orders {
		@GET
		public String asText() {
			return "all";
		}
	}
}
