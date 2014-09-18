package org.tectonica.pubsub.servlet;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.tectonica.pubsub.intf.PubsubPersister;
import org.tectonica.pubsub.persist.PubsubInMemStore;
import org.tectonica.util.JsonUtil;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

@SuppressWarnings("serial")
public class PubsubServlet extends AbstractPubsubServlet
{
	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private PubsubPersister pubsubStore = PubsubInMemStore.get();

	@Override
	public String connect(String clientId)
	{
		if (clientId == null || clientId.isEmpty())
			clientId = UUID.randomUUID().toString();
		String token = channelService.createChannel(clientId);
		pubsubStore.setClientToken(clientId, token);
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
		MessagePayload mp = new MessagePayload();
		mp.msg = msg;
		mp.topic = topic;

		PublishResponse response = new PublishResponse();
		response.subCount = 0;

		Set<String> subscribers = pubsubStore.getSubscribers(topic);
		if (subscribers != null)
		{
			subscribers.remove(excludeToken);
			String payloadJson = JsonUtil.toJson(mp);
			response.subCount = subscribers.size();
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
		return response;
	}
}
