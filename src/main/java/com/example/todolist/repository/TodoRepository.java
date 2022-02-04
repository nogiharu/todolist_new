package com.example.todolist.repository;
import java.util.List;

import com.example.todolist.entity.Todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Integer>{
    List<Todo> findAllByOrderById();
}