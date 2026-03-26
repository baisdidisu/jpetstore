package org.mybatis.jpetstore.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.*;

/**
 * 购物车Mapper接口
 */
public interface CartMapper {

  /**
   * 添加商品到购物车
   */
  @Insert("INSERT INTO cart (username, itemid, quantity) VALUES (#{username}, #{itemid}, #{quantity})")
  void addItem(@Param("username") String username, @Param("itemid") String itemid, @Param("quantity") int quantity);

  /**
   * 更新购物车商品数量
   */
  @Update("UPDATE cart SET quantity = #{quantity} WHERE username = #{username} AND itemid = #{itemid}")
  void updateQuantity(@Param("username") String username, @Param("itemid") String itemid,
      @Param("quantity") int quantity);

  /**
   * 从购物车移除商品
   */
  @Delete("DELETE FROM cart WHERE username = #{username} AND itemid = #{itemid}")
  void removeItem(@Param("username") String username, @Param("itemid") String itemid);

  /**
   * 清空用户购物车
   */
  @Delete("DELETE FROM cart WHERE username = #{username}")
  void clearCart(@Param("username") String username);

  /**
   * 获取用户购物车中的所有商品
   */
  @Select("SELECT itemid, quantity FROM cart WHERE username = #{username}")
  List<Map<String, Object>> getCartItems(@Param("username") String username);

  /**
   * 获取某个商品在购物车中的数量
   */
  @Select("SELECT quantity FROM cart WHERE username = #{username} AND itemid = #{itemid}")
  Integer getItemQuantity(@Param("username") String username, @Param("itemid") String itemid);

  /**
   * 获取用户购物车中的商品总数
   */
  @Select("SELECT COUNT(*) FROM cart WHERE username = #{username}")
  int getItemCount(@Param("username") String username);

  /**
   * 批量添加商品到购物车（用于导入）
   */
  @Insert({ "<script>", "INSERT INTO cart (username, itemid, quantity) VALUES ",
      "<foreach collection='items' item='item' separator=','>", "(#{username}, #{item.itemId}, #{item.quantity})",
      "</foreach>", "</script>" })
  void batchAddItems(@Param("username") String username, @Param("items") List<Map<String, Object>> items);
}
