package org.tectonica.pubsub.servlet;

import java.util.Set;

public interface PubsubStore
{
	public void setClientToken(String clientId, String token);

	public String getClientToken(String clientId);

	public String removeClientToken(String clientId);

	public int attachSubscriber(String topic, String token);

	public void detachSubscriber(String token);

	public Set<String> getSubscribers(String topic);
}
