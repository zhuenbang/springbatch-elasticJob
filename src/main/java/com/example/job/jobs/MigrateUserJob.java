package com.example.job.jobs;

import cn.hutool.core.util.NumberUtil;
import com.example.job.batch.context.JobRunContextHolder;
import com.example.job.config.MigrateProperties;
import com.example.job.constants.MigrateConstants;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
public class MigrateUserJob implements SimpleJob {

  @Resource
  @Qualifier("userMigrateJob")
  private Job userMigrateJob;

  @Resource
  private JobLauncher jobLauncher;

  @Autowired
  private MigrateProperties migrateProperties;

  @Override
  public void execute(ShardingContext shardingContext) {
    //当前分片
    int shardingItem = shardingContext.getShardingItem();
    String shardingParameter = shardingContext.getShardingParameter();
    Iterable<String> params = Splitter.on("@").split(shardingParameter);
    Long taskStartId = Long.valueOf(Iterables.get(params, 0));
    Long taskEndId = Long.valueOf(Iterables.get(params, 1));
    Optional<Long> currentStartId = JobRunContextHolder.INSTANCE.getShardingItemExecuteStartId(
        shardingItem);
    Optional<Long> currentEndId = JobRunContextHolder.INSTANCE.getShardingItemExecuteEndId(
        shardingItem);
    Long executeStartId;
    Long executeEndId;
    if (!currentStartId.isPresent()) {
      log.info("第一次执行");
      executeStartId = taskStartId;
      executeEndId = taskStartId + migrateProperties.getUserMigrateStep();
      JobRunContextHolder.INSTANCE.setShardingItemExecuteStartId(shardingItem, taskStartId);
      JobRunContextHolder.INSTANCE.setShardingItemExecuteEndId(shardingItem, executeEndId);
      log.info("设置上下文成功，startId:{},endId:{}", executeStartId, executeEndId);
    } else {
      log.info("非第一次执行");
      if (NumberUtil.isGreater(new BigDecimal(currentEndId.get()), new BigDecimal(taskEndId))) {
        log.info("当前要执行的id:{},大于了任务最大执行id:{},任务不再执行", currentEndId.get(), taskEndId);
        return;
      }
      executeStartId = JobRunContextHolder.INSTANCE.getShardingItemExecuteStartId(shardingItem).get();
      executeEndId = JobRunContextHolder.INSTANCE.getShardingItemExecuteEndId(shardingItem).get();
    }
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong(MigrateConstants.START_ID, executeStartId)
        .addLong(MigrateConstants.END_ID, executeEndId)
        .addLong(MigrateConstants.SHARDING_ITEM, Long.valueOf(shardingItem))
        .toJobParameters();
    try {
      jobLauncher.run(userMigrateJob, jobParameters);
    } catch (JobExecutionAlreadyRunningException e) {
      log.error("任务已经运行过了", e);
    } catch (JobRestartException e) {
      log.error("任务重启异常", e);
    } catch (JobInstanceAlreadyCompleteException e) {
      log.error("任务已经完成", e);
    } catch (JobParametersInvalidException e) {
      log.error("参数异常", e);
    }
  }

}

