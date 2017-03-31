package uk.gov.justice.api.resource;

import uk.gov.justice.services.adapter.rest.application.DefaultCommonProviders;

import javax.enterprise.inject.Specializes;

/**
 * Test class for checking that the common providers bean can be overridden.
 */
@Specializes
public class DummyCommonProviders extends DefaultCommonProviders {

}
