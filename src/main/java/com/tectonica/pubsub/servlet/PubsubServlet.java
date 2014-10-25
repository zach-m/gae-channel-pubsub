package com.tectonica.pubsub.servlet;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.tectonica.pubsub.intf.PubsubPersister;
import com.tectonica.pubsub.persist.PubsubInMemStore;

@SuppressWarnings("serial")
public class PubsubServlet extends AbstractPubsubServlet
{
	private static final String ADMIN_TOPIC = "_admin";

	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private PubsubPersister pubsubStore = PubsubInMemStore.get();

	@Override
	public String connect(String clientId)
	{
		if (clientId == null || clientId.isEmpty())
			clientId = UUID.randomUUID().toString();
		String token = channelService.createChannel(clientId);
		pubsubStore.setClientToken(clientId, token);

		publishAdminMessage(new MessagePayload("connected clientId " + clientId, ADMIN_TOPIC));
		
		return token;
	}

	@Override
	public void connectionStateChanged(HttpServletRequest req)
	{
		try
		{
			ChannelPresence presence = channelService.parsePresence(req);
			String clientId = presence.clientId();
			boolean connected = presence.isConnected();

			if (!connected)
			{
				// handle client disconnection
				String token = pubsubStore.removeClientToken(clientId);
				if (token == null)
					System.err.println("WARNING: disconnected clientId " + clientId + " wasn't previously registered");
				else
				{
					System.out.println(String.format("clientId %s disconnected (token %s)", clientId, token));
					pubsubStore.detachSubscriber(token);

					publishAdminMessage(new MessagePayload("disconnected clientId " + clientId, ADMIN_TOPIC));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public PublishResponse publish(String topic, String msg, String excludeToken)
	{
		MessagePayload mp = new MessagePayload(msg, topic);

		publishAdminMessage(mp);

		return publishMessage(mp, topic, excludeToken);
	}

	private void publishAdminMessage(MessagePayload mp)
	{
		publishMessage(mp, ADMIN_TOPIC, null);
	}

	private PublishResponse publishMessage(MessagePayload mp, String topic, String excludeToken)
	{
		PublishResponse response = new PublishResponse();

		Set<String> subscribers = pubsubStore.getSubscribers(topic);
		if (subscribers != null)
		{
			response.subCount = subscribers.size();
			subscribers.remove(excludeToken);
			String payloadJson = mp.toJson();
			for (String token : subscribers)
				channelService.sendMessage(new ChannelMessage(token, payloadJson));
		}

		return response;
	}

	@Override
	public SubscribeResponse subscribe(String topic, String token, boolean autoCreateTopic)
	{
		SubscribeResponse response = new SubscribeResponse();
		response.subCount = pubsubStore.attachSubscriber(topic, token, autoCreateTopic);

		publishAdminMessage(new MessagePayload("#" + response.subCount + " subscribed " + topic, ADMIN_TOPIC));

		return response;
	}
}
