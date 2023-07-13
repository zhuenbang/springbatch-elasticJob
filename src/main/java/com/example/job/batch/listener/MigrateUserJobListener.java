package com.example.job.batch.listener;


import com.example.job.batch.context.JobRunContextHolder;
import com.example.job.config.MigrateProperties;
import com.example.job.constants.MigrateConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MigrateUserJobListener implements JobExecutionListener {

    @Autowired
    private MigrateProperties migrateProperties;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("迁移任务开始:开始Id:{},结束Id:{}",
                jobExecution.getJobParameters().getLong(MigrateConstants.START_ID),
                jobExecution.getJobParameters().getLong(MigrateConstants.END_ID));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        log.info("任务执行结果:{}", status);
        Long currentEndId = jobExecution.getJobParameters().getLong(MigrateConstants.END_ID);
        Long next = currentEndId + migrateProperties.getUserMigrateStep();
        Long shardingItem = jobExecution.getJobParameters().getLong(MigrateConstants.SHARDING_ITEM);
        JobRunContextHolder.INSTANCE.setShardingItemExecuteStartId(shardingItem.intValue(), currentEndId);
        JobRunContextHolder.INSTANCE.setShardingItemExecuteEndId(shardingItem.intValue(), next);
        JobRegistry.getInstance().getJobScheduleController("migrateUserJob").triggerJob();
    }
}
