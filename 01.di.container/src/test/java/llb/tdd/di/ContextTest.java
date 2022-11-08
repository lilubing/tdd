package llb.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.collections.Sets;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ContextTest
 * @date 2022-11-03 8:31:44
 * @ProjectName 01-di-container
 * @Version V1.0
 */
class ContextTest {
    ContextConfig config;

    @BeforeEach
    public void setUp() {
        config = new ContextConfig();
    }

    @Nested
    class TypeBinding {
        @Test
        public void should_bind_type_to_a_specific_instance() {
            TestComponent instance = new TestComponent() {
            };
            config.bind(TestComponent.class, instance);

            Context context = config.getContext();
            assertSame(instance, context.get(ComponentRef.of(TestComponent.class)).get());
        }

        @ParameterizedTest(name = "supporting [0]")
        @MethodSource
        public void should_bind_type_to_an_injectable_component(Class<? extends TestComponent> componentType) {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(TestComponent.class, componentType);

            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));

            assertTrue(component.isPresent());
            //		assertSame(dependency, component.get().dependency());
        }

        public static Stream<Arguments> should_bind_type_to_an_injectable_component() {

            return Stream.of(Arguments.of(Named.of("Constructor Injection", TypeBinding.ConstructorInjection.class),
                    Arguments.of(Named.of("Field Injection", TypeBinding.FieldInjection.class)),
                    Arguments.of(Named.of("Method Injection", TypeBinding.MethodInjection.class))));
        }

        static class ConstructorInjection implements TestComponent {
            private Dependency dependency;

            @Inject
            public ConstructorInjection(Dependency dependency) {
                this.dependency = dependency;
            }

            @Override
            public Dependency dependency() {
                return dependency;
            }
        }

        static class FieldInjection implements TestComponent {
            @Inject
            Dependency dependency;

            @Override
            public Dependency dependency() {
                return dependency;
            }
        }

        static class MethodInjection implements TestComponent {
            private Dependency dependency;

            @Inject
            void install(Dependency dependency) {
                this.dependency = dependency;
            }

            @Override
            public Dependency dependency() {
                return dependency;
            }
        }

        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        @Test
        public void should_retrieve_empty_for_unbind_type() {
            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        // context
        // TOD0 could get Provider<T> from context
        @Test
        public void should_retrieve_bind_type_as_provider() {
            TestComponent instance = new TestComponent() {
            };
            config.bind(TestComponent.class, instance);

            Context context = config.getContext();

            Provider<TestComponent> provider = context.get(new ComponentRef<Provider<TestComponent>>() {
            }).get();
            assertSame(instance, provider.get());
        }

        @Test
        public void should_not_retrieve_bind_as_unsupported_container() {
            TestComponent instance = new TestComponent() {
            };
            config.bind(TestComponent.class, instance);

            Context context = config.getContext();

            assertFalse(context.get(new ComponentRef<List<TestComponent>>() {
            }).isPresent());
        }

        @Nested
        public class WithQualifier {
            @Test
            public void should_bind_instance_with_multi_qualifiers() {
                TestComponent instance = new TestComponent() {
                };
                config.bind(TestComponent.class, instance, new NamedLiteral("ChoseOne"), new SkywalkerLiteral());

                Context context = config.getContext();

                TestComponent choseOne = context.get(ComponentRef.of(TestComponent.class, new NamedLiteral("ChoseOne"))).get();
                TestComponent skywalker = context.get(ComponentRef.of(TestComponent.class, new SkywalkerLiteral())).get();

                assertSame(instance, choseOne);
                assertSame(instance, skywalker);
            }

            @Test
            public void should_bind_component_with_multi_qualifiers() {
                Dependency dependency = new Dependency() {
                };
                config.bind(Dependency.class, dependency);
                config.bind(InjectionTest.ConstructorInjection.Injection.InjectConstructor.class,
                        InjectionTest.ConstructorInjection.Injection.InjectConstructor.class,
                        new NamedLiteral("ChoseOne"), new SkywalkerLiteral());


                Context context = config.getContext();

                InjectionTest.ConstructorInjection.Injection.InjectConstructor choseOne = context.get(ComponentRef.of(InjectionTest.ConstructorInjection.Injection.InjectConstructor.class,
                        new NamedLiteral("ChoseOne"))).get();
                InjectionTest.ConstructorInjection.Injection.InjectConstructor skywalker = context.get(ComponentRef.of(InjectionTest.ConstructorInjection.Injection.InjectConstructor.class,
                        new SkywalkerLiteral())).get();

                assertSame(dependency, choseOne.dependency);
                assertSame(dependency, skywalker.dependency);
            }

            @Test
            public void should_throw_exception_if_illegal_qualifier_give_to_instance() {
                TestComponent instance = new TestComponent() {
                };
                assertThrows(IllegalComponentException.class, () -> config.bind(TestComponent.class, instance, new TestLiteral()));
            }

            @Test
            public void should_throw_exception_if_illegal_qualifier_give_to_component() {
                assertThrows(IllegalComponentException.class, () ->
                        config.bind(InjectionTest.ConstructorInjection.Injection.InjectConstructor.class,
                                InjectionTest.ConstructorInjection.Injection.InjectConstructor.class,
                                new TestLiteral()));
            }


        }

        @Nested
        public class WithScope {
            static class NotSingleton {
            }

            @Test
            public void should_not_be_singleton_scope_by_default() {
                config.bind(NotSingleton.class, NotSingleton.class);
                Context context = config.getContext();
                assertNotSame(context.get(ComponentRef.of(NotSingleton.class)).get(), context.get(ComponentRef.of(NotSingleton.class)));
            }

            @Test
            public void should_bind_component_as_singleton_scoped() {
                config.bind(NotSingleton.class, NotSingleton.class, new SingletonLiteral());
                Context context = config.getContext();
                assertSame(context.get(ComponentRef.of(NotSingleton.class)).get(), context.get(ComponentRef.of(NotSingleton.class)).get());
            }

            @Singleton
            static class SingletonAnnotated implements Dependency {
            }

            @Test
            public void should_retrieve_scope_annotation_from_component() {
                config.bind(Dependency.class, SingletonAnnotated.class);
                Context context = config.getContext();
                assertSame(context.get(ComponentRef.of(Dependency.class)).get(), context.get(ComponentRef.of(Dependency.class)).get());
            }

            // TODO bind component with customize scope annotation
            @Test
            public void should_bind_component_as_customized_scope() {
                config.scope(Pooled.class, PooledProvider::new);
                config.bind(NotSingleton.class, NotSingleton.class,new PooledLiteral());
                Context context = config.getContext();
                List<NotSingleton> instance = IntStream.range(0, 5).mapToObj(i -> context.get(ComponentRef.of(NotSingleton.class)).get()).toList();
                assertEquals(PooledProvider.MAX, new HashSet<>(instance).size());
            }

            @Nested
            public class WithQualifier {
                @Test
                public void should_not_be_singleton_scope_by_default() {
                    config.bind(NotSingleton.class, NotSingleton.class, new SkywalkerLiteral());
                    Context context = config.getContext();
                    assertNotSame(context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())).get(), context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())));
                }

                @Test
                public void should_bind_component_as_singleton_scoped() {
                    config.bind(NotSingleton.class, NotSingleton.class, new SingletonLiteral(), new SkywalkerLiteral());
                    Context context = config.getContext();
                    assertSame(context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())).get(), context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())).get());
                }

                @Test
                public void should_retrieve_scope_annotation_from_component() {
                    config.bind(Dependency.class, SingletonAnnotated.class, new SkywalkerLiteral());
                    Context context = config.getContext();
                    assertSame(context.get(ComponentRef.of(Dependency.class, new SkywalkerLiteral())).get(), context.get(ComponentRef.of(Dependency.class, new SkywalkerLiteral())).get());
                }
            }
        }
    }

    @Nested
    public class DependencyCheck {
		/*@Test
		public void should_throw_exception_if_cyclic_dependency_found() {
			config.bind(Component.class, ComponentWithInjectConstructor.class);
			config.bind(Dependency.class, DependencyDependedOnComponent.class);

			CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

			Set<Class<?>> classes = Sets.newSet(exception.getComponents());
			assertEquals(2, classes.size());
			assertTrue(classes.contains(Component.class));
			assertTrue(classes.contains(Dependency.class));

		}*/

        @ParameterizedTest
        @MethodSource
        public void should_throw_exception_if_dependency_not_found(Class<? extends TestComponent> component) {
            config.bind(TestComponent.class, component);
            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(Dependency.class, exception.getDependency().type());
            assertEquals(TestComponent.class, exception.getComponent().type());
        }

        public static Stream<Arguments> should_throw_exception_if_dependency_not_found() {
            return Stream.of(Arguments.of(Named.of("Inject Constructor", DependencyCheck.MissingDependencyConstructor.class)),
                    Arguments.of(Named.of("Inject Field", DependencyCheck.MissingDependencyField.class)),
                    Arguments.of(Named.of("Inject Method", DependencyCheck.MissingDependencyMethod.class)),
                    Arguments.of(Named.of("Provider in Inject Constructor", MissingDependencyProviderConstructor.class)),
                    Arguments.of(Named.of("Provider in Inject Field", MissingDependencyProviderField.class)),
                    Arguments.of(Named.of("Provider in Inject Method", MissingDependencyProviderMethod.class)),
                    Arguments.of(Named.of("Scope", MissingDependencyScoped.class)),
                    Arguments.of(Named.of("Scope Provider", MissingDependencyProviderScoped.class))

                    );
        }

        static class MissingDependencyConstructor implements TestComponent {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependencyField implements TestComponent {
            @Inject
            Dependency dependency;
        }

        static class MissingDependencyMethod implements TestComponent {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class MissingDependencyProviderConstructor implements TestComponent {
            @Inject
            public MissingDependencyProviderConstructor(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyProviderField implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }

        static class MissingDependencyProviderMethod implements TestComponent {
            @Inject
            void install(Provider<Dependency> dependency) {
            }
        }

        @Singleton
        static class MissingDependencyScoped implements TestComponent {
            @Inject
            Dependency dependency;
        }

        @Singleton
        static class MissingDependencyProviderScoped implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }

        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        public void should_throw_exception_if_cyclic_dependencies_found(Class<? extends TestComponent> component,
                                                                        Class<? extends Dependency> dependency) {
            config.bind(TestComponent.class, component);
            config.bind(Dependency.class, dependency);

            CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            Set<Class<?>> classes = Sets.newSet(exception.getComponents());

            assertEquals(2, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
        }

        public static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", DependencyCheck.CyclicComponentInjectField.class),
                    Named.of("Inject Method", DependencyCheck.CyclicComponentInjectMethod.class))) {
                for (Named dependency : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicDependencyInjectMethod.class),
                        Named.of("Inject Field", DependencyCheck.CyclicDependencyInjectField.class),
                        Named.of("Inject Method", DependencyCheck.CyclicDependencyInjectMethod.class))) {
                    arguments.add(Arguments.of(component, dependency));
                }
            }
            return arguments.stream();
        }

        static class CyclicComponentInjectConstructor implements TestComponent {
            @Inject
            public CyclicComponentInjectConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectField implements TestComponent {
            @Inject
            Dependency dependency;

        }

        static class CyclicComponentInjectMethod implements TestComponent {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class CyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public CyclicDependencyInjectConstructor(TestComponent component) {
            }
        }

        static class CyclicDependencyInjectField implements TestComponent {
            @Inject
            TestComponent component;

        }

        static class CyclicDependencyInjectMethod implements TestComponent {
            @Inject
            void install(TestComponent component) {
            }
        }

		/*@ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
		@MethodSource
		public void should_throw_exception_if_transitive_cyclic_dependencies_found(Class<? extends Component> component,
																				 Class<? extends Dependency> dependency,
																				 Class<? extends AnotherDependency> anotherDependency) {

			config.bind(Component.class, component);
			config.bind(Dependency.class, dependency);
			config.bind(AnotherDependency.class, anotherDependency);

			CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());
			List<Class<?>> components = Arrays.asList(exception.getComponents());

			assertEquals(3, components.size());
			assertTrue(components.contains(Component.class));
			assertTrue(components.contains(Dependency.class));
			assertTrue(components.contains(AnotherDependency.class));
		}*/

        public static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Inject Constructor1", DependencyCheck.CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", DependencyCheck.CyclicComponentInjectField.class),
                    Named.of("Inject Method", DependencyCheck.CyclicComponentInjectMethod.class))) {
                for (Named dependency : List.of(Named.of("Inject Constructor", DependencyCheck.IndirectCyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", DependencyCheck.IndirectCyclicDependencyInjectField.class),
                        Named.of("Inject Method", DependencyCheck.IndirectCyclicDependencyInjectMethod.class))) {
                    for (Named anotherDependency : List.of(Named.of("Inject Constructor", DependencyCheck.IndirectCyclicDependencyInjectConstructor.class),
                            Named.of("Inject Field", DependencyCheck.IndirectCyclicAnotherDependencyInjectField.class),
                            Named.of("Inject Method", DependencyCheck.IndirectCyclicAnotherDependencyInjectMethod.class))) {
                        arguments.add(Arguments.of(component, dependency, anotherDependency));
                    }
                }
            }
            return arguments.stream();
        }

        static class IndirectCyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public IndirectCyclicDependencyInjectConstructor(AnotherDependency anotherDependency) {
            }
        }

        static class IndirectCyclicDependencyInjectField implements TestComponent {
            @Inject
            AnotherDependency anotherDependency;
        }

        static class IndirectCyclicDependencyInjectMethod implements TestComponent {
            @Inject
            void install(AnotherDependency anotherDependency) {
            }
        }

        static class IndirectCyclicAnotherDependencyInjectConstructor implements AnotherDependency {
            @Inject
            public IndirectCyclicAnotherDependencyInjectConstructor(TestComponent component) {
            }
        }

        static class IndirectCyclicAnotherDependencyInjectField implements AnotherDependency {
            @Inject
            TestComponent component;
        }

        static class IndirectCyclicAnotherDependencyInjectMethod implements AnotherDependency {
            @Inject
            void install(TestComponent component) {
            }
        }

        static class CyclicDependencyProviderConstructor implements Dependency {
            @Inject
            public CyclicDependencyProviderConstructor(Provider<TestComponent> component) {
            }
        }

        @Test
        public void should_not_throw_exception_if_cyclic_dependency_via_provider() {
            config.bind(TestComponent.class, CyclicComponentInjectConstructor.class);
            config.bind(Dependency.class, CyclicDependencyProviderConstructor.class);
            Context context = config.getContext();
            assertTrue(context.get(ComponentRef.of(TestComponent.class)).isPresent());
        }

        @Nested
        public class WithQualifier {
            @ParameterizedTest
            @MethodSource
            public void should_throw_exception_if_dependency_with_qualifier_not_found(Class<? extends TestComponent> component) {
                config.bind(Dependency.class, new Dependency() {
                });
                config.bind(InjectConstructor.class, InjectConstructor.class, new NamedLiteral("Owner"));
                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
                assertEquals(new Component(InjectConstructor.class, new NamedLiteral("Owner")), exception.getComponent());
                assertEquals(new Component(Dependency.class, new SkywalkerLiteral()), exception.getDependency());
            }

            public static Stream<Arguments> should_throw_exception_if_dependency_with_qualifier_not_found() {
                return Stream.of(Named.of("Inject Constructor with Qualifier", InjectConstructor.class),
                        Named.of("Inject Field with Qualifier", InjectField.class),
                        Named.of("Inject Method with Qualifier", InjectMethod.class),
                        Named.of("Provider in Inject Constructor with Qualifier ", InjectConstructorProvider.class),
                        Named.of("Provider in Inject Field with Qualifier", InjectFieldProvider.class),
                        Named.of("Provider in Inject Method with Qualifier", InjectMethodProvider.class)
                ).map(Arguments::of);
            }

            static class InjectConstructor {
                @Inject
                public InjectConstructor(@Skywalker Dependency dependency) {
                }
            }

            static class
            InjectField implements TestComponent {
                @Inject
                @Skywalker
                Dependency dependency;
            }

            static class InjectMethod implements TestComponent {
                @Inject
                void install(@Skywalker Dependency dependency) {
                }
            }

            static class SkywalkerDependency implements Dependency {
                @Inject
                public SkywalkerDependency(@jakarta.inject.Named("ChosenOne") Dependency dependency) {
                }
            }

            static class NotCyclicDependency implements Dependency {
                @Inject
                public NotCyclicDependency(@Skywalker Dependency dependency) {
                }
            }

            static class InjectConstructorProvider implements TestComponent {
                @Inject
                public InjectConstructorProvider(@Skywalker Provider<Dependency> dependency) {
                }
            }

            static class InjectFieldProvider implements TestComponent {
                @Inject
                @Skywalker
                Provider<Dependency> dependency;
            }

            static class InjectMethodProvider implements TestComponent {
                @Inject
                void install(@Skywalker Provider<Dependency> dependency) {
                }
            }

            /*@ParameterizedTest(name = "{1} -> @Skywalker({0}) -> @Named(\" ChosenOne\") not cycLic dependencies")
            @MethodSource*/
            @Test
            public void should_not_throw_cyclic_exception_if_component_with_same_type_tagged_with_different_qualifier() {
                Dependency instance = new Dependency() {
                };
                config.bind(Dependency.class, instance, new NamedLiteral("ChosenOne"));
                config.bind(Dependency.class, SkywalkerDependency.class, new SkywalkerLiteral());
                config.bind(Dependency.class, NotCyclicDependency.class);
                assertDoesNotThrow(() -> config.getContext());
            }

            /*public static Stream<Arguments> should_not_throw_cyclic_exception_if_component_with_same_type_tagged_with_different_qualifier() {
                List<Arguments> arguments = new ArrayList<>();
                for (Named skywalker : List.of(Named.of(" Inject Constructor", SkywalkerInjectConstructor.class),
                        Named.of("Inject Field", SkywalkerInjectFieldclass),
                        Named.of("Inject Method", SkywalkerInjectMethod.class))) {
                    for (Named notCyclic : List.of(Named.of("Inject Constructor", NotCyclicInjectConstructor.class),
                            Named.of("Inject Field", NotCyclicInjectField.class),
                            Named.of("Inject Method", NotCyclicInjectMethod.class)))
                    arguments.add(Arguments.of(skywalker, notCyclic));
                }
            }*/
        }
    }

}

