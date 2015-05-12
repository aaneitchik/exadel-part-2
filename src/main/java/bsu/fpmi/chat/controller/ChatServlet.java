package bsu.fpmi.chat.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bsu.fpmi.chat.util.MessageUtil;
import bsu.fpmi.chat.util.XMLUtil;
import com.sun.deploy.net.HttpResponse;
import org.apache.log4j.Logger;

import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import bsu.fpmi.chat.util.ServletUtil;

import static bsu.fpmi.chat.util.MessageUtil.*;

@WebServlet(urlPatterns = {"/chat"}, asyncSupported = true)
public final class ChatServlet extends HttpServlet {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm ");
	private List<AsyncContext> contexts = new LinkedList<>();
	private int messagesDrawn = 0;

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ChatServlet.class.getName());
	private static String filepath = "history.xml";

	@Override
	public void init() {
		File file = new File(filepath);
		if(!file.exists() || file.isDirectory()) {
			XMLUtil.getInstance().startWritingToXML(filepath);
		}
		XMLUtil.historyParser(logger, filepath);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("Get request received");
		String token = request.getParameter(TOKEN);
		if (token != null && !"".equals(token)) {
			response.setContentType(ServletUtil.APPLICATION_JSON);
			if(messagesDrawn == MessageStorage.getSize()) {
				response.setCharacterEncoding("UTF-8");
				final AsyncContext context = request.startAsync(request, response);
				context.setTimeout(10000);
				context.addListener(new AsyncListener() {
										@Override
										public void onComplete(AsyncEvent asyncEvent) throws IOException {
											AsyncContext ac = asyncEvent.getAsyncContext();
											contexts.remove(ac);
										}

										@Override
										public void onTimeout(AsyncEvent asyncEvent) throws IOException {
											logger.info("Async timed out");
											AsyncContext ac = asyncEvent.getAsyncContext();
											contexts.remove(ac);
											sendMessages(ac.getResponse());
											ac.complete();
										}

										@Override
										public void onError(AsyncEvent asyncEvent) throws IOException {
											logger.info("Async error");
											AsyncContext ac = asyncEvent.getAsyncContext();
											contexts.remove(ac);
											sendMessages(ac.getResponse());
											ac.complete();
										}

										@Override
										public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

										}
									}
				);
				contexts.add(context);
			}
			else {
				sendMessages(response);
			}
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String data = ServletUtil.getMessageBody(request);
		List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
		this.contexts.clear();
		try {
			JSONObject json = stringToJson(data);
			Message message = jsonToMessage(json);

			message.setId(MessageStorage.getSize() + 1);
			MessageStorage.addMessage(message);

			Date currentDate = new Date();
			logger.info(dateFormat.format(currentDate) + message.getUserName() + " : " + message.getMessage());
			XMLUtil.getInstance().addMessageToXML(message, currentDate, filepath);
			response.setStatus(HttpServletResponse.SC_OK);
			completeAsyncContexts(asyncContexts);
		} catch (ParseException e) {
			logger.error("Invalid user message " + e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
		this.contexts.clear();
		String id = request.getParameter(ID);
		String data = ServletUtil.getMessageBody(request);
		logger.info("PUT request: id = " + id + " received");
		if (id != null && !"".equals(id)) {
			try {
				Message message = new Message(MessageUtil.stringToJson(data));
				MessageStorage.getMessageById(id).setMessage(message.getMessage());
				MessageStorage.getMessageById(id).setState("modified");
				logger.info("Message was successfully edited");
				XMLUtil.getInstance().editMessageInXML(id, message.getMessage(), filepath);
				completeAsyncContexts(asyncContexts);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			logger.error("Message ID is out of bounds");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
		this.contexts.clear();
		String id = request.getParameter(ID);
		logger.info("DELETE request: id = " + id + " received");
		if (id != null && !"".equals(id)) {
			MessageStorage.getMessageById(id).setMessage("");
			MessageStorage.getMessageById(id).setState("modified");
			logger.info("Message was successfully deleted");
			XMLUtil.getInstance().deleteMessageFromXML(id, filepath);
			try {
				completeAsyncContexts(asyncContexts);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Message ID is out of bounds");
		}
	}

	@SuppressWarnings("unchecked")
	private String formResponse() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(MESSAGES, MessageStorage.getMessages());
		jsonObject.put(TOKEN, getToken(MessageStorage.getSize()));
		return jsonObject.toJSONString();
	}

	private void completeAsyncContexts(List<AsyncContext> contexts) throws IOException {
		for (AsyncContext context : contexts) {
			sendMessages(context.getResponse());
			context.complete();
		}
	}

	private void sendMessages(ServletResponse response) {
		try {
			String messages = formResponse();
			PrintWriter out = response.getWriter();
			out.print(messages);
			out.flush();
			messagesDrawn = MessageStorage.getSize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
