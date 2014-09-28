package com.tectonica.pubsub.intf;

import java.util.Set;

public interface PubsubPersister
{
	public void setClientToken(String clientId, String token);

	public String getClientToken(String clientId);

	public String removeClientToken(String clientId);

	public int attachSubscriber(String topic, String token, boolean autoCreateTopic);

	public void detachSubscriber(String token);

	public Set<String> getSubscribers(String topic);
}
