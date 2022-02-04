package com.example.todolist.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.web.servlet.view.document.AbstractPdfView;

public class TodoPdf extends AbstractPdfView{
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document doc, PdfWriter writer,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        //日本語
        String japanese = "HeiseiMin-W3";
        //横書き
        String horizontal = "UniJIS-UCS2-H";
        // 明朝１６ｐｔ太字
        Font font_m16_bold = new Font(BaseFont.createFont(japanese,horizontal,BaseFont.NOT_EMBEDDED),
        16,Font.BOLD);
        // 明朝１２ｐｔ
        Font font_m12 = new Font(BaseFont.createFont(japanese,horizontal,BaseFont.NOT_EMBEDDED),12);
        // 明朝１０ｐｔ
        Font font_m10 = new Font(BaseFont.createFont(japanese,horizontal,BaseFont.NOT_EMBEDDED),10);
        // 明朝１２ｐｔ下線あり
        Font font_m12_underline = new Font(BaseFont.createFont(japanese,horizontal,BaseFont.NOT_EMBEDDED),
        12,Font.UNDERLINE);
        // 明朝１０ｐｔ赤
        Font font_m10_red = new Font(BaseFont.createFont(japanese,horizontal,BaseFont.NOT_EMBEDDED),
        10);
        font_m10_red.setColor(new Color(255,0,0));

        //
        doc.add(new Paragraph("ToDo List",font_m16_bold));
        doc.add(new Paragraph(" "));

        // todo取得
        int numTodo = 0;
        int numDoneTodo = 0;
        int numTask = 0;
        int numDoneTask = 0;

        // todo,task明細出力。ついでにカウント
        @SuppressWarnings("unchecked")
        List<Todo> todoList = (List<Todo>)model.get("todoList");
        for (Todo todo : todoList) {
            ++numTodo;
            if(todo.getDone().equals("Y")){
                ++numDoneTodo;
            }
            doc.add(todoParagraph(todo,font_m12));
            for (Task task: todo.getTaskList()) {
                ++numTask;
                if(task.getDone().equals("Y")){
                    ++numDoneTask;
                }
                doc.add(taskParagraph(task,font_m12));
            }
        }
        // todo完了率
        int todoDoneRate;
        if(numDoneTodo > 0){
            todoDoneRate = (int)((float)numDoneTodo / (float)numTodo * 100);
        }else{
            todoDoneRate = -1;
        }
        // task完了率
        int taskDoneRate;
        if(numDoneTask > 0){
            taskDoneRate = (int)((float)numDoneTask / (float)numTask * 100);
        }else{
            taskDoneRate = -1;
        }
        //テーブル
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" 集計表 ",font_m12_underline));
        Table table = new Table(8);

        table.setWidth(100);
        table.setPadding(5);

        // 見出し行
        // Todo数, 完了, 未完了, 完了率, Task数, 完了, 未完了, 完了率
        HorizontalAlignment center = HorizontalAlignment.CENTER;
        String[] headers = {"Todo数", "完了","未完了","完了率", "Task数","完了","未完了","完了率"};
        for (String header : headers) {
            table.addCell(makeCell(header, font_m10, 0.9f, center));
        }

        // 集計結果行
        // Todo数, 完了, 未完了
        List<Cell> cellList = new ArrayList<>();
        cellList.add(makeCell("" + numTodo, font_m10, 1.0f, center));
        cellList.add(makeCell("" + numDoneTodo, font_m10, 1.0f, center));
        cellList.add(makeCell("" + (numTodo - numDoneTodo), font_m10, 1.0f, center));
        // todo完了率
        if(todoDoneRate < 0){
            cellList.add(makeCell("-", font_m10_red, 1.0f, center));
        }else{
            cellList.add(makeCell(todoDoneRate + "%", font_m10_red, 1.0f, center));
        }
        // Task数, 完了, 未完了
        cellList.add(makeCell("" + numTask, font_m10, 1.0f, center));
        cellList.add(makeCell("" + numDoneTask, font_m10, 1.0f, center));
        cellList.add(makeCell("" + (numTask - numDoneTask), font_m10, 1.0f, center));
        // Task完了率
        if(taskDoneRate < 0){
            cellList.add(makeCell("-", font_m10_red, 1.0f, center));
        }else{
            cellList.add(makeCell(taskDoneRate + "%", font_m10_red, 1.0f, center));
        }
        for (Cell cell : cellList) {
            table.addCell(cell);
        }
        doc.add(table);
    }
    // Cellヘルパーメソッド
    private Cell makeCell(String content, Font font, float grayFill,HorizontalAlignment center) {
        Cell cell = new Cell(new Paragraph(content,font));
        cell.setHorizontalAlignment(center);
        cell.setGrayFill(grayFill);
        return cell;
    }
    // TodoをPDFに編集
    private Paragraph todoParagraph(Todo todo,Font font) {
        StringBuilder sb = new StringBuilder();
        // 完了/未完了
        if(todo.getDone().equals("Y")){
            sb.append("■");
        }else{
            sb.append("□");
        }
        // 件名
        sb.append(" " + todo.getTitle() + " ( ");
        // 重要度
        sb.append("重要度： " + (todo.getImportance() == 1 ? "★★★" : "★") + " / ");
        // 期限
        sb.append("緊急度： " + (todo.getUrgency() == 1 ? "★★★" : "★") + " / ");
        // 期限
        sb.append("期限： " + (todo.getDeadline() == null ? "-" : todo.getDeadline()) + " ) ");
        return new Paragraph(sb.toString(),font);
    }
     // TaskをPDF出力用に編集して返す
    private Paragraph taskParagraph(Task task,Font font) {
        StringBuilder sb = new StringBuilder();
        //　完了/未完了
        if(task.getDone().equals("Y")){
            sb.append("■");
        }else{
            sb.append("□");
        }
        // 件名
        sb.append(" " + task.getTitle());
        // 期限
        sb.append(" ( ");
        sb.append(" 期限： " + (task.getDeadline() == null ? "-" : task.getDeadline()));
        sb.append(" ) ");

        return new Paragraph("\t" + sb.toString(),font);
    }
}