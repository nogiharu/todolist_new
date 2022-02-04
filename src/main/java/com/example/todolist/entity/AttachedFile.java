package com.example.todolist.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attached_file")
@Data
@NoArgsConstructor
public class AttachedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "todo_id")
    private Integer todoId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "note")
    private String note;
}