package org.mybatis.jpetstore.domain;

import java.io.Serializable;
import java.util.Date;

public class UserLog implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long logId;
  private String username;
  private String actionType;
  private String actionDetail;
  private String itemId;
  private Integer orderId;
  private String ipAddress;
  private Date actionTime;

  public Long getLogId() {
    return logId;
  }

  public void setLogId(Long logId) {
    this.logId = logId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getActionType() {
    return actionType;
  }

  public void setActionType(String actionType) {
    this.actionType = actionType;
  }

  public String getActionDetail() {
    return actionDetail;
  }

  public void setActionDetail(String actionDetail) {
    this.actionDetail = actionDetail;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public Integer getOrderId() {
    return orderId;
  }

  public void setOrderId(Integer orderId) {
    this.orderId = orderId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public Date getActionTime() {
    return actionTime;
  }

  public void setActionTime(Date actionTime) {
    this.actionTime = actionTime;
  }
}