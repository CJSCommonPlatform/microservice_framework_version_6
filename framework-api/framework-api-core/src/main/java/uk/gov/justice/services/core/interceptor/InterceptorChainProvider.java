package uk.gov.justice.services.core.interceptor;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;


public interface InterceptorChainProvider {

    /**
     * Provide the Component to where the InterceptorChainTypes will be used.
     *
     * @return the Component
     */
    String component();

    /**
     * Provide a List containing Pairs of Priority and Interceptor Class for the InterceptorCache to
     * create an InterceptorChain.  Priority order is low is highest. e.g. 1 = is highest priority
     *
     * @return Deque containing Interceptor Classes
     */
    List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes();
}