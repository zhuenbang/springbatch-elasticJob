package com.example.job.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

  @Autowired
  private Environment env;

 /* @Bean
  public DataSource batchDatasource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(env.getProperty("spring.batchDatasource.url"));
    config.setUsername(env.getProperty("spring.batchDatasource.username"));
    config.setPassword(env.getProperty("spring.batchDatasource.password"));
    config.setDriverClassName("com.mysql.jdbc.Driver");
    config.setMaximumPoolSize(50);
    config.setPoolName("batch_thread_pool");
    HikariDataSource dataSource = new HikariDataSource(config);
    return dataSource;
  }
*/

  @Bean("batchTemplate")
  public JdbcTemplate batchTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

}
