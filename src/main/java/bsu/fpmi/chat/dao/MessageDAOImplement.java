package bsu.fpmi.chat.dao;

import bsu.fpmi.chat.db.ConnectionManager;
import bsu.fpmi.chat.model.Message;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAOImplement implements MessageDAO {
    private static Logger logger = Logger.getLogger(MessageDAOImplement.class.getName());

    @Override
    public void add(Message message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("INSERT INTO messages (id, username, message, state, sender) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, message.getIntId());
            preparedStatement.setString(2, message.getUserName());
            preparedStatement.setString(3, message.getMessage());
            preparedStatement.setString(4, message.getState());
            preparedStatement.setString(5, message.getSender());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void update(Message message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("UPDATE messages SET message = ?, state = ? WHERE id = ?");
            preparedStatement.setString(1, message.getMessage());
            preparedStatement.setString(2, message.getState());
            preparedStatement.setInt(3, message.getIntId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public List<Message> selectAll() {
        List<Message> messages = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM messages");
            while (resultSet.next()) {
                String id = Integer.toString(resultSet.getInt("id"));
                String username = resultSet.getString("username");
                String message = resultSet.getString("message");
                String state = resultSet.getString("state");
                String sender = resultSet.getString("sender");
                messages.add(new Message(id, username, message, state, sender));
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return messages;
    }

}