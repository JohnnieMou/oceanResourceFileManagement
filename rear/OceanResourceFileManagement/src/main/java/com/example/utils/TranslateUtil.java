package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.utils.baidu.translate.TransApi;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("all")
public class TranslateUtil {

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            3,
            3,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Value("${baidu.app-id}")
    String appIdTemp;

    @Value("${baidu.security-key}")
    String securityKeyTemp;

    private static String appId;
    private static String securityKey;

    private static TranslateUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
        appId = appIdTemp;
        securityKey = securityKeyTemp;
    }

    // 预处理
    private static String preProcess(String text) {
        //text如果有()则把()和其中内容删去
        text = text.replaceAll("\\([^\\)]*\\)", "");
        //text如果有中文（）则把（）和其中内容删去
        text = text.replaceAll("（[^）]*）", "");
        return text;
    }

    // 结果处理
    private static String resultProcess(String text) {
        return text.toLowerCase().replace(" ", "_").replace("-","_");
    }

    // 百度翻译单个文本
    public static String toEnglish(String text) {
        preProcess(text);

        TransApi api = new TransApi(appId, securityKey);
        String transResult = api.getTransResult(text, "auto", "en");

        Map<String, String> mapResult = JSON.parseObject(transResult, Map.class);
        List<Map<String, String>> resultList = (List<Map<String, String>>) JSONArray.parse(JSON.toJSONString(mapResult.get("trans_result")));
        transResult = resultList.get(0).get("dst");

        return resultProcess(transResult);
    }

    // 百度翻译多个文本
    public static List<String> toEnglish(List<String> texts) {
        List<String> result = new ArrayList<>();
        TransApi api = new TransApi(appId, securityKey);
        for (String text : texts) {
            preProcess(text);

            String transResult = api.getTransResult(text, "auto", "en");

            Map<String, String> mapResult = JSON.parseObject(transResult, Map.class);
            List<Map<String, String>> resultList = (List<Map<String, String>>) JSONArray.parse(JSON.toJSONString(mapResult.get("trans_result")));
            transResult = resultList.get(0).get("dst");

            result.add(resultProcess(transResult));
        }
        return result;
    }

    //百度翻译异步多个文本
    @SneakyThrows
    public static List<String> toEnglishAsyn(List<String> texts) {
        // 使用线程安全的集合
        List<Future<String>> futures = new ArrayList<>();

        for (String text : texts) {
            final String finalText = text; // 使用 final 变量
            Future<String> future = executor.submit(() -> {
                TransApi api = new TransApi(appId, securityKey);
                String cleanedText = preProcess(finalText);
                String transResult = api.getTransResult(cleanedText, "auto", "en");

                Map<String, String> mapResult = JSON.parseObject(transResult, Map.class);
                List<Map<String, String>> resultList = (List<Map<String, String>>) JSONArray.parse(JSON.toJSONString(mapResult.get("trans_result")));
                return resultList.get(0).getOrDefault("dst", ""); // 使用 getOrDefault 避免空指针
            });
            futures.add(future);
        }

        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                System.err.println(e.getMessage()); // 处理未来任务的异常
                results.add(""); // 处理异常情况下的默认返回值
            }
        }

        return results;
    }

    // 百度异步翻译表头
    @SneakyThrows
    public static List<String> fieldToEnglish(Map<Integer, String> fieldHead) {
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        List<Future<String>> futures = Collections.synchronizedList(new ArrayList<>());
        fieldHead.forEach((k, v) -> {
            v = preProcess(v);
            final String finalText = v; // 使用 final 变量
            Future<String> future = executor.submit(() -> {
                TransApi api = new TransApi(appId, securityKey);
                String cleanedText = preProcess(finalText);
                String transResult = api.getTransResult(cleanedText, "auto", "en");

                Map<String, String> mapResult = JSON.parseObject(transResult, Map.class);
                List<Map<String, String>> resultList = (List<Map<String, String>>) JSONArray.parse(JSON.toJSONString(mapResult.get("trans_result")));
                String value = resultList.get(0).getOrDefault("dst", ""); // 使用 getOrDefault 避免空指针

                try {
                    // 检查是否已存在相同的字段名
                    if (results.contains(value)) {
                        int index = 1;
                        String originalV = value;
                        // 添加序号直到字段名唯一
                        while (results.contains(value)) {
                            value = originalV + "_" + index;
                            index++;
                        }
                    }
                    value = resultProcess(value);
                    fieldHead.put(k, value);
                    results.add(value);
                } catch (Exception e) {
                    System.err.println(Arrays.toString(e.getStackTrace())); // 处理未来任务的异常
                    results.add(""); // 处理异常情况下的默认返回值
                }
                return null;
            });
            futures.add(future);
        });

        for (Future<String> future : futures) {
            future.get(); // 处理所有任务的异常
        }

        return results;
    }

    // 百度翻译合并翻译
    public static List<String> combineToEnglish(Map<Integer, String> fieldHead) {
        // 合并所有字段值为一个字符串，用"#"分隔
        String combinedText = fieldHead.values().stream()
                .map(TranslateUtil::preProcess)
                .collect(Collectors.joining("#"));

        // 发送单次翻译请求
        TransApi api = new TransApi(appId, securityKey);
        String transResult = api.getTransResult(combinedText, "auto", "en");

        // 解析返回的结果
        Map<String, String> mapResult = JSON.parseObject(transResult, Map.class);
        List<Map<String, String>> resultList = (List<Map<String, String>>) JSONArray.parse(JSON.toJSONString(mapResult.get("trans_result")));
        String translatedText = resultList.get(0).getOrDefault("dst", "");

        // 根据"#"分隔翻译后的结果
        String[] translatedValues = translatedText.split("#");

        List<String> results = new ArrayList<>();

        // 更新fieldHead和结果列表
        int i = 0;
        for (Map.Entry<Integer, String> entry : fieldHead.entrySet()) {
            String value = translatedValues[i++];
            value = resultProcess(value.trim());

            try {
                // 检查是否已存在相同的字段名
                if (results.contains(value)) {
                    int index = 1;
                    String originalV = value;
                    // 添加序号直到字段名唯一
                    while (results.contains(value)) {
                        value = originalV + "_" + index;
                        index++;
                    }
                }
                entry.setValue(value);
                results.add(value);
            } catch (Exception e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
                results.add("");
            }
        }

        return results;
    }

    // 百度翻译单个文本转中文
    public static String toChinese(String text) {
        preProcess(text);

        TransApi api = new TransApi(appId, securityKey);
        text = text.replace("_", " ");

        String transResult = api.getTransResult(text, "auto", "zh");

        //转为Json，获取结果
        JSONObject jsonObject = JSON.parseObject(transResult);
        JSONArray array = (JSONArray) jsonObject.get("trans_result");
        JSONObject dst = (JSONObject) array.get(0);
        transResult = dst.getString("dst");

        return transResult;
    }
}
