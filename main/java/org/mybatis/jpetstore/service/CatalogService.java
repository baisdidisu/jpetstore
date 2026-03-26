package org.mybatis.jpetstore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.jpetstore.domain.Cart;
import org.mybatis.jpetstore.domain.CartItem;
import org.mybatis.jpetstore.domain.Category;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.domain.Product;
import org.mybatis.jpetstore.mapper.CartMapper;
import org.mybatis.jpetstore.mapper.CategoryMapper;
import org.mybatis.jpetstore.mapper.ItemMapper;
import org.mybatis.jpetstore.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class CatalogService.
 *
 * @author Eduardo Macarron
 */
@Service
public class CatalogService {

  @Autowired
  private CategoryMapper categoryMapper;

  @Autowired
  private ItemMapper itemMapper;

  @Autowired
  private ProductMapper productMapper;

  @Autowired(required = false) // required = false 因为 CartMapper 是新增的，避免启动报错
  private CartMapper cartMapper;

  // ==================== 原有的商品目录功能 ====================

  /**
   * Gets the category list.
   *
   * @return the category list
   */
  public List<Category> getCategoryList() {
    return categoryMapper.getCategoryList();
  }

  /**
   * Gets the category.
   *
   * @param categoryId
   *          the category id
   *
   * @return the category
   */
  public Category getCategory(String categoryId) {
    return categoryMapper.getCategory(categoryId);
  }

  /**
   * Gets the product.
   *
   * @param productId
   *          the product id
   *
   * @return the product
   */
  public Product getProduct(String productId) {
    return productMapper.getProduct(productId);
  }

  /**
   * Gets the product list by category.
   *
   * @param categoryId
   *          the category id
   *
   * @return the product list by category
   */
  public List<Product> getProductListByCategory(String categoryId) {
    return productMapper.getProductListByCategory(categoryId);
  }

  /**
   * Search product list.
   *
   * @param keywords
   *          the keywords
   *
   * @return the list
   */
  public List<Product> searchProductList(String keywords) {
    List<Product> products = new ArrayList<Product>();
    for (String keyword : keywords.split("\\s+")) {
      products.addAll(productMapper.searchProductList("%" + keyword.toLowerCase() + "%"));
    }
    return products;
  }

  /**
   * Gets the item list by product.
   *
   * @param productId
   *          the product id
   *
   * @return the item list by product
   */
  public List<Item> getItemListByProduct(String productId) {
    return itemMapper.getItemListByProduct(productId);
  }

  /**
   * Gets the item.
   *
   * @param itemId
   *          the item id
   *
   * @return the item
   */
  public Item getItem(String itemId) {
    return itemMapper.getItem(itemId);
  }

  /**
   * Is item in stock boolean.
   *
   * @param itemId
   *          the item id
   *
   * @return the boolean
   */
  public boolean isItemInStock(String itemId) {
    return itemMapper.getInventoryQuantity(itemId) > 0;
  }

  // ==================== 新增的购物车持久化功能 ====================

  /**
   * 添加商品到数据库购物车
   *
   * @param username
   *          用户名
   * @param itemId
   *          商品ID
   * @param quantity
   *          数量
   */
  @Transactional
  public void addItemToCart(String username, String itemId, int quantity) {
    if (cartMapper == null) {
      // 如果CartMapper未配置，直接返回（向后兼容）
      return;
    }

    try {
      // 先尝试查询该用户购物车中是否已有此商品
      Integer existingQuantity = cartMapper.getItemQuantity(username, itemId);

      if (existingQuantity != null && existingQuantity > 0) {
        // 如果已存在，更新数量（累加）
        cartMapper.updateQuantity(username, itemId, existingQuantity + quantity);
      } else {
        // 如果不存在，插入新记录
        cartMapper.addItem(username, itemId, quantity);
      }
    } catch (Exception e) {
      // 记录错误但不影响主流程
      e.printStackTrace();
    }
  }

