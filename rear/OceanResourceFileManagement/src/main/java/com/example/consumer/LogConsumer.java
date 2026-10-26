package com.example.consumer;

import com.example.constant.RabbitMQConstant;
import com.example.pojo.entity.OperLog;
import com.example.service.IOperLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogConsumer {

    private final IOperLogService operLogService;

    @RabbitListener(queues = RabbitMQConstant.LOG_QUEUE)
    public void receiveLog(OperLog operLog) {
        String jsonResult = operLog.getJsonResult();
        String errorMsg = operLog.getErrorMsg();

        // 如果jsonResult为空，则设置为null
        if (StringUtils.isBlank(jsonResult)) {
            operLog.setJsonResult("null");
        }
        // 截取jsonResult前2000个的字符
        else if (jsonResult.length() > 2000) {
            operLog.setJsonResult(jsonResult.substring(0, 2000));
        }

        // 如果errorMsg为空，则设置为null
        if (StringUtils.isBlank(errorMsg)) {
            operLog.setErrorMsg("null");
        }
        // 截取errorMsg前2000个的字符
        else if (errorMsg.length() > 2000) {
            operLog.setErrorMsg(errorMsg.substring(0, 2000));
        }

        operLogService.save(operLog);
    }
}