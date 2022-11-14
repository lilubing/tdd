package llb.tdd.di;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.xml.sax.Locator;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ResourceRouter
 * @date 2022-11-09 7:35:58
 * @ProjectName tdd
 * @Version V1.0
 */
interface ResourceRouter {
	OutboundResponse dispatch(HttpServletRequest request, ResourceContext resourceContext);
	interface Resource {
		Optional<ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, ResourceContext resourceContext, UriInfoBuilder builder);
	}
	interface RootResource extends Resource, UriHandler {
	}
	interface ResourceMethod extends UriHandler {
		String getHttpMethod();
		GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder);
	}
	interface SubResourceLocator extends UriHandler {
		Resource getSubResource(ResourceContext resourceContext, UriInfoBuilder uriInfoBuilder);
	}
}
class DefaultResourceRouter implements ResourceRouter {
	private Runtime runtime;
	private List<RootResource> rootResources;
	public DefaultResourceRouter(Runtime runtime, List<RootResource> rootResources) {
		this.runtime = runtime;
		this.rootResources = rootResources;
	}
	@Override
	public OutboundResponse dispatch(HttpServletRequest request, ResourceContext resourceContext) {
		String path = request.getServletPath();
		UriInfoBuilder uri = runtime.createUriInfoBuilder(request);
		Optional<ResourceMethod> method = UriHandlers.mapMatched(path, rootResources, (result, resource) -> findResourceMethod(request, resourceContext, uri, result, resource));
		if (method.isEmpty()) return (OutboundResponse) Response.status(Response.Status.NOT_FOUND).build();
		return (OutboundResponse) method.map(m -> m.call(resourceContext, uri)).map(entity -> Response.ok(entity).build())
				.orElseGet(() -> Response.noContent().build());
	}
	private Optional<ResourceMethod> findResourceMethod(HttpServletRequest request, ResourceContext resourceContext, UriInfoBuilder uri, Optional<UriTemplate.MatchResult> matched, RootResource handler) {
		return handler.match(matched.get(), request.getMethod(),
				Collections.list(request.getHeaders(HttpHeaders.ACCEPT)).toArray(String[]::new), resourceContext, uri);
	}
}
class RootResourceClass implements ResourceRouter.RootResource {
	private PathTemplate uriTemplate;
	private Class<?> resourceClass;
	private ResourceMethods resourceMethods;
	private SubResourceLocators subResourceLocators;

	public RootResourceClass(Class<?> resourceClass) {
		this.resourceClass = resourceClass;
		this.uriTemplate = new PathTemplate(resourceClass.getAnnotation(Path.class).value());
		this.resourceMethods = new ResourceMethods(resourceClass.getMethods());
		this.subResourceLocators = new SubResourceLocators(resourceClass.getMethods());
	}
	@Override
	public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, ResourceContext resourceContext, UriInfoBuilder builder) {
		builder.addMatchedResource(resourceContext.getResource(resourceClass));
		String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
		return resourceMethods.findResourceMethods(remaining, method).or(() ->
				subResourceLocators.findSubResourceMethods(remaining, method, mediaTypes, resourceContext, builder));
	}
	@Override
	public UriTemplate getUriTemplate() {
		return uriTemplate;
	}
}
class SubResource implements ResourceRouter.Resource {
	private Object subResource;
	private ResourceMethods resourceMethods;
	public SubResource(Object subResource) {
		this.subResource = subResource;
		this.resourceMethods = new ResourceMethods(subResource.getClass().getMethods());
	}
	@Override
	public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, ResourceContext resourceContext, UriInfoBuilder builder) {
		String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
		return resourceMethods.findResourceMethods(remaining, method);
	}
}
class DefaultResourceMethod implements ResourceRouter.ResourceMethod {
	private String httpMethod;
	private UriTemplate uriTemplate;
	private Method method;
	public DefaultResourceMethod(Method method) {
		this.method = method;
		this.uriTemplate = new PathTemplate(Optional.ofNullable(method.getAnnotation(Path.class)).map(Path::value).orElse(""));
		this.httpMethod = Arrays.stream(method.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(HttpMethod.class))
				.findFirst().get().annotationType().getAnnotation(HttpMethod.class).value();
	}
	@Override
	public String getHttpMethod() {
		return httpMethod;
	}
	@Override
	public UriTemplate getUriTemplate() {
		return uriTemplate;
	}
	@Override
	public GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder) {
		return null;
	}
	@Override
	public String toString() {
		return method.getDeclaringClass().getSimpleName() + "." + method.getName();
	}
}
class ResourceMethods {
	private Map<String, List<ResourceRouter.ResourceMethod>> resourceMethods;
	public ResourceMethods(Method[] methods) {
		this.resourceMethods = getResourceMethods(methods);
	}
	private static Map<String, List<ResourceRouter.ResourceMethod>> getResourceMethods(Method[] methods) {
		return Arrays.stream(methods).filter(m -> Arrays.stream(m.getAnnotations())
						.anyMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
				.map(DefaultResourceMethod::new)
				.collect(Collectors.groupingBy(ResourceRouter.ResourceMethod::getHttpMethod));
	}
	public Optional<ResourceRouter.ResourceMethod> findResourceMethods(String path, String method) {
		return Optional.ofNullable(resourceMethods.get(method)).flatMap(methods -> UriHandlers.match(path, methods, r -> r.getRemaining() == null));
	}
}
class SubResourceLocators {
	private final List<ResourceRouter.SubResourceLocator> subResourceLocators;
	public SubResourceLocators(Method[] methods) {
		subResourceLocators = Arrays.stream(methods).filter(m -> m.isAnnotationPresent(Path.class) &&
						Arrays.stream(m.getAnnotations()).noneMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
				.map((Function<Method, ResourceRouter.SubResourceLocator>) DefaultSubResourceLocator::new).toList();
	}
	public Optional<ResourceRouter.SubResourceLocator> findSubResource(String path) {
		return UriHandlers.match(path, subResourceLocators);
	}

	public Optional<ResourceRouter.ResourceMethod> findSubResourceMethods(String path, String method, String[] mediaTypes, ResourceContext resourceContext, UriInfoBuilder builder) {
		return UriHandlers.mapMatched(path, subResourceLocators, (result, locator) -> locator.getSubResource(resourceContext, builder).match(result.get(), method, mediaTypes, resourceContext, builder));
	}

	static class DefaultSubResourceLocator implements ResourceRouter.SubResourceLocator {
		private PathTemplate uriTemplate;
		private Method method;
		public DefaultSubResourceLocator(Method method) {
			this.method = method;
			this.uriTemplate = new PathTemplate(method.getAnnotation(Path.class).value());
		}
		@Override
		public UriTemplate getUriTemplate() {
			return uriTemplate;
		}
		@Override
		public String toString() {
			return method.getDeclaringClass().getSimpleName() + "." + method.getName();
		}
		@Override
		public ResourceRouter.Resource getSubResource(ResourceContext resourceContext, UriInfoBuilder uriInfoBuilder) {
			Object resource = uriInfoBuilder.getLastMatchedResource();
			try {
				Object subResource = method.invoke(resource);
				uriInfoBuilder.addMatchedResource(subResource);
				return new SubResource(subResource);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}