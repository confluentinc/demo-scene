create table test1 (
	id int not null ,
	col1 varchar(50),
	create_ts timestamp default current_timestamp
);

insert into test1 (id, col1) values (1,'a');
insert into test1 (id, col1) values (2,'b');


create table test2 (
	id int not null ,
	col1 varchar(50),
	create_ts timestamp default current_timestamp
);

insert into test2 (id, col1) values (3,'x');
insert into test2 (id, col1) values (4,'y');
insert into test2 (id, col1) values (5,'z');