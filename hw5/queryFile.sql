/*Warmup problems */

/*Your first Transaction*/

set transaction read write;


/*Code goes here*/
Select s.name, s.age from s;
select * from data;

commit;

/*Update, Insert, Delete: Part 1: insert entries*/
set transaction read write;


/*Code goes here*/
insert into data values (6, 60);
insert into data values (10, 33);
insert into s values (6, 6, 'Michael');
insert into s values (7, 60, 'Michelle');
commit;

/*========please do not remove=========*/
select * from data;
select * from s;
/*===================================*/

/*Part 2: update entries*/
/*Put everything in 1 transaction, as seen in above part 1*/



/*Code goes here*/
update data set data.f2=1 where data.f1=1;
update s set s.age=100 where s.name='Michelle';
update data set data.f2=100 where data.f2=50;



/*========please do not remove=========*/
select * from data;
select * from s;
/*===================================*/

/*Part 3: delete entries*/
/*Put everything in 1 transaction, as seen in above part 1*/



/*Code goes here*/
delete from data where data.f1=2;
delete from s;
commit;



/*========please do not remove=========*/
select * from data;
select * from s;
/*===================================*/
