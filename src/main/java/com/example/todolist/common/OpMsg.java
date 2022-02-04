package com.example.todolist.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpMsg {
    private String msgType;// "I":Information, "W":Warning, "E":Error
    private String msgText;// メッセージ
}