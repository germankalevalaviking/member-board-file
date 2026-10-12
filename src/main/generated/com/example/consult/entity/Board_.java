package com.example.consult.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Board.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Board_ {

	public static final String COMMENT_LIST = "commentList";
	public static final String ORIGINAL_FILE_NAME = "originalFileName";
	public static final String REG_TIME = "regTime";
	public static final String MEMBER = "member";
	public static final String STORED_FILE_NAME = "storedFileName";
	public static final String ID = "id";
	public static final String VIEW_COUNT = "viewCount";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";

	
	/**
	 * @see com.example.consult.entity.Board#commentList
	 **/
	public static volatile ListAttribute<Board, Comment> commentList;
	
	/**
	 * @see com.example.consult.entity.Board#originalFileName
	 **/
	public static volatile SingularAttribute<Board, String> originalFileName;
	
	/**
	 * @see com.example.consult.entity.Board#regTime
	 **/
	public static volatile SingularAttribute<Board, LocalDateTime> regTime;
	
	/**
	 * @see com.example.consult.entity.Board#member
	 **/
	public static volatile SingularAttribute<Board, Member> member;
	
	/**
	 * @see com.example.consult.entity.Board#storedFileName
	 **/
	public static volatile SingularAttribute<Board, String> storedFileName;
	
	/**
	 * @see com.example.consult.entity.Board#id
	 **/
	public static volatile SingularAttribute<Board, Long> id;
	
	/**
	 * @see com.example.consult.entity.Board#viewCount
	 **/
	public static volatile SingularAttribute<Board, Integer> viewCount;
	
	/**
	 * @see com.example.consult.entity.Board#title
	 **/
	public static volatile SingularAttribute<Board, String> title;
	
	/**
	 * @see com.example.consult.entity.Board
	 **/
	public static volatile EntityType<Board> class_;
	
	/**
	 * @see com.example.consult.entity.Board#content
	 **/
	public static volatile SingularAttribute<Board, String> content;

}

