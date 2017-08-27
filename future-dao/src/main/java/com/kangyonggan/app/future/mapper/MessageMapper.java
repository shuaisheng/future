package com.kangyonggan.app.future.mapper;

import com.kangyonggan.app.future.model.vo.Message;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMapper extends MyMapper<Message> {

    /**
     * 删除消息分发数据
     *
     * @param id
     */
    void deleteMessageUserByMessageId(Long id);

    /**
     * 保存消息分发用户
     *
     * @param messageId
     * @param usernames
     */
    void insertMessageUsers(@Param("messageId") Long messageId, @Param("usernames") String[] usernames);

    /**
     * 查询未读消息数量
     *
     * @param username
     * @return
     */
    int selectMessageCount(String username);

    /**
     * 查找所有消息
     *
     * @param username
     * @return
     */
    List<Message> selectMessagesByUsername(String username);

    /**
     * 把消息更新为已读
     *
     * @param messageId
     * @param username
     */
    void updateMessageUser4Read(@Param("messageId") Long messageId, @Param("username") String username);
}