  /**
   * 更新购物车中商品数量
   *
   * @param username
   *          用户名
   * @param itemId
   *          商品ID
   * @param quantity
   *          新数量
   */
  @Transactional
  public void updateCartItemQuantity(String username, String itemId, int quantity) {
    if (cartMapper == null) {
      return;
    }

    try {
      if (quantity <= 0) {
        // 如果数量小于等于0，删除该商品
        cartMapper.removeItem(username, itemId);
      } else {
        cartMapper.updateQuantity(username, itemId, quantity);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 从购物车移除商品
   *
   * @param username
   *          用户名
   * @param itemId
   *          商品ID
   */
  @Transactional
  public void removeItemFromCart(String username, String itemId) {
    if (cartMapper == null) {
      return;
    }

    try {
      cartMapper.removeItem(username, itemId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 清空用户购物车
   *
   * @param username
   *          用户名
   */
  @Transactional
  public void clearCart(String username) {
    if (cartMapper == null) {
      return;
    }

    try {
      cartMapper.clearCart(username);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 从数据库加载用户购物车
   *
   * @param username
   *          用户名
   *
   * @return Cart对象
   */
  public Cart getCartFromDatabase(String username) {
    Cart cart = new Cart();

    if (cartMapper == null) {
      return cart;
    }

    try {
      List<Map<String, Object>> cartData = cartMapper.getCartItems(username);

      for (Map<String, Object> data : cartData) {
        String itemId = (String) data.get("ITEMID"); // 注意：HSQLDB返回的列名可能是大写
        Integer quantity = (Integer) data.get("QUANTITY");

        if (itemId != null && quantity != null) {
          Item item = getItem(itemId);
          if (item != null) {
            boolean inStock = isItemInStock(itemId);
            cart.addItem(item, inStock);
            cart.setQuantityByItemId(itemId, quantity);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return cart;
  }

  /**
   * 同步Session购物车到数据库 用于用户登录时将临时购物车数据持久化
   *
   * @param username
   *          用户名
   * @param cart
   *          Session中的购物车
   */
  @Transactional
  public void syncCartToDatabase(String username, Cart cart) {
    if (cartMapper == null || cart == null) {
      return;
    }

    try {
      // 获取数据库中的购物车
      List<Map<String, Object>> dbCartItems = cartMapper.getCartItems(username);
      Map<String, Integer> dbItemMap = new HashMap<>();

      for (Map<String, Object> data : dbCartItems) {
        String itemId = (String) data.get("ITEMID");
        Integer quantity = (Integer) data.get("QUANTITY");
        if (itemId != null && quantity != null) {
          dbItemMap.put(itemId, quantity);
        }
      }

      // 同步Session购物车到数据库
      List<CartItem> sessionItems = cart.getCartItemList();
      for (CartItem cartItem : sessionItems) {
        String itemId = cartItem.getItem().getItemId();
        int sessionQuantity = cartItem.getQuantity();

        if (dbItemMap.containsKey(itemId)) {
          // 如果数据库中已存在，累加数量
          int totalQuantity = dbItemMap.get(itemId) + sessionQuantity;
          cartMapper.updateQuantity(username, itemId, totalQuantity);
        } else {
          // 如果数据库中不存在，新增
          cartMapper.addItem(username, itemId, sessionQuantity);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 合并数据库购物车到Session购物车 用于用户登录时加载持久化的购物车数据
   *
   * @param username
   *          用户名
   * @param sessionCart
   *          Session中的购物车
   *
   * @return 合并后的购物车
   */
  public Cart mergeCart(String username, Cart sessionCart) {
    if (cartMapper == null) {
      return sessionCart;
    }

    Cart dbCart = getCartFromDatabase(username);

    if (sessionCart == null || sessionCart.getNumberOfItems() == 0) {
      // 如果Session购物车为空，直接返回数据库购物车
      return dbCart;
    }

    if (dbCart.getNumberOfItems() == 0) {
      // 如果数据库购物车为空，将Session购物车同步到数据库
      syncCartToDatabase(username, sessionCart);
      return sessionCart;
    }

    // 合并两个购物车
    Cart mergedCart = new Cart();

    // 先添加数据库购物车的商品
    for (CartItem item : dbCart.getCartItemList()) {
      mergedCart.addItem(item.getItem(), item.isInStock());
      mergedCart.setQuantityByItemId(item.getItem().getItemId(), item.getQuantity());
    }

    // 再添加Session购物车的商品（如果已存在则累加数量）
    for (CartItem item : sessionCart.getCartItemList()) {
      String itemId = item.getItem().getItemId();
      if (mergedCart.containsItemId(itemId)) {
        int currentQty = mergedCart.getCartItem(itemId).getQuantity();
        mergedCart.setQuantityByItemId(itemId, currentQty + item.getQuantity());
      } else {
        mergedCart.addItem(item.getItem(), item.isInStock());
        mergedCart.setQuantityByItemId(itemId, item.getQuantity());
      }
    }

    // 将合并后的购物车同步到数据库
    clearCart(username); // 先清空
    syncCartToDatabase(username, mergedCart); // 再同步

    return mergedCart;
  }

  /**
   * 获取用户购物车中的商品数量
   *
   * @param username
   *          用户名
   *
   * @return 商品数量
   */
  public int getCartItemCount(String username) {
    if (cartMapper == null) {
      return 0;
    }

    try {
      List<Map<String, Object>> items = cartMapper.getCartItems(username);
      int count = 0;
      for (Map<String, Object> item : items) {
        Integer quantity = (Integer) item.get("QUANTITY");
        if (quantity != null) {
          count += quantity;
        }
      }
      return count;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * 检查购物车中是否存在某商品
   *
   * @param username
   *          用户名
   * @param itemId
   *          商品ID
   *
   * @return 是否存在
   */
  public boolean isItemInCart(String username, String itemId) {
    if (cartMapper == null) {
      return false;
    }

    try {
      Integer quantity = cartMapper.getItemQuantity(username, itemId);
      return quantity != null && quantity > 0;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
