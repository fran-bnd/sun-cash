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
    balance bigint,
    account_user int,
    foreign key (account_user) references bank_user(user_id)
);

create table transactions (
    transactionID int primary key auto_increment,
    transaction_type int,
    amount bigint,
    time timestamp default CURRENT_TIMESTAMP,
    accountID_from int,
    foreign key (accountID_from) references account(account_id),
    accountID_to int,
    foreign key (accountID_to) references account(account_id)
);
