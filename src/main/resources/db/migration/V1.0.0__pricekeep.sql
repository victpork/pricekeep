
    create sequence BaseBatch_SEQ start with 1 increment by 50;

    create sequence product_SEQ start with 1 increment by 50;

    create sequence quote_discount_SEQ start with 1 increment by 50;

    create sequence store_group_SEQ start with 1 increment by 50;

    create sequence store_SEQ start with 1 increment by 50;

    create table alert (
        target_price numeric(38,2),
        product_id bigint not null,
        primary key (product_id)
    );

    create table group_prd_code (
        internal_code varchar(255),
        product_id bigint not null,
        storegroup_id bigint not null,
        primary key (product_id, storegroup_id)
    );

    create table product (
        id bigint not null,
        description varchar(255),
        gtin varchar(14) unique,
        imgPath varchar(255),
        item_per_package integer,
        name varchar(100),
        package_size numeric(38,2),
        tags varchar(255) array,
        unit varchar(2) check ((unit in ('G','EA','KG','L','M','ML'))),
        unitScale numeric(38,2),
        primary key (id)
    );

    create table product_quote_batch_rel (
        batch_id bigint not null,
        store_id bigint not null
    );

    create table product_stats (
        product_id bigint not null,
        value numeric(38,2) not null,
        statType varchar(255) not null check ((statType in ('Q_AVG','Q_MIN','M_AVG','M_MIN','Y_AVG','Y_MIN'))),
        primary key (product_id, statType)
    );

    create table quote (
        quote_date date not null,
        dct_unit_price numeric(38,2),
        price numeric(38,2) not null,
        quote_source varchar(1) check ((quote_source in ('S','U'))),
        unit_price numeric(38,2),
        store_id bigint not null,
        product_id bigint not null,
        discount_id bigint unique,
        primary key (product_id, quote_date, store_id)
    );

    create table quote_discount (
        discount_id bigint not null,
        multibuy_quantity integer,
        sale_price numeric(38,2),
        save_value numeric(38,2) not null,
        type varchar(1) check ((type in ('P','B','E','F','O'))),
        primary key (discount_id)
    );

    create table quote_import_batch (
        id bigint not null,
        cron_trigger varchar(50),
        description varchar(100),
        enabled boolean not null,
        job_type varchar(30),
        lastRunResult smallint check ((lastRunResult between 0 and 5)),
        lastRunTime timestamp(6),
        name varchar(20),
        keyword varchar(255),
        primary key (id)
    );

    create table store (
        id bigint not null,
        address varchar(255),
        int_id varchar(255),
        name varchar(100) not null,
        url varchar(255),
        group_id bigint,
        primary key (id)
    );

    create table store_batch (
        id bigint not null,
        cron_trigger varchar(50),
        description varchar(100),
        enabled boolean not null,
        job_type varchar(30),
        lastRunResult smallint check ((lastRunResult between 0 and 5)),
        lastRunTime timestamp(6),
        name varchar(20),
        primary key (id)
    );

    create table store_group (
        id bigint not null,
        iconPath varchar(255),
        name varchar(255),
        primary key (id)
    );

    create table store_group_batch_rel (
        store_group_id bigint not null,
        batch_id bigint not null
    );

    create index IDXjmivyxk9rmgysrmsqw15lqr5b 
       on product (name);

    create index IDXcqtcg7ykq9d958exx0f2xosxx 
       on product (tags);

    create index IDX4jwky47j6xbqwgnvh39ehm3dp 
       on quote (product_id, store_id, quote_date desc);

    create index nameIdx 
       on store (name);

    create index group_index 
       on store (group_id, int_id);

    alter table if exists alert 
       add constraint FK3rcacnt56bt0q728ke3g0ofcj 
       foreign key (product_id) 
       references product;

    alter table if exists group_prd_code 
       add constraint FKfrsjx6itxilpffyhjm393tli5 
       foreign key (product_id) 
       references product;

    alter table if exists group_prd_code 
       add constraint FKj72sys8o1cslgks24544qsxdy 
       foreign key (storegroup_id) 
       references store_group;

    alter table if exists product_quote_batch_rel 
       add constraint fk_store_id 
       foreign key (store_id) 
       references store;

    alter table if exists product_quote_batch_rel 
       add constraint fk_batch_id 
       foreign key (batch_id) 
       references quote_import_batch;

    alter table if exists product_stats 
       add constraint FK8gvo9avyk3a5e2hedmjam3ah1 
       foreign key (product_id) 
       references product;

    alter table if exists quote 
       add constraint FKnldgk7ylwasyvuko6sdu6yv21 
       foreign key (store_id) 
       references store;

    alter table if exists quote 
       add constraint FK8j9vq82d4q0hya4ci4aru0e1f 
       foreign key (product_id) 
       references product;

    alter table if exists quote 
       add constraint FK24b5ekknb0kppump502qq4qw4 
       foreign key (discount_id) 
       references quote_discount;

    alter table if exists store 
       add constraint FKfmq7948icay5tb2o0xf0cgcao 
       foreign key (group_id) 
       references store_group;

    alter table if exists store_group_batch_rel 
       add constraint FKccsi8mw4vgccvbvweo92tqgl4 
       foreign key (batch_id) 
       references store_group;

    alter table if exists store_group_batch_rel 
       add constraint FKec6s7klkbeit3ymcbx2ycnjfm 
       foreign key (store_group_id) 
       references store_batch;
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
