package uk.gov.justice.subscription.jms.core;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.subscription.domain.builders.EventBuilder.event;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.HashMap;

import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

@RunWith(MockitoJUnitRunner.class)
public class EventFilterCodeGeneratorTest {

    @InjectMocks
    private EventFilterCodeGenerator eventFilterCodeGenerator;

    @Test
    public void shouldGenerateCorrectEventFilterJavaCode() throws Exception {

        final String serviceName = "my-context";
        final String componentName = "EVENT_LISTENER";
        final String jmsUri = "jms:topic:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();


        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .build();


        final HashMap<ActionType, Action> actions = new HashMap<>();
        final Resource resource = new Resource();
        resource.setActions(actions);

        final Action action = mock(Action.class, RETURNS_DEEP_STUBS.get());
        actions.put(POST, action);

        final MimeType mimeType_1 = new MimeType("application/vnd.my-context.events.something-happened+json");
        final MimeType mimeType_2 = new MimeType("application/vnd.my-context.events.something-else-happened+json");

        final String basePackageName = "uk.gov.moj.base.package.name";

        when(action.getBody().values()).thenReturn(asList(mimeType_1, mimeType_2));

        final ClassNameFactory classNameFactory =
                new ClassNameFactory(basePackageName, serviceName, componentName, jmsUri);


        final TypeSpec typeSpec = eventFilterCodeGenerator.generate(subscription, classNameFactory);

        assertThat(typeSpec.toString(), is("@javax.enterprise.context.ApplicationScoped\n" +
                "public class MyContextEventListenerMyContextHandlerCommandEventFilter extends uk.gov.justice.services.event.buffer.api.AbstractEventFilter {\n" +
                "  public MyContextEventListenerMyContextHandlerCommandEventFilter() {\n" +
                "    super(\"my-context.events.something-happened\",\"my-context.events.something-else-happened\");}\n" +
                "}\n"));
    }
}
