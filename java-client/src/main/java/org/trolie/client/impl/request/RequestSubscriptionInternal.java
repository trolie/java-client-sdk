package org.trolie.client.impl.request;

import org.trolie.client.RequestSubscription;

import java.util.concurrent.Future;

/**
 * Internal (non API) common subscription behaviors.
 */
public interface RequestSubscriptionInternal extends RequestSubscription {

    void start();

    Future<Void> stop();

}
