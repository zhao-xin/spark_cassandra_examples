CREATE KEYSPACE cloudcomputing
WITH REPLICATION = {
'class' : 'NetworkTopologyStrategy',
'datacenter1' : 2
} ;

use cloudcomputing ;

CREATE TABLE data (
campus text,
date timestamp,
sensor_id int,
temperature float,
PRIMARY KEY (campus, date, sensor_id)
) WITH COMPACT STORAGE ;

insert into data(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 1, 26) ;
insert into data(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 2, 26.5) ;
insert into data(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 3, 25) ;
insert into data(campus,date,sensor_id,temperature) values ('Gatton', '2018-06-01', 4, 23.5) ;
insert into data(campus,date,sensor_id,temperature) values ('Gatton', '2018-06-01', 5, 23) ;
insert into data(campus,date,sensor_id,temperature) values ('Herston', '2018-06-01', 6, 21.5) ;
insert into data(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-02', 1, 28.5) ;
insert into data(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-02', 2, 29) ;
insert into data(campus,date,sensor_id,temperature) values ('Gatton', '2018-06-02', 4, 22) ;

insert into data(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 1, 26.1) ;

CREATE TABLE date (
campus text,
date timestamp,
sensor_id int,
temperature float,
PRIMARY KEY (date, campus, sensor_id)
) WITH COMPACT STORAGE ;

insert into date(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 1, 26) ;
insert into date(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 2, 26.5) ;
insert into date(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 3, 25) ;
insert into date(campus,date,sensor_id,temperature) values ('Gatton', '2018-06-01', 4, 23.5) ;
insert into date(campus,date,sensor_id,temperature) values ('Gatton', '2018-06-01', 5, 23) ;
insert into date(campus,date,sensor_id,temperature) values ('Herston', '2018-06-01', 6, 21.5) ;
insert into date(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-02', 1, 28.5) ;
insert into date(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-02', 2, 29) ;
insert into date(campus,date,sensor_id,temperature) values ('Gatton', '2018-06-02', 4, 22) ;

insert into date(campus,date,sensor_id,temperature) values ('St Lucia', '2018-06-01', 1, 26.1) ;

