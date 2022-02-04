package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;


import javax.persistence.EntityManager;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.entity.Todo_;
import com.example.todolist.form.TodoQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
    private final EntityManager entityManager;

    @Override
    public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {
        // 検索条件を管理するビルダーをentityManager経由で取得
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        // ビルダーからクエリを生成。
        CriteriaQuery<Todo> query = builder.createQuery(Todo.class);
        // クエリの起点を指定
        Root<Todo> root = query.from(Todo.class);
        // 後続判定リストを生成
        List<Predicate> predicates = new ArrayList<>();
        // 件名
        String title = "";
        if (todoQuery.getTitle().length()>0) {
           title = "%" + todoQuery.getTitle() + "%";
        } else {
            // titleが空文字なら％を付けて、後続のandから始められるようにする
            title = "%";
        }
        predicates.add(builder.like(root.get(Todo_.TITLE),title));
        // 重要度
        if (todoQuery.getImportance() != -1) {
            predicates.add(builder.and(builder.equal(root.get(Todo_.IMPORTANCE), todoQuery.getImportance())));
        }
        // 緊急度
        if (todoQuery.getUrgency() != -1) {
            predicates.add(builder.and(builder.equal(root.get(Todo_.URGENCY), todoQuery.getUrgency())));
        }
        // 期限：開始～
        if (!todoQuery.getDeadlineFrom().equals("")) {
            predicates.add(builder.and(builder.greaterThanOrEqualTo(root.get(Todo_.DEADLINE),
                    Utils.str2date(todoQuery.getDeadlineFrom()))));
        }
        // 期限：～終了
        if (!todoQuery.getDeadlineTo().equals("")) {
            predicates.add(builder.and(
                    builder.lessThanOrEqualTo(root.get(Todo_.DEADLINE), Utils.str2date(todoQuery.getDeadlineTo()))));
        }
        // 完了
        if (todoQuery.getDone()!= null && todoQuery.getDone().equals("Y")) {
            predicates.add(builder.and(builder.equal(root.get(Todo_.DONE), todoQuery.getDone())));
        }
        // リストを配列に変換
        Predicate[] preArray = new Predicate[predicates.size()];
        predicates.toArray(preArray);
        
        // クエリをセット
        query = query.select(root).where(preArray).orderBy(builder.asc(root.get(Todo_.ID)));
        // entityManagerでクエリをTypedQueryに渡しクエリ生成
        TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
        // 検索された該当件数を取得
        int totalRows = typedQuery.getResultList().size();
       
        // 検索件数に応じた初期ページ番号をセット
        // 件数５件で１ページのため、６件めのデータなら１ページ（０ページから数える）からスタート
        // ページ番号×データ件数で初期ページ番号を割り出す
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        //ページログ取得
       // TodoLog.queryPageLog(pageable.getPageNumber(),pageable.getPageSize(),totalRows);
        // １ページ当たりの件数をセット
        typedQuery.setMaxResults(pageable.getPageSize());

        // PageImplコンストラクタにデータ、ページ番号、データ件数を渡す
        Page<Todo> page = new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
        //データログ取得
      //  TodoLog.queryDataLog(typedQuery.getResultList());
        return page;
    }
}