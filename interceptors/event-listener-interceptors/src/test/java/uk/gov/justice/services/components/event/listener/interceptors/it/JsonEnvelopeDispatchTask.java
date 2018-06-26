package uk.gov.justice.services.components.event.listener.interceptors.it;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;

public class JsonEnvelopeDispatchTask implements Callable<Optional<JsonEnvelope>>, ManagedTask {

    private final JsonEnvelope jsonEnvelope;
    private final AsynchronousDispatchBean asynchronousDispatchBean;
    private final ManagedTaskListener managedTaskListener;

    public JsonEnvelopeDispatchTask(final JsonEnvelope jsonEnvelope,
                                    final AsynchronousDispatchBean asynchronousDispatchBean,
                                    final ManagedTaskListener managedTaskListener) {
        this.jsonEnvelope = jsonEnvelope;
        this.asynchronousDispatchBean = asynchronousDispatchBean;
        this.managedTaskListener = managedTaskListener;
    }

    @Override
    public Optional<JsonEnvelope> call() throws Exception {
        return asynchronousDispatchBean.process(jsonEnvelope);
    }

    @Override
    public ManagedTaskListener getManagedTaskListener() {
        return managedTaskListener;
    }

    @Override
    public Map<String, String> getExecutionProperties() {
        return null;
    }
}
