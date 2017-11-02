package uk.gov.justice.services.core.interceptor;

import java.util.List;


public interface InterceptorChainEntryProvider {

    /**
     * Provide the Component to where the InterceptorChainTypes will be used.
     *
     * @return the Component
     */
    String component();

    /**
     * Provide a List containing {@link InterceptorChainEntry} for the InterceptorCache to create an
     * InterceptorChain.  Priority order is low is highest. e.g. 1 = is highest priority
     *
     * @return Deque containing Interceptor Classes
     */
    List<InterceptorChainEntry> interceptorChainTypes();
}
