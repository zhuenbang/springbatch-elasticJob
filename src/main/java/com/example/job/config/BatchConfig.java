package com.example.job.config;

import com.example.job.batch.listener.MigrateUserJobListener;
import com.example.job.batch.model.User;
import com.example.job.batch.writer.UserWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.annotation.Resource;

import javax.sql.DataSource;

@Configuration
public class BatchConfig extends DefaultBatchConfigurer {

  @Resource
  private JobBuilderFactory jobBuilderFactory;

  @Resource
  private StepBuilderFactory stepBuilderFactory;

  @Autowired
  private DataSource batchDatasource;

  @Autowired
  private MigrateProperties migrateProperties;

  @Autowired
  private MigrateUserJobListener migrateUserJobListener;

   @Override
  protected JobRepository createJobRepository() throws Exception {
    JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
    factory.setDataSource(batchDatasource);
    factory.setTransactionManager(this.getTransactionManager());
    factory.afterPropertiesSet();
    factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");
    return factory.getObject();
  }

//  /**
//   * 数据记录使用存取使用内存，减少IO
//   *
//   * @return
//   * @throws Exception
//   */
//  @Override
//  protected JobRepository createJobRepository() throws Exception {
//    JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean()
//    MapJobRepositoryFactoryBean mapJobRepository = new MapJobRepositoryFactoryBean();
//    return mapJobRepository.getObject();
//  }

  /**
   * 数据读取， 先去基准表
   *
   * @param startId
   * @param endId
   * @return
   */
  @Bean
  @StepScope
  public JdbcCursorItemReader<User> userReader(
      @Value("#{jobParameters[startId]}") Long startId,
      @Value("#{jobParameters[endId]}") Long endId) {
    JdbcCursorItemReader<User> cursorItemReader = new JdbcCursorItemReaderBuilder()
        .dataSource(batchDatasource)
        .name("userReader")
        .sql("select * from admin_account where id > '" + startId + "' and id<= '" + endId + "'")
        .rowMapper(new BeanPropertyRowMapper(User.class))
        .maxRows(5000000)
        .fetchSize(migrateProperties.getFetchSize())
        .queryTimeout(10000)
        .build();
    return cursorItemReader;
  }

  @Bean
  public Step userMigrateStep(@Qualifier("userReader") ItemReader reader,
      UserWriter userWriter) {
    return this.stepBuilderFactory.get("userMigrateStep")
        .<User, User>chunk(migrateProperties.getChunkSize())
        .reader(reader)
        .processor(new PassThroughItemProcessor())
        .writer(userWriter)
        .throttleLimit(8)
        .build();
  }

  @Bean
  public Job userMigrateJob(@Qualifier("userMigrateStep") Step step) {
    return this.jobBuilderFactory.get("userMigrateJob")
        .start(step)
        .listener(migrateUserJobListener)
        .incrementer(new RunIdIncrementer())
        .build();
  }



}
