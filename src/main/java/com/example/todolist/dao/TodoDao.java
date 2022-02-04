package com.example.todolist.dao;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoDao {
    //Criteria APIによるページとクエリ検索
    Page<Todo> findByCriteria(TodoQuery todoQuery,Pageable pageable);
}