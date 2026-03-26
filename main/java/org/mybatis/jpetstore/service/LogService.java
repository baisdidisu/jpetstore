package org.mybatis.jpetstore.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.mybatis.jpetstore.domain.UserLog;
import org.mybatis.jpetstore.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogService {

  @Autowired
  private LogMapper logMapper;

  // 定义操作类型常量
  public static class ActionTypes {
    public static final String USER_LOGIN = "用户登录";
    public static final String USER_LOGOUT = "用户登出";
    public static final String USER_REGISTER = "用户注册";
    public static final String VIEW_CATEGORY = "查看分类";
    public static final String VIEW_PRODUCT = "查看产品";
    public static final String VIEW_ITEM = "查看商品";
    public static final String SEARCH_PRODUCT = "搜索商品";
    public static final String ADD_TO_CART = "添加购物车";
    public static final String UPDATE_CART = "更新购物车";
    public static final String REMOVE_FROM_CART = "移除购物车";
    public static final String CREATE_ORDER = "创建订单";
    public static final String VIEW_ORDER = "查看订单";
  }

  @Transactional
  public void log(String username, String actionType, String actionDetail, String itemId, Integer orderId,
      HttpServletRequest request) {
    try {
      UserLog log = new UserLog();
      log.setUsername(username);
      log.setActionType(actionType);
      log.setActionDetail(actionDetail);
      log.setItemId(itemId);
      log.setOrderId(orderId);
      log.setActionTime(new Date());

      if (request != null) {
        log.setIpAddress(getClientIp(request));
      }

      logMapper.insertLog(log);
    } catch (Exception e) {
      // 日志记录失败不应影响主业务
      e.printStackTrace();
    }
  }

  // 简化的日志方法
  public void logSimple(String username, String actionType, String actionDetail, HttpServletRequest request) {
    log(username, actionType, actionDetail, null, null, request);
  }

  // 记录商品相关操作
  public void logItemAction(String username, String actionType, String actionDetail, String itemId,
      HttpServletRequest request) {
    log(username, actionType, actionDetail, itemId, null, request);
  }

  // 记录订单相关操作
  public void logOrderAction(String username, String actionType, String actionDetail, Integer orderId,
      HttpServletRequest request) {
    log(username, actionType, actionDetail, null, orderId, request);
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }
}
