package llb.tdd.di;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: DefaultResourceMethodTest
 * @date 2022-11-16 8:16:24
 * @ProjectName tdd
 * @Version V1.0
 */
public class DefaultResourceMethodTest {
	@Test
	public void should_call_resource_method() throws NoSuchMethodException {
		CallableResourceMethods resource = Mockito.mock(CallableResourceMethods.class);
		ResourceContext context = Mockito.mock(ResourceContext.class);
		UriInfoBuilder builder = Mockito.mock(UriInfoBuilder.class);
		Mockito.when(builder.getLastMatchedResource()).thenReturn(resource);
		Mockito.when(resource.get()).thenReturn("resource called");

		DefaultResourceMethod resourceMethod = new DefaultResourceMethod(CallableResourceMethods.class.getMethod("get"));

		Assertions.assertEquals(new GenericEntity("resource called", String.class), resourceMethod.call(context, builder));
	}

	// TODO return type, List<String>
	// TODO injection - context
	// TODO injection - uri info: path,query, matrix...


	interface CallableResourceMethods {
		@GET
		String get();
	}
}
