package com.example.consult.entity;

import com.example.consult.Role;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Member.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Member_ {

	public static final String PASSWORD = "password";
	public static final String REG_TIME = "regTime";
	public static final String ROLE = "role";
	public static final String PHONE = "phone";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String EMAIL = "email";

	
	/**
	 * @see com.example.consult.entity.Member#password
	 **/
	public static volatile SingularAttribute<Member, String> password;
	
	/**
	 * @see com.example.consult.entity.Member#regTime
	 **/
	public static volatile SingularAttribute<Member, LocalDateTime> regTime;
	
	/**
	 * @see com.example.consult.entity.Member#role
	 **/
	public static volatile SingularAttribute<Member, Role> role;
	
	/**
	 * @see com.example.consult.entity.Member#phone
	 **/
	public static volatile SingularAttribute<Member, String> phone;
	
	/**
	 * @see com.example.consult.entity.Member#name
	 **/
	public static volatile SingularAttribute<Member, String> name;
	
	/**
	 * @see com.example.consult.entity.Member#id
	 **/
	public static volatile SingularAttribute<Member, Long> id;
	
	/**
	 * @see com.example.consult.entity.Member
	 **/
	public static volatile EntityType<Member> class_;
	
	/**
	 * @see com.example.consult.entity.Member#email
	 **/
	public static volatile SingularAttribute<Member, String> email;

}

