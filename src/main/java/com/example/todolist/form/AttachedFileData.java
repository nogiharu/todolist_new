package com.example.todolist.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachedFileData {
    // id
    private Integer id;
    // アップロードファイル名
    private String fileName;
    // メモ
    private String note;
    // 新しいタブで開くかどうか
    private boolean openInNewTab;
}