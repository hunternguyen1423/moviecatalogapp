drop table if exists employees;
create table employees (
    email VARCHAR(50) PRIMARY KEY,
	password VARCHAR(128) NOT NULL,
	fullname VARCHAR(100)
);