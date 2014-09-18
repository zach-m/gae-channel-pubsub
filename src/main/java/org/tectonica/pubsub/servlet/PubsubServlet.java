package org.tectonica.pubsub.servlet;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.tectonica.pubsub.persist.PubsubInMemStore;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class PubsubServlet extends AbstractPubsubServlet
{
	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private PubsubStore pubsubStore = PubsubInMemStore.get();

	@Override
	protected String connect(String clientId)
	{
		if (clientId == null || clientId.isEmpty())
			clientId = UUID.randomUUID().toString();
		String token = channelService.createChannel(clientId);
		pubsubStore.setClientToken(clientId, token);
		System.out.println("token for '" + clientId + "' is: " + token);
		return token;
	}

	@Override
	protected void connectionStateChanged(HttpServletRequest req)
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
	protected JSONObject subscribe(String topic, String token) throws JSONException
	{
		int subCount = pubsubStore.attachSubscriber(topic, token);
		JSONObject content = new JSONObject();
		content.put("subCount", subCount);
		return content;
	}

	@Override
	protected JSONObject publish(String topic, String msg, String excludeToken) throws JSONException
	{
		JSONObject data = new JSONObject();
		data.put("msg", msg);
		data.put("topic", topic);

		int subCount = 0;

		Set<String> subscribers = pubsubStore.getSubscribers(topic);
		if (subscribers != null)
		{
			subscribers.remove(excludeToken);
			String payload = data.toString();
			subCount = subscribers.size();
			for (String token : subscribers)
				channelService.sendMessage(new ChannelMessage(token, payload));
		}

		JSONObject content = new JSONObject();
		content.put("subCount", subCount);

		return content;
	}
}