record NamedLiteral(String value) implements jakarta.inject.Named {

    @Override
    public Class<? extends Annotation> annotationType() {
        return jakarta.inject.Named.class;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof jakarta.inject.Named named) return Objects.equals(value, named.value());
        return false;
    }

    @Override
    public int hashCode() {
        return "value".hashCode() * 127 ^ value.hashCode();
    }
}

@java.lang.annotation.Documented
@java.lang.annotation.Retention(RUNTIME)
@jakarta.inject.Qualifier
@interface Skywalker {
}

record SkywalkerLiteral() implements Skywalker {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Skywalker.class;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Skywalker;
    }
}

record TestLiteral() implements Test {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Test.class;
    }
}

record SingletonLiteral() implements Singleton {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Singleton.class;
    }
}

@Scope
@Documented
@Retention(RUNTIME)
@interface Pooled {}

record PooledLiteral() implements Pooled {
    @Override
    public Class<? extends Annotation> annotationType() {
        return Pooled.class;
    }
}

class PooledProvider<T> implements ComponentProvider<T> {
    static int MAX = 2;
    private List<T> pool = new ArrayList<>();
    int current;
    private ComponentProvider<T> provider;

    public PooledProvider(ComponentProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public T get(Context context) {
        if (pool.size() < MAX) {
            pool.add(provider.get(context));
        }
        return pool.get(current++ % MAX);
    }

    @Override
    public List<ComponentRef<?>> getDependencies() {
        return provider.getDependencies();
    }
}