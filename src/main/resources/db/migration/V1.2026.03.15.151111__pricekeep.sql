
    alter table if exists product_stats 
       add column asOfDate date not null;

    CREATE EXTENSION IF NOT EXISTS postgis;

    alter table if exists store 
       add column geo_loc GEOGRAPHY(Point, 4326);

    alter table if exists product 
       drop constraint if exists IDX81cuq7spo63x7wmrw7ev9jfi5;

    alter table if exists product 
       add constraint IDX81cuq7spo63x7wmrw7ev9jfi5 unique (gtin);
