package llb.tdd.di;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
		Optional<ResourceMethod> match(String path, String method, String[] mediaType, UriInfoBuilder builder);
	}

	interface RootResource extends Resource {
		UriTemplate getUriTemplate();
	}

	interface ResourceMethod {
		GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder);
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
		Optional<Result> matched = rootResources.stream().map(resource -> new Result(resource.getUriTemplate().match(path), resource))
				.filter(result -> result.matched.isPresent()).findFirst();

		Optional<ResourceMethod> method = matched.flatMap(result -> result.resource.match(result.matched.get().getRemaining(),
				request.getMethod(), Collections.list(request.getHeaders(HttpHeaders.ACCEPT)).toArray(String[]::new), uri));
		GenericEntity<?> entity = method.map(m -> m.call(resourceContext, uri)).get();
		return (OutboundResponse) Response.ok(entity).build();
	}
	record Result(Optional<UriTemplate.MatchResult> matched, RootResource resource) {
	}
}
