-- --liquibase formatted sql

--changeset artem:1
create table users
(
    id                bigserial primary key,
    username          text    not null unique,
    email             text    not null unique,
    login_token             text ,
    main_token             text ,
--     password          text,
    image             text    not null,
    role              text    not null default 'USER'
);
