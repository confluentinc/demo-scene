use micronaut;

create table if not exists order_item (
    order_id bigint not null,
    product_id varchar(255) not null,
    description varchar(255) not null,
    quantity int not null,
    unit_price double not null,
    primary key (order_id, product_id)
);
