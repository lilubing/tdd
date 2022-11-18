package llb.tdd.di;

import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class InjectableCallerTest {
	protected DefaultResourceMethodTest.LastCall lastCall;
	protected SomeServiceInContext service;
	ResourceContext resourceContext;
	UriInfoBuilder builder;
	UriInfo uriInfo;
	MultivaluedHashMap<String, String> parameters;
	Object resource;

	@BeforeEach
	public void before() {
		lastCall = null;
		resource = initResource();


		resourceContext = Mockito.mock(ResourceContext.class);
		builder = Mockito.mock(UriInfoBuilder.class);
		uriInfo = Mockito.mock(UriInfo.class);
		service = Mockito.mock(SomeServiceInContext.class);
		parameters = new MultivaluedHashMap<>();

		Mockito.when(builder.getLastMatchedResource()).thenReturn(resource);
		Mockito.when(builder.createUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getPathParameters()).thenReturn(parameters);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(parameters);
		Mockito.when(resourceContext.getResource(SomeServiceInContext.class)).thenReturn(service);
	}

	protected abstract Object initResource();

	protected String getMethodName(String name, List<? extends Class<?>> classStream) {
		return name + "("+ classStream.stream().map(Class::getSimpleName) .collect(Collectors.joining(",")) + ")";
	}

	@TestFactory
	public List<DynamicTest> inject_convertable_types() {
		List<DynamicTest> tests = new ArrayList<>();
		List<InjectableTypeTestCase> typeCases = List.of(
				new InjectableTypeTestCase(String.class, "string", "string"),
				new InjectableTypeTestCase(int.class, "1", 1),
				new InjectableTypeTestCase(double.class, "3.25", 3.25),
				new InjectableTypeTestCase(float.class, "3.25", 3.25f),
				new InjectableTypeTestCase(short.class, "128", (short) 128),
				new InjectableTypeTestCase(byte.class, "42", (byte) 42),
				new InjectableTypeTestCase(boolean.class, "true", true),
				new InjectableTypeTestCase(BigDecimal.class, "12345", new BigDecimal("12345")),
				new InjectableTypeTestCase(Converter.class, "Factory", Converter.Factory)
		);
		List<String> paramTypes = List.of("getPathParam", "getQueryParam");
		for (String type : paramTypes) {
			for (InjectableTypeTestCase testCase : typeCases) {
				tests.add(DynamicTest.dynamicTest("should inject " + testCase.type().getSimpleName()
						+ " to " + type, () -> {
					verifyResourceMethodCalled(type, testCase.type(), testCase.string(), testCase.value());
				}));
			}
		}
		return tests;
	}

	@TestFactory
	public List<DynamicTest> inject_context_object() {
		List<DynamicTest> tests = new ArrayList<>();
		List<InjectableTypeTestCase> typeCases = List.of(
				new InjectableTypeTestCase(SomeServiceInContext.class, "N/A", service),
				new InjectableTypeTestCase(ResourceContext.class, "N/A", resourceContext),
				new InjectableTypeTestCase(UriInfo.class, "N/A", uriInfo)
		);
		for (InjectableTypeTestCase testCase : typeCases) {
			tests.add(DynamicTest.dynamicTest("should inject " + testCase.type().getSimpleName()
					+ " to getContext", () -> {
				verifyResourceMethodCalled("getContext", testCase.type(), testCase.string(), testCase.value());
			}));
		}
		return tests;
	}

	private void verifyResourceMethodCalled(String method, Class<?> type, String paramString, Object paramValue) throws NoSuchMethodException {
		parameters.put("param", List.of(paramString));

		callInjectable(method, type);

		assertEquals(getMethodName(method, List.of(type)), lastCall.name());
		assertEquals(List.of(paramValue), lastCall.arguments());
	}

	protected abstract void callInjectable(String method, Class<?> type) throws NoSuchMethodException;

	record LastCall(String name, List<Object> arguments) {}

	record InjectableTypeTestCase(Class<?> type, String string, Object value){}
}
