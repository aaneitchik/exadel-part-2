package bsu.fpmi.chat.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bsu.fpmi.chat.util.MessageUtil;
import bsu.fpmi.chat.util.XMLUtil;
import org.apache.log4j.Logger;

import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import bsu.fpmi.chat.util.ServletUtil;

import static bsu.fpmi.chat.util.MessageUtil.*;

@WebServlet("/chat")
public final class ChatServlet extends HttpServlet {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm ");

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
		String token = request.getParameter(TOKEN);

		if (token != null && !"".equals(token)) {
			int index = getIndex(token);
			String messages = formResponse(index);
			response.setContentType(ServletUtil.APPLICATION_JSON);
			PrintWriter out = response.getWriter();
			out.print(messages);
			out.flush();
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String data = ServletUtil.getMessageBody(request);
		try {
			JSONObject json = stringToJson(data);
			Message message = jsonToMessage(json);

			message.setId(MessageStorage.getSize() + 1);
			MessageStorage.addMessage(message);

			Date currentDate = new Date();
			logger.info(dateFormat.format(currentDate) + message.getUserName() + " : " + message.getMessage());
			XMLUtil.getInstance().addMessageToXML(message, currentDate, filepath);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (ParseException e) {
			logger.error("Invalid user message " + e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			logger.error("Message ID is out of bounds");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter(ID);
		logger.info("DELETE request: id = " + id + " received");
		if (id != null && !"".equals(id)) {
			MessageStorage.getMessageById(id).setMessage("");
			MessageStorage.getMessageById(id).setState("modified");
			logger.info("Message was successfully deleted");
			XMLUtil.getInstance().deleteMessageFromXML(id, filepath);
		} else {
			logger.info("Message ID is out of bounds");
		}
	}

	@SuppressWarnings("unchecked")
	private String formResponse(int index) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(MESSAGES, MessageStorage.getSubMessagesByIndex(index));
		jsonObject.put(TOKEN, getToken(MessageStorage.getSize()));
		return jsonObject.toJSONString();
	}

}
