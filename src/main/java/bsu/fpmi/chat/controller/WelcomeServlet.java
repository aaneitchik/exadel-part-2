package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.model.MessageStorage;
import bsu.fpmi.chat.util.ServletUtil;
import bsu.fpmi.chat.util.XMLUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static bsu.fpmi.chat.util.MessageUtil.*;

@WebServlet("/index")
public final class WelcomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(WelcomeServlet.class.getName());
    private static String filepath = "users.xml";

    @Override
    public void init() {
        File file = new File(filepath);
        if (!file.exists() || file.isDirectory()) {
            XMLUtil.getInstance().startWritingUsersToXML(filepath);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String data = ServletUtil.getMessageBody(request);
        logger.info("Request for signing in received");
        try {
            JSONObject json = stringToJson(data);
            String email = (String) (json.get("email"));
            String password = (String) (json.get("password"));
            String actionType = (String) (json.get("actionType"));

            response.setContentType(ServletUtil.APPLICATION_JSON);
            PrintWriter out = response.getWriter();

            if(actionType.equals("signin")) {
                if (XMLUtil.getInstance().validateUser(email, password, filepath)) {
                    out.print(formResponse("OK"));
                    logger.info("User profile validated, opening chat");
                } else {
                    out.print(formResponse("ERROR"));
                    logger.info("Wrong email or password");
                }
            } else if (actionType.equals("signup")) {
                if (XMLUtil.getInstance().addUserToXML(email, password, filepath)) {
                    out.print(formResponse("OK"));
                    logger.info("User " + email + " added successfully");
                } else {
                    out.print(formResponse("ERROR"));
                    logger.info("User with this name is already registered");
                }
            }

            out.flush();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException e) {
            logger.error("Invalid user message " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    private String formResponse(String response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("answer", response);
        return jsonObject.toJSONString();
    }
}