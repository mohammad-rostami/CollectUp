package com.collect_up.c_up.model;

/**
 * Created by collect-up3 on 7/9/2016.
 */
public class VerificationResult {

  private boolean Success;
  private String Token;
  private String Error;

  public boolean isSuccess() {
    return Success;
  }

  public void setSuccess(boolean success) {
    Success = success;
  }

  public String getToken() {
    return Token;
  }

  public void setToken(String token) {
    Token = token;
  }

  public String getError() {
    return Error;
  }

  public void setError(String error) {
    Error = error;
  }


}
