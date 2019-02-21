package uk.gov.justice.services.core.json;

import static java.util.Optional.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import uk.gov.justice.services.core.mapping.MediaType;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BackwardsCompatibleJsonSchemaValidatorTest {

    @Mock
    private SchemaCatalogAwareJsonSchemaValidator schemaCatalogAwareJsonSchemaValidator;

    @Mock
    private FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @InjectMocks
    private BackwardsCompatibleJsonSchemaValidator backwardsCompatibleJsonSchemaValidator;

    @Test
    public void shouldFallBackToFileBaseJsonSchemaValidator() {
        final String envelopeJson = "{}";
        final String actionName = "actionName";

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName);

        verify(fileBasedJsonSchemaValidator).validateWithoutSchemaCatalog(envelopeJson, actionName);
    }

    @Test
    public void shouldFallBackToFileBaseJsonSchemaValidatorIfOptionalMediaTypeIsEmpty() {
        final String envelopeJson = "{}";
        final String actionName = "actionName";

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName, empty());

        verify(fileBasedJsonSchemaValidator).validateWithoutSchemaCatalog(envelopeJson, actionName);
    }

    @Test
    public void shouldFallBackToFileBaseJsonSchemaValidatorIfOptionalMediaTypeIsPresent() {
        final String envelopeJson = "{}";
        final String actionName = "actionName";
        final Optional<MediaType> mediaType = Optional.of(mock(MediaType.class));

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName, mediaType);

        verify(schemaCatalogAwareJsonSchemaValidator).validate(envelopeJson, actionName, mediaType);
    }

    @Test
    public void shouldNotValidateActionWhenConfigureInWhiteList() {
        backwardsCompatibleJsonSchemaValidator.whitelistActionName = "actionName";
        backwardsCompatibleJsonSchemaValidator.postConstruct();

        final String envelopeJson = "{}";
        final String actionName = "actionName";
        final Optional<MediaType> mediaType = Optional.of(mock(MediaType.class));

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName, mediaType);

        verifyZeroInteractions(schemaCatalogAwareJsonSchemaValidator);
    }

    @Test
    public void shouldOnlyValidateActionsWhichAreNotPresentInWhiteList() {
        backwardsCompatibleJsonSchemaValidator.whitelistActionName = "actionName1,actionName2";
        backwardsCompatibleJsonSchemaValidator.postConstruct();

        final String envelopeJson = "{}";
        final String actionName1 = "actionName1";
        final String actionName2 = "actionName2";
        final String actionName3 = "actionName3";
        final Optional<MediaType> mediaType = Optional.of(mock(MediaType.class));

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName1, mediaType);
        verifyZeroInteractions(schemaCatalogAwareJsonSchemaValidator);

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName2, mediaType);
        verifyZeroInteractions(schemaCatalogAwareJsonSchemaValidator);

        backwardsCompatibleJsonSchemaValidator.validate(envelopeJson, actionName3, mediaType);
        verify(schemaCatalogAwareJsonSchemaValidator).validate(envelopeJson, actionName3, mediaType);
    }
}