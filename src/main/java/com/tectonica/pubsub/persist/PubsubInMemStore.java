package com.tectonica.pubsub.persist;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tectonica.pubsub.intf.PubsubPersister;
import com.tectonica.util.ConcurrentMultimap;

public class PubsubInMemStore implements PubsubPersister
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
	public int attachSubscriber(String topic, String token, boolean autoCreateTopic)
	{
		return topicToSubscribers.put(topic, token, autoCreateTopic);
	}

	@Override
	public void detachSubscriber(String token)
	{
		topicToSubscribers.removeFromAll(token);
	}

	@Override
	public Set<String> getSubscribers(String topic)
	{
		return topicToSubscribers.get(topic);
	}

	// /////////////////////////////////////////////

	private PubsubInMemStore()
	{
		// singleton
	}

	private static PubsubPersister instance = new PubsubInMemStore();

	public static PubsubPersister get()
	{
		return instance;
	}
}