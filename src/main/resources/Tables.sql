--h2 is typically used to setup a test database, not a prod database.
--first, drop your tables (to reset your database for testing)
--then create your tables

drop table if exists transactions;
drop table if exists account;
drop table if exists bank_user;

create table bank_user (
    user_id int primary key auto_increment,
    username varchar(255) unique,
    password varchar(255)
);

create table account (
    account_id int primary key auto_increment,
    balance decimal,
    account_user int,
    foreign key (account_user) references bank_user(user_id)
);

create table transactions (
    transaction_id int primary key auto_increment,
    transaction_type varchar(255),
    amount decimal,
    transaction_time timestamp,
    account_user int,
    foreign key (account_user) references bank_user(user_id),
    account_id int,
    foreign key (account_id) references account (account_id)
);
