package org.tectonica.pubsub;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public abstract class AbstractPubsubServlet extends HttpServlet
{
	abstract protected String connect(String clientId);

	abstract protected void connectionStateChanged(HttpServletRequest req);

	abstract protected JSONObject publish(String topic, String msg, String excludeToken) throws JSONException;

	abstract protected JSONObject subscribe(String topic, String token) throws JSONException;

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
					resp.setContentType("application/json");
					JSONObject content = subscribe(pieces[2], pieces[3]);
					resp.getWriter().print(content);
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
			String topic = req.getPathInfo().substring(1);
			String msg = ServletUtil.streamToString(req.getInputStream());

			// TODO: check if the caller is authorized to publish to this topic (IP, Headers, etc.)

			ServletUtil.applyCORS(req, resp);

			resp.setContentType("application/json");
			JSONObject content = publish(topic, msg, req.getParameter("exclude"));
			resp.getWriter().print(content);
		}
		catch (Exception e)
		{
			resp.setContentType("text/plain");
			resp.getWriter().println(e.toString());
			resp.sendError(404);
		}
	}
}
