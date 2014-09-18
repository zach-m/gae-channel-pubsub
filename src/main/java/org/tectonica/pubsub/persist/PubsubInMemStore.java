package org.tectonica.pubsub.persist;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.tectonica.pubsub.servlet.PubsubStore;
import org.tectonica.util.ConcurrentMultimap;

public class PubsubInMemStore implements PubsubStore
{
	private ConcurrentHashMap<String, String> clientToToken = new ConcurrentHashMap<>();

	@Override
	public void setClientToken(String clientId, String token)
	{
		clientToToken.put(clientId, token);
	}

	@Override
	public String getClientToken(String clientId)
	{
		return clientToToken.get(clientId);
	}

	@Override
	public String removeClientToken(String clientId)
	{
		return clientToToken.remove(clientId);
	}

	// /////////////////////////////////////////////

	private ConcurrentMultimap<String, String> topicToSubscribers = new ConcurrentMultimap<>();

	@Override
	public int attachSubscriber(String topic, String token)
	{
		return topicToSubscribers.add(topic, token);
	}

	@Override
	public void detachSubscriber(String token)
	{
		topicToSubscribers.removeFromAll(token);
	}

	@Override
	public Set<String> getSubscribers(String topic)
	{
		return topicToSubscribers.valuesOf(topic);
	}

	// /////////////////////////////////////////////

	private PubsubInMemStore()
	{
		// singleton
	}

	private static PubsubStore instance = new PubsubInMemStore();

	public static PubsubStore get()
	{
		return instance;
	}
}