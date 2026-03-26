package org.mybatis.jpetstore.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.domain.Cart;
import org.mybatis.jpetstore.domain.CartItem;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.service.CatalogService;
import org.mybatis.jpetstore.service.LogService;
import org.mybatis.jpetstore.web.support.SessionState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cart")
public class CartController {
  private final CatalogService catalogService;
  private final LogService logService;

  public CartController(CatalogService catalogService, LogService logService) {
    this.catalogService = catalogService;
    this.logService = logService;
  }

  @GetMapping
  public String viewCart(HttpSession session, Model model) {
    model.addAttribute("cart", SessionState.getOrCreateCart(session));
    return "cart/cart";
  }

  @PostMapping("/items/{itemId}")
  public String addItem(@PathVariable String itemId, HttpSession session, HttpServletRequest request) {
    Cart cart = SessionState.getOrCreateCart(session);
    if (cart.containsItemId(itemId)) {
      cart.incrementQuantityByItemId(itemId);
    } else {
      Item item = catalogService.getItem(itemId);
      if (item != null) {
        cart.addItem(item, catalogService.isItemInStock(itemId));
      }
    }
    Account account = SessionState.getAccount(session);
    if (account != null) {
      catalogService.addItemToCart(account.getUsername(), itemId, 1);
      logService.logItemAction(account.getUsername(), LogService.ActionTypes.ADD_TO_CART, "添加商品到购物车", itemId, request);
    }
    return "redirect:/cart";
  }

  @PostMapping("/items/{itemId}/remove")
  public String removeItem(@PathVariable String itemId, HttpSession session, HttpServletRequest request) {
    Cart cart = SessionState.getOrCreateCart(session);
    cart.removeItemById(itemId);
    Account account = SessionState.getAccount(session);
    if (account != null) {
      catalogService.removeItemFromCart(account.getUsername(), itemId);
      logService.logItemAction(account.getUsername(), LogService.ActionTypes.REMOVE_FROM_CART, "移除购物车商品", itemId,
          request);
    }
    return "redirect:/cart";
  }

  @PostMapping("/update")
  public String updateCart(HttpSession session, HttpServletRequest request) {
    Cart cart = SessionState.getOrCreateCart(session);
    for (CartItem cartItem : cart.getCartItemList()) {
      String itemId = cartItem.getItem().getItemId();
      String value = request.getParameter(itemId);
      try {
        int quantity = Integer.parseInt(value);
        cart.setQuantityByItemId(itemId, quantity);
      } catch (Exception ignore) {
      }
    }
    cart.getCartItemList().removeIf(ci -> ci.getQuantity() < 1);
    Account account = SessionState.getAccount(session);
    if (account != null) {
      for (CartItem cartItem : cart.getCartItemList()) {
        catalogService.updateCartItemQuantity(account.getUsername(), cartItem.getItem().getItemId(),
            cartItem.getQuantity());
      }
    }
    return "redirect:/cart";
  }

  @PostMapping("/items/{itemId}/ajax")
  @ResponseBody
  public Map<String, Object> updateItemAjax(@PathVariable String itemId, @RequestParam int quantity,
      HttpSession session) {
    Map<String, Object> result = new HashMap<>();
    Cart cart = SessionState.getOrCreateCart(session);
    CartItem cartItem = cart.getCartItem(itemId);
    if (cartItem == null) {
      result.put("ok", false);
      return result;
    }
    if (quantity < 1) {
      cart.removeItemById(itemId);
      result.put("removed", true);
    } else {
      cart.setQuantityByItemId(itemId, quantity);
      result.put("removed", false);
      result.put("quantity", quantity);
      result.put("itemTotal", cart.getCartItem(itemId).getTotal());
    }
    result.put("ok", true);
    result.put("subTotal", cart.getSubTotal());
    result.put("numberOfItems", cart.getNumberOfItems());
    return result;
  }
}
