package org.mybatis.jpetstore.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.mybatis.jpetstore.domain.UserLog;

public interface LogMapper {

  @Insert("INSERT INTO user_log (username, action_type, action_detail, item_id, order_id, ip_address, action_time) "
      + "VALUES (#{username}, #{actionType}, #{actionDetail}, #{itemId}, #{orderId}, #{ipAddress}, #{actionTime})")
  void insertLog(UserLog log);

  @Select("SELECT * FROM user_log WHERE username = #{username} ORDER BY action_time DESC")
  List<UserLog> getLogsByUsername(String username);

  @Select("SELECT * FROM user_log ORDER BY action_time DESC LIMIT 100")
  List<UserLog> getRecentLogs();
}
