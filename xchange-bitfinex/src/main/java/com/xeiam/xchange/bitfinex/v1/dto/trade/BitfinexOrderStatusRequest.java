package com.xeiam.xchange.bitfinex.v1.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

public class BitfinexOrderStatusRequest {

  @JsonProperty("request")
  protected String request;

  @JsonProperty("nonce")
  protected String nonce;

  @JsonProperty("order_id")
  @JsonRawValue
  private int orderId;

  /**
   * Constructor
   * 
   * @param nonce
   * @param orderId
   */
  public BitfinexOrderStatusRequest(String nonce, int orderId) {

    this.request = "/v1/order/status";
    this.orderId = orderId;
    this.nonce = nonce;
  }

  public String getOrderId() {

    return String.valueOf(orderId);
  }

}
