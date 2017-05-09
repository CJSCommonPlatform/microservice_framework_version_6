package uk.gov.justice.services.test.utils.domain;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.domain.AggregateUnderTest.aggregateUnderTest;
import static uk.gov.justice.services.test.utils.domain.AggregateUnderTest.eventNameFrom;
import static uk.gov.justice.services.test.utils.domain.AggregateUnderTest.jsonNodeWithoutMetadataFrom;
import static uk.gov.justice.services.test.utils.domain.AggregateUnderTest.jsonNodesStreamFrom;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DomainTestSteps {

    private AggregateUnderTest aggregateUnderTest;

    @Given("no previous events")
    public void no_previous_events() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        aggregateUnderTest = aggregateUnderTest();
    }

    @Given("there are previous events (.*)")
    public void previous_events(final String fileNames) throws Exception {
        aggregateUnderTest = aggregateUnderTest().withInitialEventsFromFiles(fileNames);

    }

    @When("(.*) to a (.*) using (.*)")
    public void call_method_with_params(final String methodName, final String aggregateName, final String fileName)
            throws Exception {
        aggregateUnderTest.initialiseFromClass(aggregateName).invokeMethod(methodName, fileName);
    }

    @Then("the (.*)")
    public void assert_events_generated(final String fileNames)  {

        final List<JsonNode> jsonNodeList = jsonNodesStreamFrom(fileNames).collect(toList());
        assertThat(aggregateUnderTest.generatedEvents(), hasSize(jsonNodeList.size()));
        int index = 0;
        for (JsonNode jsonNode : jsonNodeList) {
            assertThat(aggregateUnderTest.generatedEventName(index), equalToIgnoringCase(eventNameFrom(jsonNode)));
            assertThat(aggregateUnderTest.generatedEventAsJsonNode(index), equalTo(jsonNodeWithoutMetadataFrom(jsonNode)));
            index++;
        }

    }

}