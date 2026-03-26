package org.mybatis.jpetstore.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.domain.Cart;
import org.mybatis.jpetstore.service.AccountService;
import org.mybatis.jpetstore.service.CatalogService;
import org.mybatis.jpetstore.service.LogService;
import org.mybatis.jpetstore.web.support.SessionState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {
  private static final List<String> LANGUAGE_LIST = List.of("english", "japanese");
  private static final List<String> CATEGORY_LIST = List.of("FISH", "DOGS", "REPTILES", "CATS", "BIRDS");

  private final AccountService accountService;
  private final CatalogService catalogService;
  private final LogService logService;

  public AccountController(AccountService accountService, CatalogService catalogService, LogService logService) {
    this.accountService = accountService;
    this.catalogService = catalogService;
    this.logService = logService;
  }

  @GetMapping("/signin")
  public String signInForm() {
    return "account/signin";
  }

  @PostMapping("/signin")
  public String signIn(@RequestParam String username, @RequestParam String password, @RequestParam String captchaInput,
      HttpServletRequest request, RedirectAttributes redirectAttributes) {
    String captcha = (String) request.getSession().getAttribute(CaptchaController.CAPTCHA_SESSION_KEY);
    if (captcha == null || captchaInput == null || !captcha.equalsIgnoreCase(captchaInput.trim())) {
      redirectAttributes.addFlashAttribute("error", "验证码错误");
      return "redirect:/account/signin";
    }
    Account account = accountService.getAccount(username, password);
    if (account == null) {
      redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
      return "redirect:/account/signin";
    }
    account.setPassword(null);
    HttpSession session = request.getSession();
    SessionState.setAccount(session, account);

    Cart mergedCart = SessionState.getOrCreateCart(session);
    mergedCart = catalogService.mergeCart(account.getUsername(), mergedCart);
    SessionState.setCart(session, mergedCart);

    logService.logSimple(account.getUsername(), LogService.ActionTypes.USER_LOGIN, "用户成功登录", request);
    return "redirect:/catalog";
  }

  @PostMapping("/signout")
  public String signOut(HttpServletRequest request) {
    HttpSession session = request.getSession();
    Account account = SessionState.getAccount(session);
    if (account != null) {
      logService.logSimple(account.getUsername(), LogService.ActionTypes.USER_LOGOUT, "用户登出", request);
    }
    session.invalidate();
    return "redirect:/catalog";
  }

  @GetMapping("/register")
  public String registerForm(Model model) {
    model.addAttribute("account", new Account());
    model.addAttribute("languages", LANGUAGE_LIST);
    model.addAttribute("categories", CATEGORY_LIST);
    return "account/register";
  }

  @PostMapping("/register")
  public String register(Account account, @RequestParam String repeatedPassword, @RequestParam String captchaInput,
      HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
    String captcha = (String) request.getSession().getAttribute(CaptchaController.CAPTCHA_SESSION_KEY);
    if (captcha == null || captchaInput == null || !captcha.equalsIgnoreCase(captchaInput.trim())) {
      model.addAttribute("error", "验证码错误");
      model.addAttribute("languages", LANGUAGE_LIST);
      model.addAttribute("categories", CATEGORY_LIST);
      return "account/register";
    }
    if (account.getUsername() == null || account.getUsername().trim().isEmpty() || account.getPassword() == null
        || account.getPassword().trim().isEmpty()) {
      model.addAttribute("error", "用户名和密码不能为空");
      model.addAttribute("languages", LANGUAGE_LIST);
      model.addAttribute("categories", CATEGORY_LIST);
      return "account/register";
    }
    if (!account.getPassword().equals(repeatedPassword)) {
      model.addAttribute("error", "两次密码不一致");
      model.addAttribute("languages", LANGUAGE_LIST);
      model.addAttribute("categories", CATEGORY_LIST);
      return "account/register";
    }
    accountService.insertAccount(account);
    Account dbAccount = accountService.getAccount(account.getUsername());
    dbAccount.setPassword(null);
    SessionState.setAccount(request.getSession(), dbAccount);
    logService.logSimple(dbAccount.getUsername(), LogService.ActionTypes.USER_REGISTER, "新用户注册成功", request);
    redirectAttributes.addFlashAttribute("message", "注册成功");
    return "redirect:/catalog";
  }

  @GetMapping("/profile")
  public String editProfileForm(HttpSession session, Model model) {
    Account account = SessionState.getAccount(session);
    if (account == null) {
      return "redirect:/account/signin";
    }
    Account dbAccount = accountService.getAccount(account.getUsername());
    dbAccount.setPassword("");
    model.addAttribute("account", dbAccount);
    model.addAttribute("languages", LANGUAGE_LIST);
    model.addAttribute("categories", CATEGORY_LIST);
    return "account/profile";
  }

  @PostMapping("/profile")
  public String editProfile(Account account, HttpSession session, HttpServletRequest request,
      RedirectAttributes redirectAttributes) {
    Account sessionAccount = SessionState.getAccount(session);
    if (sessionAccount == null) {
      return "redirect:/account/signin";
    }
    account.setUsername(sessionAccount.getUsername());
    accountService.updateAccount(account);
    Account updated = accountService.getAccount(sessionAccount.getUsername());
    updated.setPassword(null);
    SessionState.setAccount(session, updated);
    logService.logSimple(updated.getUsername(), "更新账户信息", "用户更新了个人信息", request);
    redirectAttributes.addFlashAttribute("message", "更新成功");
    return "redirect:/account/profile";
  }

  @GetMapping("/check-username")
  @ResponseBody
  public Map<String, Boolean> checkUsername(@RequestParam String username) {
    return Map.of("exists", accountService.getAccount(username) != null);
  }
}
