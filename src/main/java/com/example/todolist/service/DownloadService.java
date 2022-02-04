package com.example.todolist.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.AttachedFile;
import com.example.todolist.repository.AttachedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownloadService {
    private final AttachedFileRepository attachedFileRepository;

    @Value("${attached.file.path}")//D:/traning/spring_boot/KikutaHideaki/Jissen/temp/uploadFiles
    private String ATTACHED_FILE_PATH;

    public ResponseEntity<Resource> downloadAttachedFile(int afId) throws IOException {
        //該当のファイルテーブルのEntityを取得
        AttachedFile af = attachedFileRepository.findById(afId).get();
        //フルパスを取得
        String fileName = Utils.makeAttachedFilePath(ATTACHED_FILE_PATH, af);
        //フルパスをPath型に変換
        Path path = Path.of(fileName);
        //ResourceにPath型のフルパスを渡す
        Resource resource = new PathResource(path);
        //Entityからファイル名を渡し、拡張子判別後、MediaTypeを取得
        MediaType contentType = Utils.ext2contentType(af.getFileName());

        MediaType type = MediaType.parseMediaType(Files.probeContentType(path));
        System.out.println(type);
        
        //ResponseEntity<Resource> responseEntity;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setContentLength(Files.size(path));

System.out.println(contentType);

        if (contentType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
            headers.setContentDisposition(ContentDisposition.attachment().filename(af.getFileName(), StandardCharsets.UTF_8).build());
            //responseEntity = ResponseEntity.ok().headers(headers).body(resource);
           /*  responseEntity = ResponseEntity.ok().contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                            URLEncoder.encode(af.getFileName(), "UTF-8") + "\"")
                    .body(resource); */
        } else {
            headers.setContentDisposition(ContentDisposition.inline().filename(af.getFileName()).build());
            //responseEntity = ResponseEntity.ok().headers(headers).body(resource);
            /* responseEntity = ResponseEntity.ok().contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource); */
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }
   
}