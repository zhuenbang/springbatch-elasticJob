package com.example.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "migrate")
@Component
@Data
public class MigrateProperties {

  /**
   *  每次取多少数据
   */
  private Integer fetchSize = 1;

  /**
   * 每次提交数
   */
  private Integer chunkSize = 1;

  /**
   * 用户迁移步长
   */
  private Integer userMigrateStep = 1;

}
