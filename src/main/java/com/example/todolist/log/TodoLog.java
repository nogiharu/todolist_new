package com.example.todolist.log;

import java.util.List;

import com.example.todolist.entity.Todo;

import org.springframework.validation.BindingResult;

public class TodoLog {
    public static void queryPageLog(int x, int y, int z) {
        System.out.println("[getPageNumber]:" + x + "[getPageSize]:" + y);
        System.out.println("[getResultList.size]:" + z);
        System.out.println("[setFirstResult]:" + x * y);

    }
    public static void queryDataLog(List<Todo> resultList) {
        resultList.stream().forEach(System.out::println);
    }
    public static void result(BindingResult result) {
     
      System.out.println("[Log_result]" + result.hashCode());
    }
}