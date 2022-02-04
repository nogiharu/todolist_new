package com.example.todolist.service;

import java.io.BufferedOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.AttachedFile;
import com.example.todolist.form.TaskData;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.AttachedFileRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final MessageSource messageSource;
    private final AttachedFileRepository attachedFileRepository;

    @Value("${attached.file.path}")
    private String ATTACHED_FILE_PATH;
    // --------------------------------------------------------------------------------
    // ファイルアップロード
    // --------------------------------------------------------------------------------
    public void saveAttachedFile(int todoId,String note,MultipartFile fileContents) {
        //アップロードファイル名
        String fileName = fileContents.getOriginalFilename();

        
        //添付ファイルの格納(upload)時刻を取得
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String createTime = sdf.format(new Date());

        //テーブルへの格納するインスタンス作成
        AttachedFile af = new AttachedFile();
        af.setTodoId(todoId);
        af.setFileName(fileName);
        af.setCreateTime(createTime);
        af.setNote(note);
        Path dir = Paths.get(ATTACHED_FILE_PATH);
        Path path = Paths.get(Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, af));

        //アップロードファイルの内容を取得
        byte[] contents;
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path))) {
            //格納フォルダの存在チェック
            if(Files.notExists(path)){
                Files.createDirectory(dir);
            }
            //アップロードファイルを書き込む
            contents = fileContents.getBytes();
            bos.write(contents);
            //テーブルへの記録
            attachedFileRepository.saveAndFlush(af);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // --------------------------------------------------------------------------------
    // 添付ファイルの削除
    // --------------------------------------------------------------------------------
    public void deleteAttachedFile(int afId) {
        AttachedFile af = attachedFileRepository.findById(afId).get();
        Path path = Paths.get(Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, af));
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    // --------------------------------------------------------------------------------
    // Todo削除時の添付ファイルの削除
    // --------------------------------------------------------------------------------
    public void deleteAttachedFiles(int todoId) {
        List<AttachedFile> af = attachedFileRepository.findByTodoIdOrderById(todoId);
        try {
            for (AttachedFile attachedFile : af) {
                Path path = Paths.get(Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, attachedFile));
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
      // List<String> path = af.stream().map(x ->  Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, x)).
    }
    // --------------------------------------------------------------------------------
    // Todo のチェック
    // --------------------------------------------------------------------------------
    public boolean isValid(TodoData todoData, BindingResult result, boolean isCreate, Locale locale) {
        boolean ans = true;
        // 件名が全角スペースだけならエラー
        if (!Utils.isBlank(todoData.getTitle())) {
            if (Utils.isAllDoubleSpace(todoData.getTitle())) {
                FieldError fieldError = new FieldError(
                        result.getObjectName(),
                        "title",
                        messageSource.getMessage("DoubleSpace.todoData.title",null, locale));
                result.addError(fieldError);
                ans = false;
            }
        }
        String deadline = todoData.getDeadline();
        if (!deadline.equals("")) {
            // formatできないならエラーをアド
            if (!Utils.isValidDateFormat(deadline)) {
                FieldError fieldError = new FieldError(
                        result.getObjectName(),
                        "deadline",
                        messageSource.getMessage("InvalidFormat.todoData.deadline",null, locale));
                result.addError(fieldError);
                ans = false;

            } else {
                // 過去日付チェックは新規登録の場合のみ
                if (isCreate) {
                    // 過去日付ならエラーをアド
                    if (!Utils.isTodayOrFurtureDate(deadline)) {
                        FieldError fieldError = new FieldError(
                                result.getObjectName(),
                                "deadline",
                                messageSource.getMessage("Past.todoData.deadline",null, locale));
                        result.addError(fieldError);
                        ans = false;
                    }
                }
            }
        }
        List<TaskData> taskList = todoData.getTaskList();
        if(taskList != null){
            for (int i = 0; i < taskList.size(); i++) {
                TaskData taskData = taskList.get(i);
                if(!Utils.isBlank(taskData.getTitle())){
                    if(Utils.isAllDoubleSpace(taskData.getTitle())){
                        FieldError fieldError = new FieldError(result.getObjectName()
                        , "taskList["+ i +"].title", messageSource.getMessage("DoubleSpace.todoData.title", null, locale));
                        result.addError(fieldError);
                    }
                }
            String taskDeadline = taskData.getDeadline();
            if(!taskDeadline.equals("") && !Utils.isValidDateFormat(taskDeadline)){
                FieldError fieldError = new FieldError(result.getObjectName()
                , "taskList[" + i + "].deadline", messageSource.getMessage("InvalidFormat.todoData.deadline", null, locale));
                result.addError(fieldError);
            }
            }
        }
       

        return ans;
    }
    // --------------------------------------------------------------------------------
    // Taskのチェック
    // --------------------------------------------------------------------------------
    public boolean isValid(TaskData taskData,BindingResult result,Locale locale) {
        boolean ans = true;

        //タスクの件名が半角スペースだけ or ""ならエラー
        if(Utils.isBlank(taskData.getTitle())){
            FieldError fieldError = new FieldError(result.getObjectName(),
            "newTask.title" , messageSource.getMessage("NotBlank.taskData.title",
            null, locale));
            result.addError(fieldError);
            ans = false;
        }else{
            //タスクの件名が全半角スペースだけならエラー
            if(Utils.isAllDoubleSpace(taskData.getTitle())){
            FieldError fieldError = new FieldError(result.getObjectName(),
            "newTask.title" , messageSource.getMessage("DoubleSpace.todoData.title",
            null, locale));
            result.addError(fieldError);
            ans = false;
            }
        }
        
        //期限が""ならチェックしない
        if(taskData.getDeadline().equals("")){
            return ans;
        }
        //期限の形式チェック
        if(!Utils.isValidDateFormat(taskData.getDeadline())){
            FieldError fieldError = new FieldError(result.getObjectName(),
            "newTask.deadline" , messageSource.getMessage("InvalidFormat.todoData.deadline",
            null, locale));
            result.addError(fieldError);
            ans = false;
        }else{
            if(!Utils.isTodayOrFurtureDate(taskData.getDeadline())){
            FieldError fieldError = new FieldError(result.getObjectName(),
            "newTask.deadline" , messageSource.getMessage("Past.todoData.deadline",
            null, locale));
            result.addError(fieldError);
            ans = false;
            }
        }
        return ans;
    }
    // --------------------------------------------------------------------------------
    // 検索条件のチェック
    // --------------------------------------------------------------------------------
    public boolean isValid(TodoQuery todoQuery, BindingResult result,Locale locale) {
        boolean ans = true;

        // 期限:開始をformatできないならエラーをアド
        String date = todoQuery.getDeadlineFrom();
        if (!date.equals("") && !Utils.isValidDateFormat(date)) {
            FieldError fieldError = new FieldError(
                result.getObjectName(),
                "deadlineFrom",
                messageSource.getMessage("InvalidFormat.todoQuery.deadlineFrom",null, locale));
            result.addError(fieldError);
            ans = false;
        }

        // 期限:終了をformatできないならエラーをアド
        date = todoQuery.getDeadlineTo();
        if (!date.equals("") && !Utils.isValidDateFormat(date)) {
            FieldError fieldError = new FieldError(
                result.getObjectName(),
                "deadlineTo",
                messageSource.getMessage("InvalidFormat.todoQuery.deadlineTo",null, locale));
            result.addError(fieldError);
            ans = false;
        }
        return ans;
    }
}