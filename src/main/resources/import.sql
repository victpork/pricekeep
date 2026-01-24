-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
insert into store_group (id, name, iconpath) values (1, 'woolworths', '/static/assets/img/storegroup-icon/cd_logo_wapple.svg');
insert into store_group (id, name, iconpath) values (2, 'paknsave', '/static/assets/img/storegroup-icon/pns.png');
insert into store_group (id, name, iconpath) values (3, 'newworld', '/static/assets/img/storegroup-icon/nw.svg');
alter sequence store_group_seq restart with 4;

insert into store (id, name, group_id) values (1, 'Woolworths Non-existent 1', 1);
insert into store (id, name, group_id) values (2, 'Woolworths Non-existent 2', 1);
insert into store (id, name, group_id) values (3, 'Woolworths Non-existent 3', 1);
insert into store (id, name, group_id) values (4, 'P&S Non-existent 1', 2);
insert into store (id, name, group_id) values (5, 'P&S Non-existent 2', 2);
alter sequence store_seq restart with 6;

insert into product (id, name, gtin, item_per_package, package_size, unit) values (1, 'Milk', '1234567890123', 1, 3, 'L');
insert into product (id, name, gtin, item_per_package, package_size, unit) values (2, 'Cereal', '2345678901234', 1, 0.5, 'KG');
insert into product (id, name, gtin, item_per_package, package_size, unit) values (3, 'Bread', '3456789012345', 1, 0.5, 'KG');
alter sequence product_seq restart with 4;


insert into quote (product_id, store_id, quote_date, price) values ( 1, 1, DATE'2025-03-10', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 3, DATE'2025-03-10', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 3, DATE'2025-03-10', 7.50);

insert into quote (product_id, store_id, quote_date, price) values ( 1, 1, DATE'2025-03-09', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 1, DATE'2025-03-09', 7.29);
insert into quote (product_id, store_id, quote_date, price) values ( 3, 1, DATE'2025-03-09', 3.5);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 2, DATE'2025-03-09', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 2, DATE'2025-03-09', 7.29);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 4, DATE'2025-03-09', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 4, DATE'2025-03-09', 5.59);
insert into quote (product_id, store_id, quote_date, price) values ( 3, 4, DATE'2025-03-09', 4);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 5, DATE'2025-03-09', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 5, DATE'2025-03-09', 3.59);
insert into quote (product_id, store_id, quote_date, price) values ( 3, 5, DATE'2025-03-09', 3.99);

insert into quote (product_id, store_id, quote_date, price) values ( 1, 1, DATE'2025-03-08', 5.99);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 1, DATE'2025-03-08', 7.29);
insert into quote (product_id, store_id, quote_date, price) values ( 3, 1, DATE'2025-03-08', 3.5);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 2, DATE'2025-03-08', 5.99);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 2, DATE'2025-03-08', 7.29);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 4, DATE'2025-03-08', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 4, DATE'2025-03-08', 5.59);
insert into quote (product_id, store_id, quote_date, price) values ( 3, 4, DATE'2025-03-08', 4);
insert into quote (product_id, store_id, quote_date, price) values ( 1, 5, DATE'2025-03-08', 5);
insert into quote (product_id, store_id, quote_date, price) values ( 2, 5, DATE'2025-03-08', 3.59);
insert into quote (product_id, store_id, quote_date, price) values ( 3, 5, DATE'2025-03-08', 3.99);

insert into store_batch(id, name, enabled, job_type, cron_trigger, description) values (1, 'Woolworth Store', true, 'StoreImportJob', '0 0 10 5 * ? *', 'Import stores from server');
insert into store_group_batch_rel(batch_id, store_group_id) values (1, 1);
insert into quote_import_batch(id, name, enabled, job_type, cron_trigger, description, keyword) values (2, 'Import Cheese', true, 'ProductQuoteImportJob', '0 0 6 ? * * *', 'Import cheese products', 'cheese');
insert into product_quote_batch_rel(batch_id, store_id) values (2, 3);