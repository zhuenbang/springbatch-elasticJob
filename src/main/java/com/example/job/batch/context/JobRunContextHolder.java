package com.example.job.batch.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class JobRunContextHolder {

  public static JobRunContextHolder INSTANCE = new JobRunContextHolder();

  /**
   * 存放当前分片结束的Id
   */
  public static final Map<Integer, Long> endIdContext = new ConcurrentHashMap<>();

  /**
   * 存放当前分片开始的Id
   */
  public static final Map<Integer, Long> startIdContext = new ConcurrentHashMap<>();


  public Optional<Long> getShardingItemExecuteEndId(Integer shardingItem) {
    return Optional.ofNullable(endIdContext.get(shardingItem));

  }
  public void setShardingItemExecuteEndId(Integer sharingItem,Long endId) {
    endIdContext.put(sharingItem, endId);
  }

  public Optional<Long> getShardingItemExecuteStartId(Integer shardingItem) {
    return Optional.ofNullable(startIdContext.get(shardingItem));

  }

  public void setShardingItemExecuteStartId(Integer sharingItem,Long startId) {
    startIdContext.put(sharingItem, startId);
  }


}
