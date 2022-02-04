package com.example.todolist.controller;

import org.springframework.core.io.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;




import com.example.todolist.service.DownloadService;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class DownloadController {
    private final DownloadService downloadService;

    @Value("${attached.file.path}")
    private String ATTACHED_FILE_PATH;


    //練習
    @GetMapping("/todo/af/download/{afId}")
    public ResponseEntity<Resource> downloadAttachedFile2(@PathVariable int afId)throws Exception {
        return downloadService.downloadAttachedFile(afId);
        }
        
    }

