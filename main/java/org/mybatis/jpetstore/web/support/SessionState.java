package org.mybatis.jpetstore.web.support;

import javax.servlet.http.HttpSession;

import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.domain.Cart;

public final class SessionState {

  public static final String ACCOUNT = "account";
  public static final String CART = "cart";

  private SessionState() {
  }

  public static Account getAccount(HttpSession session) {
    return (Account) session.getAttribute(ACCOUNT);
  }

  public static void setAccount(HttpSession session, Account account) {
    session.setAttribute(ACCOUNT, account);
  }

  public static Cart getOrCreateCart(HttpSession session) {
    Cart cart = (Cart) session.getAttribute(CART);
    if (cart == null) {
      cart = new Cart();
      session.setAttribute(CART, cart);
    }
    return cart;
  }

  public static void setCart(HttpSession session, Cart cart) {
    session.setAttribute(CART, cart);
  }
}
