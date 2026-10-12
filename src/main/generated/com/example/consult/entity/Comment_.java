package com.example.consult.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Comment.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Comment_ {

	public static final String PARENT = "parent";
	public static final String UPDATE_DATE = "updateDate";
	public static final String CHILDREN = "children";
	public static final String MEMBER = "member";
	public static final String ID = "id";
	public static final String BOARD = "board";
	public static final String CONTENT = "content";
	public static final String CREATE_DATE = "createDate";

	
	/**
	 * @see com.example.consult.entity.Comment#parent
	 **/
	public static volatile SingularAttribute<Comment, Comment> parent;
	
	/**
	 * @see com.example.consult.entity.Comment#updateDate
	 **/
	public static volatile SingularAttribute<Comment, LocalDateTime> updateDate;
	
	/**
	 * @see com.example.consult.entity.Comment#children
	 **/
	public static volatile ListAttribute<Comment, Comment> children;
	
	/**
	 * @see com.example.consult.entity.Comment#member
	 **/
	public static volatile SingularAttribute<Comment, Member> member;
	
	/**
	 * @see com.example.consult.entity.Comment#id
	 **/
	public static volatile SingularAttribute<Comment, Integer> id;
	
	/**
	 * @see com.example.consult.entity.Comment
	 **/
	public static volatile EntityType<Comment> class_;
	
	/**
	 * @see com.example.consult.entity.Comment#board
	 **/
	public static volatile SingularAttribute<Comment, Board> board;
	
	/**
	 * @see com.example.consult.entity.Comment#content
	 **/
	public static volatile SingularAttribute<Comment, String> content;
	
	/**
	 * @see com.example.consult.entity.Comment#createDate
	 **/
	public static volatile SingularAttribute<Comment, LocalDateTime> createDate;

}

