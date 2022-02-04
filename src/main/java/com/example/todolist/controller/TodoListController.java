package com.example.todolist.controller;

import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import com.example.todolist.common.OpMsg;
import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.AttachedFile;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.AttachedFileRepository;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;
//import com.example.todolist.view.SamplePdf;
import com.example.todolist.view.TodoPdf;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class TodoListController {
    private final TodoRepository todoRepository;
    private final TaskRepository taskRepository;
    private final AttachedFileRepository attachedFileRepository;
    private final TodoService todoService;
    private final HttpSession session;
    private final MessageSource messageSource;

    @PersistenceContext
    private EntityManager entityManager;
    TodoDaoImpl todoDaoImpl;

    @PostConstruct
    public void init() {
        todoDaoImpl = new TodoDaoImpl(entityManager);
    }

    // 【処理SELECT】ToDoを全検索してページ情報とデータを一覧画面(todolist.html)に表示
    @GetMapping("/todo")
    public String showTodoList(@PageableDefault(page = 0, size = 5) Pageable pageable,
     TodoQuery todoQuery, Model model) {
        todoQuery = (TodoQuery) session.getAttribute("todoQuery");
        if (todoQuery == null) {
            todoQuery = new TodoQuery();
            session.setAttribute("todoQuery", todoQuery);
        }
        Pageable prevPageable = (Pageable) session.getAttribute("prevPageable");
        if (prevPageable == null) {
            prevPageable = pageable;
            session.setAttribute("prevPageable", prevPageable);
        }

        Page<Todo> todoList = todoDaoImpl.findByCriteria(todoQuery, prevPageable);
        model.addAttribute("todoList", todoList);

        List<Todo> todo = todoRepository.findAll();
        List<Task> taskList;
        for (Todo t : todo) {
            System.out.println(t);
            taskList = t.getTaskList();
            if(taskList.size()==0){
                System.out.println("\tTask not found.");
            }else{
                for (Task task : taskList) {
                    System.out.println("\t" + task);
                }
            }
        }
        return "todoList";
    }

    // 【処理UPDATE】todoList.htmlで[件名]リンクがクリックされたとき
    @GetMapping("/todo/{id}")
    public String todoById(@PathVariable int id, Model model) {
        Todo todo = todoRepository.findById(id).get();
        List<AttachedFile> attachedFiles = attachedFileRepository.findByTodoIdOrderById(id);
        model.addAttribute("todoData", new TodoData(todo,attachedFiles));
        session.setAttribute("mode", "update");
        return "todoForm";
    }

    // 【処理UPDATE】ToDo入力画面(todoForm.html)で[更新]をクリックされたとき
    @PostMapping("/todo/update")
    public String updateTodo(@Validated TodoData todoData, BindingResult result, Model model,
    RedirectAttributes redirectAttributes, Locale locale) {
        System.out.println(todoData.getTaskList());
        boolean isValid = todoService.isValid(todoData, result, false, locale);
        if (!result.hasErrors() && isValid) {
            Todo todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            String msg = messageSource.getMessage("msg.i.todo_updated", null, locale);
            redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
            
            return "redirect:/todo/" + todo.getId();
        } else {
            String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
            model.addAttribute("msg", new OpMsg("E", msg));
            return "todoForm";
        }
    }

    // 【処理CREATE】todoList.htmlで[新規追加]リンクがクリックされたとき
    @PostMapping("/todo/create/form")
    public String createTodo(TodoData todoData) {
        session.setAttribute("mode", "create");
        return "todoForm";
    }

    // 【処理CREATE→SELECT】ToDo入力画面(todoForm.html)で[登録]をクリックされたとき
    @PostMapping("/todo/create/do")
    public String createTodo(@Validated TodoData todoData, BindingResult result,Model model,
    RedirectAttributes redirectAttributes,Locale locale) {
       // System.out.println(todoData);
        boolean isValid = todoService.isValid(todoData, result, true, locale);
        if (!result.hasErrors() && isValid) {
            Todo todo = todoData.toEntity();
            todoRepository.saveAndFlush(todo);
            String msg = messageSource.getMessage("msg.i.todo_created", null, locale);
            redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
            return "redirect:/todo/" + todo.getId();
        } else {
            String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
            model.addAttribute("msg", new OpMsg("E", msg));
            return "todoForm";
        }
    }

    // 【処理キャンセル】ToDo入力画面(todoForm.html)で[キャンセル]がクリックされたとき
    @PostMapping("/todo/cancel")
    public String cancel() {
        return "redirect:/todo";
    }

    // 【ToDo DELETE】
    @PostMapping("/todo/delete")
    public String deleteTodo(TodoData todoData, RedirectAttributes redirectAttributes,Locale locale) {
        
        //添付ファイルの削除
        todoService.deleteAttachedFiles(todoData.getId());
        todoRepository.deleteById(todoData.getId());
        //attached_fileテーブルから削除
        List<AttachedFile> af = attachedFileRepository.findByTodoIdOrderById(todoData.getId());
        attachedFileRepository.deleteAllInBatch(af);
        String msg = messageSource.getMessage("msg.i.todo_deleted", null, locale);
        redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
        return "redirect:/todo";
    }


    // 【処理QUERY】todoList.htmlで[検索]がクリックされたとき
    @PostMapping("/todo/query")
    public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, TodoQuery todoQuery,
            BindingResult result, Model model, Locale locale) {
        if (todoService.isValid(todoQuery, result, locale)) {
            Page<Todo> todoList = todoDaoImpl.findByCriteria(todoQuery, pageable);
            model.addAttribute("todoList", todoList);
            session.setAttribute("todoQuery", todoQuery);
            if (todoList.getContent().size() == 0) {
                String msg = messageSource.getMessage("msg.w.todo_not_found", null, locale);
                model.addAttribute("msg", new OpMsg("W", msg));
            }
        }else{
            String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
                model.addAttribute("msg", new OpMsg("E", msg));
        }
        return "todoList";
    }

    // 【処理QUERY】todoList.htmlで[検索]がクリックされ、ページリンクがクリックされたとき
    @GetMapping("/todo/query")
    public String queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, TodoQuery todoQuery, Model model) {
        todoQuery = (TodoQuery) session.getAttribute("todoQuery");
        Page<Todo> todoList = todoDaoImpl.findByCriteria(todoQuery, pageable);
        session.setAttribute("prevPageable", pageable);
        // System.out.println("[GET/todo/query]"+pageable.getPageNumber());
        model.addAttribute("todoList", todoList);
        return "todoList";
    }

    //【TASK DELETE】
    @GetMapping("/task/delete")
    public String deleteTask(@RequestParam(name = "task_id")int taskId,
    @RequestParam(name = "todo_id") int todoId,RedirectAttributes redirectAttributes,Locale locale) {
        taskRepository.deleteById(taskId);
        String msg = messageSource.getMessage("msg.i.task_deleted", null, locale);
        redirectAttributes.addFlashAttribute("msg",new OpMsg("I", msg));
        return "redirect:/todo/" + todoId;
    }

    //【TASK INSERT】
    @PostMapping("/task/create")
    public String createTask(TodoData todoData,BindingResult result,Model model,
    RedirectAttributes redirectAttributes,Locale locale) {
        boolean isValid = todoService.isValid(todoData.getNewTask(), result, locale);
        if(isValid){
            Todo todo = todoData.toEntity();
            Task task = todoData.toTaskEntity();
            task.setTodo(todo);
            taskRepository.saveAndFlush(task);
            String msg = messageSource.getMessage("msg.i.task_created", null, locale);
            redirectAttributes.addFlashAttribute("msg",new OpMsg("I", msg));
            return "redirect:/todo/" + todo.getId();
        }else{
            String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
            model.addAttribute("msg", new OpMsg("E", msg));
            return "todoForm";
        }
    }

    //【File Upload】
    @PostMapping("/todo/af/upload")
    public String uploadAttachedFile(@RequestParam(name = "todo_id") int todoId,@RequestParam String note,
    @RequestParam("file_contents")MultipartFile fileContents,RedirectAttributes redirectAttributes,Model model, Locale locale) {
        if(fileContents.isEmpty()){
            String msg = messageSource.getMessage("msg.w.attachedfile_empty", null, locale);
            model.addAttribute("msg", new OpMsg("W", msg));
            return "todoForm";
        }else{
            todoService.saveAttachedFile(todoId,note,fileContents);
            String msg = messageSource.getMessage("msg.i.attachedfile_uploaded", null, locale);
            redirectAttributes.addFlashAttribute("msg",new OpMsg("I", msg));
        }
        return "redirect:/todo/" + todoId;
    }
    //添付ファイルのを削除
    @GetMapping("/todo/af/delete")
    public String deleteAttachedFile(@RequestParam(name = "af_id")int afId,@RequestParam(name = "todo_id")int todoId,
    RedirectAttributes redirectAttributes,Locale locale) {
        //添付ファイルを削除
        todoService.deleteAttachedFile(afId);
        //attached_fileテーブルから削除
        attachedFileRepository.deleteById(afId);
        //メッセージ
        String msg = messageSource.getMessage("msg.i.attachedfile_deleted", null, locale);
        redirectAttributes.addFlashAttribute("msg",new OpMsg("I", msg));
        return "redirect:/todo/" + todoId;
    }
    /* //PDF
    @GetMapping("/todo/pdf")
    public SamplePdf todoPdf(SamplePdf pdf) {
        pdf.addStaticAttribute("ldt", LocalDateTime.now());
        return pdf;
    } */
    //PDF
    @GetMapping("/todo/pdf")
    public TodoPdf todoPdf(TodoPdf pdf) {
        List<Todo> todoList = todoRepository.findAllByOrderById();
        pdf.addStaticAttribute("todoList", todoList);
        return pdf;
    }
}
