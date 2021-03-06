package com.tectonica.pubsub.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tectonica.pubsub.intf.PubsubAgent;
import com.tectonica.util.Jackson2;
import com.tectonica.util.ServletUtil;

@SuppressWarnings("serial")
public abstract class AbstractPubsubServlet extends HttpServlet implements PubsubAgent
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		try
		{
			ServletUtil.applyCORS(req, resp);

			String uri = req.getPathInfo();

			if (uri == null || uri.isEmpty() || uri.equals("/"))
			{
				// CONNECT request
				resp.setContentType("text/plain");
				String token = connect(req.getParameter("clientId"));
				resp.getWriter().print(token);
			}
			else
			{
				// SUBSCRIBE request
				String[] pieces = uri.split("/");
				if ("subscribe".equals(pieces[1]))
				{
					boolean autoCreateTopic = true; // NOTE: you need to make a security-decision here
					resp.setContentType("application/json");
					SubscribeResponse response = subscribe(pieces[2], pieces[3], autoCreateTopic);
					Jackson2.fieldsToJson(resp.getWriter(), response);
				}
			}
		}
		catch (Exception e)
		{
			resp.setContentType("text/plain");
			resp.getWriter().println(e.toString());
			resp.sendError(404);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String sp = req.getServletPath();
		if (sp.startsWith("/_ah/channel/connected") || sp.startsWith("/_ah/channel/disconnected"))
		{
			connectionStateChanged(req);
			return;
		}

		try
		{
			// PUBLISH request
			ServletUtil.applyCORS(req, resp);

			String topic = req.getPathInfo().substring(1);
			String msg = ServletUtil.streamToString(req.getInputStream());

			// TODO: check if the caller is authorized to publish to this topic (IP, Headers, etc.)

			resp.setContentType("application/json");
			PublishResponse response = publish(topic, msg, req.getParameter("exclude"));
			Jackson2.fieldsToJson(resp.getWriter(), response);
		}
		catch (Exception e)
		{
			resp.setContentType("text/plain");
			resp.getWriter().println(e.toString());
			resp.sendError(404);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		ServletUtil.applyCORS(req, resp);
		resp.setContentType("text/plain");
		resp.setStatus(204);
	}
}
