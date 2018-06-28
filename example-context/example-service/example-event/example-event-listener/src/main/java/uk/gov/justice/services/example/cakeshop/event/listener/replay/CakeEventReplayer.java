package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import static java.lang.String.format;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.event.sourcing.subscription.EventReplayer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

public class CakeEventReplayer implements EventReplayer {

    @Inject
    Logger logger;

    @Inject
    AsyncCakeEventReplayer asyncCakeEventReplayer;

    @Override
    public void replay(final InterceptorChainProcessor interceptorChainProcessor) {

        final int numberOfStreams = 1;
        final int numberOfEventsToCreate = 1000;

        final List<JsonEnvelope> jsonEnvelopes = new CakeFactory(numberOfStreams).generateEvents(numberOfEventsToCreate);

        logger.info(format("--------------- Starting replay of %s events with %s streams ---------------", numberOfEventsToCreate, numberOfStreams));

        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        jsonEnvelopes.forEach(jsonEnvelope ->
                        asyncCakeEventReplayer.replay(jsonEnvelope, interceptorChainProcessor)
//                interceptorChainProcessor.process(InterceptorContext.interceptorContextWithInput(jsonEnvelope))
        );

        stopWatch.stop();

        logger.info(format("--------------- Finished replay of %s events in %s milliseconds ---------------", numberOfEventsToCreate, stopWatch.getTime()));
    }

}
