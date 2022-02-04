package com.example.todolist.form;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.AttachedFile;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;

import org.springframework.http.MediaType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TodoData {
    private Integer id;

    @NotBlank
    private String title;

    @NotNull
    private Integer importance;

    @Min(value = 0)
    private Integer urgency;

    private String deadline;

    private String done;

    @Valid
    private List<TaskData> taskList;

    private TaskData newTask;

    private List<AttachedFileData> attachedFileList;

    public TodoData(Todo todo,List<AttachedFile> attachedFiles) {
        // Todo
        id = todo.getId();
        title = todo.getTitle();
        importance = todo.getImportance();
        urgency = todo.getUrgency();
        deadline = Utils.date2str(todo.getDeadline());
        done = todo.getDone();
        // Task
        taskList = new ArrayList<>();
        String dt;
        for (Task task : todo.getTaskList()) {
            dt = Utils.date2str(task.getDeadline());
            taskList.add(new TaskData(task.getId(),task.getTitle(),dt,task.getDone()));
        }
        newTask = new TaskData();
        // AttachedFile
        attachedFileList = new ArrayList<>();
        String fileName;
        //String fext;
        MediaType contentType;
        boolean isOpenNewWindow;
        for (AttachedFile af : attachedFiles) {
            // ファイル名
            fileName = af.getFileName();
            // 拡張子
            //fext = fileName.substring(fileName.lastIndexOf(".") +1);
            // Content-Type
            contentType = Utils.ext2contentType(fileName);
            // 別Windowで表示するか？
            isOpenNewWindow = contentType.equals(MediaType.APPLICATION_OCTET_STREAM) ? false : true;
            attachedFileList.add(
                new AttachedFileData(af.getId(),fileName,af.getNote(),isOpenNewWindow));
        }
    }

    public Todo toEntity() {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setImportance(importance);
        todo.setUrgency(urgency);
        todo.setDeadline(Utils.str2date(deadline));
        todo.setDone(done);

        if(taskList != null){
            for (TaskData taskData : taskList) {
                //System.out.println("[taskData]"+taskData);
               Task task = new Task(taskData.getId(),
               todo,taskData.getTitle(),
               Utils.str2date(taskData.getDeadline()),
               taskData.getDone());
               todo.addTask(task);
            }
        }
        return todo;
    }

    public Task toTaskEntity() {
        Task task = new Task();
        task.setId(newTask.getId());
        task.setTitle(newTask.getTitle());
        task.setDone(newTask.getDone());
        task.setDeadline(Utils.str2date(newTask.getDeadline()));

        return task;
    }
}