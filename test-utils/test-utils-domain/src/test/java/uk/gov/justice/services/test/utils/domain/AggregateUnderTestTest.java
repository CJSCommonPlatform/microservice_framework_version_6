package uk.gov.justice.services.test.utils.domain;

import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.domain.AggregateUnderTest.aggregateUnderTest;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.test.utils.domain.arg.ComplexArgument;
import uk.gov.justice.services.test.utils.domain.event.SthDoneWithIntArgEvent;
import uk.gov.justice.services.test.utils.domain.event.SthDoneWithNoArgsEvent;
import uk.gov.justice.services.test.utils.domain.event.InitialEventA;
import uk.gov.justice.services.test.utils.domain.event.InitialEventB;
import uk.gov.justice.services.test.utils.domain.event.SthDoneWithStringArgEvent;
import uk.gov.justice.services.test.utils.domain.event.SthElseDoneEvent;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class AggregateUnderTestTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldInitialiseAggregateWithNoInitialEvents() throws Exception {
        AggregateUnderTest aggregateUnderTest = aggregateUnderTest().initialiseFromClass(AggregateDummy1.class.getName());

        assertThat(aggregateUnderTest.object, instanceOf(AggregateDummy1.class));
        assertThat(((AggregateDummy1) aggregateUnderTest.object).appliedEvents(), empty());
    }

    @Test
    public void shouldNotInitialiseAggregateIfAlreadyInitialised() throws Exception {
        AggregateUnderTest aggregateUnderTest = aggregateUnderTest().initialiseFromClass(AggregateDummy1.class.getName());

        assertThat(aggregateUnderTest.object, sameInstance(aggregateUnderTest.initialiseFromClass(AggregateDummy1.class.getName()).object));
    }

    @Test
    public void shouldCreateAggregateWithInitialEvents() throws Exception {
        AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .withInitialEventsFromFiles("event-a,event-b")
                .initialiseFromClass(AggregateDummy1.class.getName());

        assertThat(aggregateUnderTest.object, instanceOf(AggregateDummy1.class));

        final List<Object> appliedEvents = ((AggregateDummy1) aggregateUnderTest.object).appliedEvents();
        assertThat(appliedEvents, hasSize(2));
        assertThat(appliedEvents.get(0), instanceOf(InitialEventA.class));
        final InitialEventA eventA = (InitialEventA) appliedEvents.get(0);
        assertThat(eventA.getId(), is(UUID.fromString("5c5a1d30-0414-11e7-93ae-92361f002671")));
        assertThat(eventA.getStringFieldA(), is("Abc123"));

        assertThat(appliedEvents.get(1), instanceOf(InitialEventB.class));
        final InitialEventB eventB = (InitialEventB) appliedEvents.get(1);
        assertThat(eventB.getId(), is(UUID.fromString("5c5a1d30-0414-11e7-93ae-92361f002672")));
        assertThat(eventB.getStringFieldB(), is("BCD123"));

    }

    @Test
    public void shouldThrowExceptionIfEventFileDoesNotExist() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Error reading json file: non-existent");


        aggregateUnderTest()
                .withInitialEventsFromFiles("non-existent")
                .initialiseFromClass(AggregateDummy1.class.getName());
    }

    @Test
    public void shouldThrowExceptionIfEventClassDoesNotExist() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Error applying initial event: context.event-no-class. Event class not found");


        aggregateUnderTest()
                .withInitialEventsFromFiles("event-no-class")
                .initialiseFromClass(AggregateDummy1.class.getName());
    }

    @Test
    public void shouldCallNoArgMethod() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithNoArgs", "empty-arg-file");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithNoArgs"));

    }

    @Test
    public void shouldCallMethodWithStringArgumentFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithStringArg", "string-arg-file-abc");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithStringArg"));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), hasItemInArray("stringFormFileABC"));
    }

    @Test
    public void shouldCallTwoMethods() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithNoArgs", "empty-arg-file");
        aggregateUnderTest.invokeMethod("doSthWithStringArg", "string-arg-file-abc");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(2));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithNoArgs"));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 1), is("doSthWithStringArg"));

    }

    @Test
    public void shouldCallMethodWithStringAndUUIDArgumentsFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithStringAndUUIDArg", "string-uuid-args-file-bcd");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithStringAndUUIDArg"));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), arrayContaining("stringFormFileBCD", UUID.fromString("6c5a1d30-0414-11e7-93ae-92361f002671")));
    }

    @Test
    public void shouldCallMethodWithBooleanArgFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithBooleanArg", "boolean-arg-file");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithBooleanArg"));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), arrayContaining(true));
    }

    @Test
    public void shouldCallMethodWithIntegerArgFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithIntArg", "int-arg-file-123");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithIntArg"));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), arrayContaining(123));
    }

    @Ignore("Currently failing")
    @Test
    public void shouldCallMethodWithLongArgFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithLongArg", "long-arg-file-123");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), is("doSthWithLongArg"));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), arrayContaining(123l));
    }

    @Ignore("Currently failing")
    @Test
    public void shouldCallMethodWithDateTimeArgFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithDateTimeArg", "date-time-arg-file");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(methodInvocationArgumentsOf(aggregateUnderTest, 0), is("doSthWithDateTimeArg"));
        assertTrue(((ZonedDateTime) methodInvocationArgumentsOf(aggregateUnderTest, 0)[0])
                .isEqual(ZonedDateTime.parse("2017-01-21T16:42:03.522Z", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))));
    }

    @Test
    public void shouldCallMethodWithComplexArgFromFile() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithComplexArg", "complex-arg-file");

        assertThat(methodInvocationsCountOf(aggregateUnderTest), is(1));
        assertThat(nameOfInvokedMethodOn(aggregateUnderTest, 0), is("doSthWithComplexArg"));
        final ComplexArgument arg = (ComplexArgument) methodInvocationArgumentsOf(aggregateUnderTest, 0)[0];
        assertThat(arg.getBooleanArg(), is(true));
        assertThat(arg.getStringArg(), is("someString"));
        assertThat(arg.getIntArg(), is(456));
    }

    @Test
    public void shouldReturnInfoAboutGeneratedEvent() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithStringArg", "string-arg-file-abc");

        assertThat(aggregateUnderTest.generatedEvents(), hasSize(1));
        assertThat(aggregateUnderTest.generatedEvents().get(0), instanceOf(SthDoneWithStringArgEvent.class));
        assertThat(aggregateUnderTest.generatedEventName(0), is("context.sth-done-with-string-arg"));
        assertThat(aggregateUnderTest.generatedEventAsJsonNode(0).get("strArg").asText(), is("stringFormFileABC"));

    }

    @Test
    public void shouldReturnInfoAboutTwoGeneratedEvents() throws Exception {
        final AggregateUnderTest aggregateUnderTest = aggregateUnderTest()
                .initialiseFromClass(AggregateDummy1.class.getName());

        aggregateUnderTest.invokeMethod("doSthWithIntArg", "int-arg-file-123");
        aggregateUnderTest.invokeMethod("doSthWithStringArg", "string-arg-file-abc");

        assertThat(aggregateUnderTest.generatedEvents(), hasSize(2));
        assertThat(aggregateUnderTest.generatedEvents().get(0), instanceOf(SthDoneWithIntArgEvent.class));
        assertThat(aggregateUnderTest.generatedEventName(0), is("context.sth-done-with-int-arg"));

        assertThat(aggregateUnderTest.generatedEvents().get(1), instanceOf(SthDoneWithStringArgEvent.class));
        assertThat(aggregateUnderTest.generatedEventName(1), is("context.sth-done-with-string-arg"));

    }

    public static class AggregateDummy1 implements Aggregate {


        private List<Object> appliedEvents = new LinkedList<>();
        private List<Pair<String, Object[]>> methodInvocations = new LinkedList<>();

        public Stream<Object> doSthWithNoArgs() {
            methodInvocations.add(Pair.of("doSthWithNoArgs", new Object[]{}));
            return Stream.of(new SthDoneWithNoArgsEvent());
        }

        public Stream<Object> doSthWithStringArg(final String stringArg) {
            methodInvocations.add(Pair.of("doSthWithStringArg", new Object[]{stringArg}));
            return Stream.of(new SthDoneWithStringArgEvent(stringArg));
        }

        public Stream<Object> doSthWithIntArg(final Integer intArg) {
            methodInvocations.add(Pair.of("doSthWithIntArg", new Object[]{intArg}));
            return Stream.of(new SthDoneWithIntArgEvent(intArg));
        }

        public Stream<Object> doSthWithLongArg(final Long longArg) {
            methodInvocations.add(Pair.of("doSthWithLongArg", new Object[]{longArg}));
            return Stream.of(new SthElseDoneEvent());
        }

        public Stream<Object> doSthWithStringAndUUIDArg(final String stringArg, final UUID id) {
            methodInvocations.add(Pair.of("doSthWithStringAndUUIDArg", new Object[]{stringArg, id}));
            return Stream.of(new SthElseDoneEvent());
        }

        public Stream<Object> doSthWithBooleanArg(final Boolean booleanArg) {
            methodInvocations.add(Pair.of("doSthWithBooleanArg", new Object[]{booleanArg}));
            return Stream.of(new SthElseDoneEvent());
        }

        public Stream<Object> doSthWithDateTimeArg(final ZonedDateTime dateTime) {
            methodInvocations.add(Pair.of("doSthWithDateTimeArg", new Object[]{dateTime}));
            return Stream.of(new SthElseDoneEvent());
        }

        public Stream<Object> doSthWithComplexArg(final ComplexArgument complex) {
            methodInvocations.add(Pair.of("doSthWithComplexArg", new Object[]{complex}));
            return Stream.of(new SthElseDoneEvent());
        }

        @Override
        public Object apply(final Object event) {
            return appliedEvents.add(event);
        }

        public List<Object> appliedEvents() {
            return appliedEvents;
        }

        public List<Pair<String, Object[]>> methodInvocations() {
            return methodInvocations;
        }

    }

    public int methodInvocationsCountOf(final AggregateUnderTest aggregateUnderTest) {
        return ((AggregateDummy1) aggregateUnderTest.object).methodInvocations().size();
    }

    private List<Pair<String, Object[]>> methodInvocationsOf(final AggregateUnderTest aggregateUnderTest) {
        return ((AggregateDummy1) aggregateUnderTest.object).methodInvocations();
    }

    public String nameOfInvokedMethodOn(final AggregateUnderTest aggregateUnderTest, final int invocationNumber) {
        return ((AggregateDummy1) aggregateUnderTest.object).methodInvocations().get(invocationNumber).getLeft();
    }

    public Object[] methodInvocationArgumentsOf(final AggregateUnderTest aggregateUnderTest, final int invocationNumber) {
        return ((AggregateDummy1) aggregateUnderTest.object).methodInvocations().get(invocationNumber).getRight();
    }
}