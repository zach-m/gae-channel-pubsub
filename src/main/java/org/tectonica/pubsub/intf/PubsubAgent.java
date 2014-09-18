package org.tectonica.pubsub.intf;

import javax.servlet.http.HttpServletRequest;

public interface PubsubAgent
{
	public String connect(String clientId);

	public void connectionStateChanged(HttpServletRequest req);

	public PublishResponse publish(String topic, String msg, String excludeToken);

	public SubscribeResponse subscribe(String topic, String token, boolean autoCreateTopic);

	static class SubscribeResponse
	{
		public int subCount;
	}

	static class MessagePayload
	{
		public String msg;
		public String topic;
	}

	static class PublishResponse
	{
		public int subCount;
	}
}
