package com.tectonica.pubsub.intf;

import javax.servlet.http.HttpServletRequest;

import com.tectonica.util.Jackson2;

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

		public MessagePayload(String msg, String topic)
		{
			this.msg = msg;
			this.topic = topic;
		}

		public String toJson()
		{
			return Jackson2.fieldsToJson(this);
		}
	}

	static class PublishResponse
	{
		public int subCount = 0;
	}
}
