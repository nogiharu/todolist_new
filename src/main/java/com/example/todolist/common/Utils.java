package com.example.todolist.common;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;

import com.example.todolist.entity.AttachedFile;

import org.springframework.http.MediaType;

public class Utils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 文字列をyyyy-MM-dd型の日付としてjava.sql.Date型へ変換する。<br>
     * 
     * @param s 変換対象
     * @return 変換結果(引数がnull, "", yyyy-MM-dd形式でなければnullを返す)
     */

    public static Date str2date(String s) {
        long ms = 0;
        try {
            ms = sdf.parse(s).getTime();
        } catch (ParseException e) {
            return null;
        }
        return new Date(ms);
    }

    /**
     * java.sql.Date型を文字列へ変換する。<br>
     * @param date 変換対象
     * @return 文字列
     */
    public static String date2str(Date date) {
        String s = "";
        if(date != null){
            s = sdf.format(date);
        }
        return s;
    }

    /**
     * 引数が"" or 半角SPACE/TABだけで構成されているならtrueを返す
     * 
     * @param s チェック対象
     * @return true:半角SPACE/TABのみ or "", null, false:左記以外
     */
    public static boolean isBlank(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '\t') {// 半角SPACE
                return false;
            }
        }
        return true;
    }

    /**
     * 引数が全角SPACEだけで構成されていればtrueを返す
     * 
     * @param s チェック対象
     * @return true:全角SPACEのみ, "", null false:左記以外
     */
    public static boolean isAllDoubleSpace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '　') { // 全角SPACE
                return false;
            }
        }
        return true;
    }

    /**
     * 引数がyyyy-mm-ddと解釈できればtrueを返す
     * 
     * @param s チェック対象
     * @return true:yyyy-mm-ddと解釈できる、false:解釈できない or 引数がnull or ""
     */
    public static boolean isValidDateFormat(String s) {
        try {
            // parseできればyyyy-MM-dd形式とみなし、trueを返す
            LocalDate.parse(s);
            return true;

        } catch (DateTimeException e) {
            // yyyy-mm-dd形式以外
            return false;
        }
    }

    /**
     * 引数が今日以降の日付を表す文字列(yyyy-MM-dd形式)ならtrueを返す
     * 
     * @param str チェック対象
     * @return true:今日以降、false:昨日以前、引数がyyyy-MM-dd形式でない or null or ""
     */

    public static boolean isTodayOrFurtureDate(String s) {
        LocalDate today = LocalDate.now();
        LocalDate deadlineDate = null;
        try {
            // "yyyy-mm-dd" -> LocalDate
            deadlineDate = LocalDate.parse(s);
            if (deadlineDate.isBefore(today)) {
                // 過去日付なのでfalse
                return false;

            } else {
                return true;
            }

        } catch (DateTimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    //添付ファイルの格納ファイル名を作成する
    public static String makeAttachedFilePath(String path,AttachedFile af) {
        return path + "/" + af.getCreateTime() + "_" + af.getFileName();
    }

    public static MediaType ext2contentType(String fileName) {
        String fext = fileName.substring(fileName.lastIndexOf(".")+1);
        MediaType type;
        
        switch(fext){
            // GIF
            case "gif":
            type = MediaType.IMAGE_GIF;
            break;
            // JPEG
            case "jpg":
            case "jpeg":
            type = MediaType.IMAGE_JPEG;
            break;
            // PNG
            case "png":
            type = MediaType.IMAGE_PNG;
            break;
            // PDF
            case "pdf":
            type = MediaType.APPLICATION_PDF;
            break;
            // 上記以外
            default:
            type = MediaType.APPLICATION_OCTET_STREAM;
            break;
        }
        return type;
    }
}