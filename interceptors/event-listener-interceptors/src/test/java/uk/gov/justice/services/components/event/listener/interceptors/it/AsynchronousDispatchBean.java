package uk.gov.justice.services.components.event.listener.interceptors.it;

import static javax.ejb.TransactionAttributeType.REQUIRED;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;

@Stateful
@Adapter(EVENT_LISTENER)
public class AsynchronousDispatchBean {

    @Resource(name = "openejb/Resource/frameworkviewstore")
    private DataSource dataSource;

    @Inject
    private InterceptorChainProcessor interceptorChainProcessor;

    public void init() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.bind("java:/DS.MultiThreadedEventBufferIT", dataSource);
    }

    @TransactionAttribute(REQUIRED)
    public Optional<JsonEnvelope> process(final JsonEnvelope jsonEnvelope) {
        return interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));
    }

}
