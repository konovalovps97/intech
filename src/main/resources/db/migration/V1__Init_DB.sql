create sequence hibernate_sequence start 2 increment 1;

--CREATE DATABASE intech;

--create user intech_user with password 'intech_user';

--grant ALL on DATABASE intech to intech_user;


create table if not exists user_role (
    user_id int8 not null,
    roles varchar(255)
);

create table if not exists users (
    id int8 not null,
    login varchar(255) not null,
    username varchar(255),
    surname varchar(255),
    password varchar(255) not null,
    primary key (id)
);


create table if not exists topics (
    id varchar(255) not null,
    displayname varchar(255) not null,
    description varchar(512)  ,
    author varchar(255) not null,
    logo varchar(255) ,
    primary key (id)
);


create table if not exists messages (
    id varchar(255) not null,
    topic_id varchar(255) not null,
    content varchar(255) not null,
    sender varchar(255) not null,
    timestamp bigint not null,
    type varchar(255) not null,
    primary key (id)
);

alter table if exists user_role
    add constraint user_role_user_fk
    foreign key (user_id) references users;

