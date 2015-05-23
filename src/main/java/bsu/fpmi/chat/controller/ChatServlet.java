package bsu.fpmi.chat.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bsu.fpmi.chat.dao.MessageDAO;
import bsu.fpmi.chat.dao.MessageDAOImplement;
import bsu.fpmi.chat.util.MessageUtil;
import org.apache.log4j.Logger;

import bsu.fpmi.chat.model.Message;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import bsu.fpmi.chat.util.ServletUtil;

import static bsu.fpmi.chat.util.MessageUtil.*;

@WebServlet(urlPatterns = {"/chat"}, asyncSupported = true)
public final class ChatServlet extends HttpServlet {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm ");
	private final static Queue<AsyncContext> contexts = new ConcurrentLinkedQueue<>();

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ChatServlet.class.getName());
	private MessageDAO messageDAO;

	@Override
	public void init() {
		this.messageDAO = new MessageDAOImplement();
	}

	@Override
	protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		logger.info("Get request received");
		String token = request.getParameter(TOKEN);
		int index = getIndex(token);
		if (token != null && !"".equals(token)) {
			response.setContentType(ServletUtil.APPLICATION_JSON);
			if (index != 0) { //because when it's loaded the first time, it should always send messages
				response.setCharacterEncoding("UTF-8");
				final AsyncContext context = request.startAsync(request, response);
				logger.info("Async started");
				context.setTimeout(300000);
				context.addListener(new AsyncListener() {
										@Override
										public void onComplete(AsyncEvent asyncEvent) throws IOException {
											contexts.remove(context);
										}

										@Override
										public void onTimeout(AsyncEvent asyncEvent) throws IOException {
											logger.info("Async timed out");
											sendMessages(context.getResponse());
											context.complete();
										}

										@Override
										public void onError(AsyncEvent asyncEvent) throws IOException {
											logger.info("Async error");
											sendMessages(context.getResponse());
											context.complete();
										}

										@Override
										public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

										}
									}
				);
				contexts.add(context);
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				sendMessages(response);
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String data = ServletUtil.getMessageBody(request);
		try {
			JSONObject json = stringToJson(data);
			Message message = jsonToMessage(json);

			message.setId(messageDAO.selectAll().size() + 1);

			Date currentDate = new Date();
			logger.info(dateFormat.format(currentDate) + message.getUserName() + " : " + message.getMessage());
			messageDAO.add(message);
			response.setStatus(HttpServletResponse.SC_OK);
			completeAsyncContexts(contexts);
		} catch (ParseException e) {
			logger.error("Invalid user message " + e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String id = request.getParameter(ID);
		String data = ServletUtil.getMessageBody(request);
		logger.info("PUT request: id = " + id + " received");
		if (id != null && !"".equals(id)) {
			try {
				Message message = new Message(MessageUtil.stringToJson(data));
				message.setState("modified");
				messageDAO.update(message);
				logger.info("Message was successfully edited");
				completeAsyncContexts(contexts);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			logger.error("Message ID is out of bounds");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		String id = request.getParameter(ID);
		logger.info("DELETE request: id = " + id + " received");
		if (id != null && !"".equals(id)) {
			messageDAO.update(new Message(id, "", "", "modified", "anonymous"));
			logger.info("Message was successfully deleted");
			try {
				completeAsyncContexts(contexts);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Message ID is out of bounds");
		}
	}

	@SuppressWarnings("unchecked")
	private String formResponse() {
		List<JSONObject> messageList = MessageUtil.getMessages(messageDAO.selectAll());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(MESSAGES, messageList);
		jsonObject.put(TOKEN, getToken(messageList.size()));
		return jsonObject.toJSONString();
	}

	private void completeAsyncContexts(Queue<AsyncContext> contexts) throws IOException {
		for (AsyncContext context : contexts) {
			sendMessages(context.getResponse());
			logger.info("Async completed without timing out");
			context.complete();
		}
	}

	private void sendMessages(ServletResponse response) {
		try {
			String messages = formResponse();
			PrintWriter out = response.getWriter();
			out.print(messages);
			out.flush();
			logger.info("Messages sent to clients");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
