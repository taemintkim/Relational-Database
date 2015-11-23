/* Test Rollback and Recovery Here */

set transaction read write;



/* update table and end transaction above */

set transaction read write;
select * from data;
insert into data values (6, 60);
insert into data values (10, 33);
select * from data;
-- update data set data.f2=1 where data.f1=1;
-- ###flush;
rollback;

select * from data;
-- select * from s;