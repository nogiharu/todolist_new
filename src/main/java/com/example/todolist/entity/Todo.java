package com.example.todolist.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "todo")
@Data
@ToString(exclude = "taskList")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "importance")
    private Integer importance;

    @Column(name = "urgency")
    private Integer urgency;

    @Column(name = "deadline")
    private Date deadline;

    @Column(name = "done")
    private String done;

    @OneToMany(mappedBy = "todo",cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<Task> taskList = new ArrayList<>();

    public void addTask(Task task) {
        //task.setTodo(this);
        taskList.add(task);
    }

}