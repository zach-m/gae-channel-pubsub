package org.tectonica.pubsub;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class PubsubStore
{
	private ConcurrentHashMap<String, String> clientToToken = new ConcurrentHashMap<>();

	public void setClientToken(String clientId, String token)
	{
		clientToToken.put(clientId, token);
	}

	public String getClientToken(String clientId)
	{
		return clientToToken.get(clientId);
	}

	public String removeClientToken(String clientId)
	{
		return clientToToken.remove(clientId);
	}

	// /////////////////////////////////////////////

	private ConcurrentMultimap<String, String> topicToSubscribers = new ConcurrentMultimap<>();

	public int attachSubscriber(String topic, String token)
	{
		return topicToSubscribers.add(topic, token);
	}

	public void detachSubscriber(String token)
	{
		topicToSubscribers.removeFromAll(token);
	}

	public Set<String> getSubscribers(String topic)
	{
		return topicToSubscribers.valuesOf(topic);
	}

	// /////////////////////////////////////////////

	private PubsubStore()
	{
		// singleton
	}

	private static PubsubStore instance = new PubsubStore();

	public static PubsubStore get()
	{
		return instance;
	}
}