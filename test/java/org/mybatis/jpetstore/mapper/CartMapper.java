package org.mybatis.jpetstore.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.*;

public interface CartMapper {

  @Insert("INSERT INTO cart (username, itemid, quantity) VALUES (#{username}, #{itemid}, #{quantity})")
  void addItem(@Param("username") String username, @Param("itemid") String itemid, @Param("quantity") int quantity);

  @Update("UPDATE cart SET quantity = #{quantity} WHERE username = #{username} AND itemid = #{itemid}")
  void updateQuantity(@Param("username") String username, @Param("itemid") String itemid,
      @Param("quantity") int quantity);

  @Delete("DELETE FROM cart WHERE username = #{username} AND itemid = #{itemid}")
  void removeItem(@Param("username") String username, @Param("itemid") String itemid);

  @Delete("DELETE FROM cart WHERE username = #{username}")
  void clearCart(@Param("username") String username);

  @Select("SELECT itemid, quantity FROM cart WHERE username = #{username}")
  List<Map<String, Object>> getCartItems(@Param("username") String username);
}
