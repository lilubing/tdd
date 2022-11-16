package llb.tdd.di;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: HeadResourceMethodTest
 * @date 2022-11-16 8:27:33
 * @ProjectName tdd
 * @Version V1.0
 */
public class HeadResourceMethodTest {
	@Test
	public void should_delegate_to_method_for_uri_template() {
		ResourceRouter.ResourceMethod method = Mockito.mock(ResourceRouter.ResourceMethod.class);
		HeadResourceMethod headResourceMethod = new HeadResourceMethod(method);
		UriTemplate uriTemplate = Mockito.mock(UriTemplate.class);
		Mockito.when(method.getUriTemplate()).thenReturn(uriTemplate);
		assertEquals(uriTemplate, headResourceMethod.getUriTemplate());
	}

	public void should_delegate_to_method_for_http_method() {
		ResourceRouter.ResourceMethod method = Mockito.mock(ResourceRouter.ResourceMethod.class);
		HeadResourceMethod headResourceMethod = new HeadResourceMethod(method);

		Mockito.when(method.getHttpMethod()).thenReturn("GET");

		headResourceMethod.getHttpMethod();

		assertEquals("GET", headResourceMethod.getHttpMethod());
	}
}