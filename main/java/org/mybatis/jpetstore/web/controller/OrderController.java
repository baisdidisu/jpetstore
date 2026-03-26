package org.mybatis.jpetstore.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.domain.Cart;
import org.mybatis.jpetstore.domain.Order;
import org.mybatis.jpetstore.service.CatalogService;
import org.mybatis.jpetstore.service.LogService;
import org.mybatis.jpetstore.service.OrderService;
import org.mybatis.jpetstore.web.support.SessionState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;
  private final CatalogService catalogService;
  private final LogService logService;

  public OrderController(OrderService orderService, CatalogService catalogService, LogService logService) {
    this.orderService = orderService;
    this.catalogService = catalogService;
    this.logService = logService;
  }

  @GetMapping
  public String list(HttpSession session, Model model) {
    Account account = SessionState.getAccount(session);
    if (account == null) {
      return "redirect:/account/signin";
    }
    List<Order> orders = orderService.getOrdersByUsername(account.getUsername());
    model.addAttribute("orders", orders);
    return "order/list";
  }

  @GetMapping("/new")
  public String newOrderForm(HttpSession session, Model model) {
    Account account = SessionState.getAccount(session);
    if (account == null) {
      return "redirect:/account/signin";
    }
    Cart cart = SessionState.getOrCreateCart(session);
    if (cart.isEmpty()) {
      return "redirect:/cart";
    }
    Order order = new Order();
    order.initOrder(account, cart);
    model.addAttribute("order", order);
    return "order/new";
  }

  @PostMapping
  public String create(Order order, HttpSession session, HttpServletRequest request,
      RedirectAttributes redirectAttributes) {
    Account account = SessionState.getAccount(session);
    if (account == null) {
      return "redirect:/account/signin";
    }
    Cart cart = SessionState.getOrCreateCart(session);
    if (cart.isEmpty()) {
      return "redirect:/cart";
    }
    order.initOrder(account, cart);
    orderService.insertOrder(order);
    catalogService.clearCart(account.getUsername());
    cart.clear();
    logService.logOrderAction(account.getUsername(), LogService.ActionTypes.CREATE_ORDER, "创建订单", order.getOrderId(),
        request);
    redirectAttributes.addFlashAttribute("message", "订单提交成功");
    return "redirect:/orders/" + order.getOrderId();
  }

  @GetMapping("/{orderId}")
  public String view(@PathVariable int orderId, HttpSession session, HttpServletRequest request, Model model) {
    Account account = SessionState.getAccount(session);
    if (account == null) {
      return "redirect:/account/signin";
    }
    Order order = orderService.getOrder(orderId);
    if (order == null || !account.getUsername().equals(order.getUsername())) {
      model.addAttribute("error", "只能查看自己的订单");
      return "common/error";
    }
    logService.logOrderAction(account.getUsername(), LogService.ActionTypes.VIEW_ORDER, "查看订单", orderId, request);
    model.addAttribute("order", order);
    return "order/view";
  }
}
