package uk.gov.justice.services.example.cakeshop.command.api;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeCommandApiTest {

    @Mock
    private Sender sender;

    private Enveloper enveloper = EnveloperFactory.createEnveloper();

    private RecipeCommandApi commandApi;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeCaptor;

    @Before
    public void setup() {
        commandApi = new RecipeCommandApi();
        commandApi.enveloper = enveloper;
        commandApi.sender = sender;
    }

    @Test
    public void shouldHandleRecipeCommands() throws Exception {
        assertThat(RecipeCommandApi.class, isHandlerClass(COMMAND_API)
                .with(method("addRecipe")
                        .thatHandles("example.add-recipe"))
                .with(method("renameRecipe")
                        .thatHandles("example.rename-recipe"))
                .with(method("removeRecipe")
                        .thatHandles("example.remove-recipe"))
                .with(method("uploadPhotograph")
                        .thatHandles("example.upload-photograph"))
        );
    }

    @Test
    public void shouldHandleAddRecipeRequest() {
        commandApi.addRecipe(buildEnvelopeWith("example.add-recipe"));

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("example.command.add-recipe"));
    }

    @Test
    public void shouldHandleRenameRecipeRequest() {
        commandApi.renameRecipe(buildEnvelopeWith("example.rename-recipe"));

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("example.command.rename-recipe"));
    }

    @Test
    public void shouldHandleRemoveRecipeRequest() {
        commandApi.removeRecipe(buildEnvelopeWith("example.remove-recipe"));

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("example.command.remove-recipe"));
    }

    @Test
    public void shouldHandleUploadPhotographRequest() {
        commandApi.uploadPhotograph(buildEnvelopeWith("example.upload-photograpgh"));

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("example.command.upload-photograph"));
    }

    private JsonEnvelope buildEnvelopeWith(final String name) {
        return envelope()
                .with(metadataWithDefaults().withName(name))
                .withPayloadOf("Field", "Value").build();
    }
}
