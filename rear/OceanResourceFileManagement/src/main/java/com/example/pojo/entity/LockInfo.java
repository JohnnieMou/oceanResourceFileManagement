package com.example.pojo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LockInfo {

    private Object lock;

    private Long userId;    // 当前持有锁的用户ID

    private LocalDateTime lockTime; // 加锁时间

    public LockInfo(Long userId, LocalDateTime lockTime) {
        this.lock = new Object();
        this.userId = userId;
        this.lockTime = lockTime;
    }
}
