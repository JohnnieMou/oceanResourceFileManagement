package com.example.manager;

import com.example.constant.JwtClaimsConstant;
import com.example.constant.LockTypeConstant;
import com.example.context.BaseContext;
import com.example.mapper.UserMapper;
import com.example.pojo.entity.LockInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class LockManager {

    private final UserMapper userMapper;
    private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();

    /**
     * 尝试获取锁
     * @return true if lock acquired, false if already locked by others
     */
    public boolean tryLock(String lockType, String name) {
        Long currentUserId = BaseContext.getUserId();
        String lockKey = lockType + name;
        LockInfo newLock = new LockInfo(currentUserId, LocalDateTime.now());
        LockInfo existingLock = locks.putIfAbsent(lockKey, newLock);
        
        if (existingLock == null) {
            log.info("ID为 {} 的用户获得了 {} 的锁", currentUserId, lockKey);
            return true;
        }

        log.info("ID为 {} 的用户正在操作 {} ，开始时间：{}", existingLock.getUserId(), lockKey, LocalDateTime.now());
        return false;
    }

    /**
     * 释放锁
     */
    public void releaseLock(String lockType, String name) {
        Long currentUserId = BaseContext.getUserId();
        String lockKey = lockType + name;
        LockInfo lockInfo = locks.get(lockKey);
        if (lockInfo != null && lockInfo.getUserId().equals(currentUserId)) {
            locks.remove(lockKey);
            log.info("ID为 {} 的用户释放了 {} 的锁", currentUserId, lockKey);
        }
    }

    /**
     * 获取锁信息
     */
    public Optional<LockInfo> getLockInfo(String lockType, String name) {
        return Optional.ofNullable(locks.get(lockType + name));
    }

    /**
     * 获取当前用户的锁
     */
    public Object getUserLock() {
        Long userId = BaseContext.getUserId();
        return getLock(LockTypeConstant.UserLock, userId);
    }

    /**
     * 根据类型和ID获取锁
     * @param lockType 锁类型（如：user:, table:, sheet:）
     * @param id 实体ID
     */
    public Object getLock(String lockType, Long id) {
        Long userId = BaseContext.getUserId();
        String lockKey = lockType + id;
        return locks.computeIfAbsent(lockKey, k -> new LockInfo(userId, LocalDateTime.now()));
    }

    /**
     * 获取多个实体的组合锁
     * @param lockType 锁类型
     * @param ids ID列表
     */
    public Object getMultiLock(String lockType, List<Long> ids) {
        Long userId = BaseContext.getUserId();
        // 排序防止死锁
        String lockKey = lockType + ids.stream()
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining(":"));
        return locks.computeIfAbsent(lockKey, k -> new LockInfo(userId, LocalDateTime.now()));
    }

    /**
     * 根据类型和名称获取锁
     * @param lockType 锁类型
     * @param name 实体名称
     */
    public Object getLock(String lockType, String name) {
        Long userId = BaseContext.getUserId();
        String lockKey = lockType + name;
        return locks.computeIfAbsent(lockKey, k -> new LockInfo(userId, LocalDateTime.now()));
    }

    /**
     * 定期清理锁
     */
    @Scheduled(fixedRate = 3600000)  // 每小时清理
    public void cleanupLocks() {
        int size = locks.size();
        locks.clear();
        log.info("已清理{}个锁对象", size);
    }
}
