use micronaut;

create table if not exists products (
    id bigint not null auto_increment,
    code  varchar(255)  not null,
    name varchar(255) not null,
    price  DECIMAL(5,2) not null,
    primary key (id)
);

insert into products (code, name, price) values ('P100', 'Product 100', 99.99);
insert into products (code, name, price) values ('P200', 'Product 200', 39.99);
insert into products (code, name, price) values ('P300', 'Product 300', 29.99);
insert into products (code, name, price) values ('P400', 'Product 400', 1.99);
insert into products (code, name, price) values ('P500', 'Product 500', 9.99);
insert into products (code, name, price) values ('P600', 'Product 600', 89.99);
insert into products (code, name, price) values ('P700', 'Product 700', 79.99);
