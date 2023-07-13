package com.example.job.batch.model;

import lombok.Data;

@Data
public class User {

  private Long id;

  private String phone;

  private String password;

  private String username;

  private Long departmentId;

  private String extInfo;

  private Integer validStatus;

  private Long createdAt;

  private Long updatedAt;

  private Integer version;

}
