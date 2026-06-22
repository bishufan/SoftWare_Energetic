package com.salary.module.system.service;

import com.salary.module.system.domain.OperationLog;
import com.salary.module.system.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OperationLogService {
    private final OperationLogRepository logRepository;

    public Page<OperationLog> getLogs(Pageable pageable) {
        return logRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    public void log(String username, String operation, String detail, String ipAddress, boolean success) {
        logRepository.save(OperationLog.builder()
                .username(username).operation(operation).detail(detail)
                .ipAddress(ipAddress).result(success ? "SUCCESS" : "FAILED")
                .createTime(LocalDateTime.now()).build());
    }
}
