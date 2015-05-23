package bsu.fpmi.chat.dao;

import java.util.List;
import bsu.fpmi.chat.model.Message;

public interface MessageDAO {
    void add(Message message);

    void update(Message message); //used both for update and delete

    List<Message> selectAll();
}