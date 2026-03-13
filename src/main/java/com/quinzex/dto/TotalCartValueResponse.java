package com.quinzex.dto;

import lombok.Data;

@Data
public class TotalCartValueResponse {
    private Double totalAmount;
  private Double gstPercentage;
  private Double gstAmount;
    private Double  subTotal;
}
