package com.example.oceanresourcefilemanagement;

import com.example.json.JacksonObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

//@SpringBootTest
class OceanResourceFileManagementApplicationTests {

    @Test
    public void testToString() {
        List<Long> ids = List.of(10L, 20L);
        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        System.out.println("idsParam = " + idsParam);
        System.out.println("String.valueOf(ids) = " + String.valueOf(ids));
        System.out.println("ids.toString() = " + ids.toString());
    }

    @Test
    void testLocalDateSerialization() throws JsonProcessingException {
        ObjectMapper mapper = new JacksonObjectMapper();
        LocalDate date = LocalDate.of(2023, 10, 5);
        String json = mapper.writeValueAsString(date);
        Assertions.assertEquals("\"2023-10-05\"", json); // 确保格式匹配
    }

    @Test
    void testLocalDateDeserialization() throws JsonProcessingException {
        ObjectMapper mapper = new JacksonObjectMapper();
        String json = "\"2023-10-05\"";
        LocalDate date = mapper.readValue(json, LocalDate.class);
        Assertions.assertEquals(LocalDate.of(2023, 10, 5), date);
    }

    @Test
    public void test() {
        System.out.println(largestGoodInteger("6777133339"));
    }


    public String largestGoodInteger(String num) {
        int len = 1;
        char before = 0;
        int res = -1;
        for (int i = 0; i < num.length(); i++) {
            char current = num.charAt(i);
            if (before == current) {
                len++;
                if (len == 3) {
                    res = Math.max(Integer.parseInt("" + before), res);
                }
            }
            else {
                before = current;
                len = 1;
            }
        }
        return res == -1 ? "" : String.valueOf(res).repeat(3);
    }

}
