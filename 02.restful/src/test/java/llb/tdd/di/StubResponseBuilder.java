package llb.tdd.di;

import jakarta.ws.rs.core.*;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: StubResponseBuilder
 * @date 2022-11-16 7:12:32
 * @ProjectName tdd
 * @Version V1.0
 */
public class StubResponseBuilder extends Response.ResponseBuilder {
	private int status;
	private Object entity;
	private Set<String> allowed = new HashSet<>();

	@Override
	public Response build() {
		OutboundResponse response = mock(OutboundResponse.class);
		when(response.getStatus()).thenReturn(status);
		when(response.getEntity()).thenReturn(entity);
		when(response.getAllowedMethods()).thenReturn(allowed);
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
		allowed.addAll(methods);
		return this;
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
