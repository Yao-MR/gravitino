/*
 * Copyright 2023 Datastrato.
 * This software is licensed under the Apache License version 2.
 */
package com.datastrato.graviton.dto.responses;

import com.datastrato.graviton.rest.RESTResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Represents a base response for REST API calls. */
@Getter
@EqualsAndHashCode
@ToString
public class BaseResponse implements RESTResponse {

  @JsonProperty("code")
  private final int code;

  /**
   * Constructor for BaseResponse.
   *
   * @param code The response code.
   */
  public BaseResponse(int code) {
    this.code = code;
  }

  /** Default constructor for BaseResponse. (Used for Jackson deserialization.) */
  public BaseResponse() {
    this.code = 0;
  }

  /**
   * Validates the response code.
   *
   * @throws IllegalArgumentException if code value is negative.
   */
  @Override
  public void validate() throws IllegalArgumentException {
    Preconditions.checkArgument(code >= 0, "code must be >= 0");
  }
}