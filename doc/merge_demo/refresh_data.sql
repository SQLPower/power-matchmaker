-- This script refreshes the data in the customer, address, and invoice tables.

set search_path to merge_demo;

delete from invoice_line;
delete from invoice;
delete from customer_address;
delete from customer;

insert into customer (id, name) values (1, 'Arthur Dent');
insert into customer (id, name) values (2, 'Arthur Dent');
insert into customer (id, name) values (3, 'Ford Prefect');
insert into customer (id, name) values (4, 'Zaphod Beeblebrox');
insert into customer (id, name) values (5, 'Marvin');

insert into customer_address
(id, customer_id, address_type,
email, phone,
street_addr, city,
state, country, pcode)
values
(1, 1, 'HOME',
'arthur@home.co.uk', '+44-454-3552345',
'1 Arthur Place', 'English countryside',
null, 'England', 'N554355');

insert into customer_address
(id, customer_id, address_type,
email, phone,
street_addr, city,
state, country, pcode)
values
(2, 1, 'SPACE',
null, null,
null, 'Vogon Destructor Ship',
null, null, null);


-- ********** Invoice 1000
insert into invoice (id, customer_id, date)
values (1000, 1, '2005-05-05');

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1000, 1, 'Towel', 1, 10.34);

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1000, 2, 'Babel Fish', 1, 1044.34);


-- ********** Invoice 1001
insert into invoice (id, customer_id, date)
values (1001, 1, '2005-06-05');

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1001, 1, 'Sub-Etha Sens-o-Matic', 1, 130.34);

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1001, 2, 'Cup of tea', 10, 1.00);


-- ********** Invoice 1002
insert into invoice (id, customer_id, date)
values (1002, 2, '2005-07-05');

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1002, 1, 'Tea', 1, 1.25);

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1002, 2, 'Biscuits', 1, 3.00);

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1002, 3, 'Guardian Newspaper', 1, .75);

insert into invoice_line (invoice_id, line_number, item, quantity, item_price)
values (1002, 4, 'Chewing Gum', 1, 2.34);

