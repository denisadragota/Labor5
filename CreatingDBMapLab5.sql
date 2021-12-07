#create database MapLab5;
use MapLab5;
create table Students( 
studentId bigint PRIMARY KEY, firstName varchar(30), lastName varchar(30) , totalCredits int);

create table Teachers(
teacherId bigint PRIMARY KEY, firstName varchar(30), lastName varchar(30));

create table Courses(
courseId bigint PRIMARY KEY, name varchar(30), credits int, teacherId bigint,
FOREIGN KEY( teacherId ) REFERENCES Teachers (teacherId));

alter table  Courses
add foreign key (teacherId) REFERENCES Teachers(teacherId);

alter table Courses 
add column maxEnrollment int;

update  Courses 
set Courses.maxEnrollment = 10
where Courses.courseId in (1,2);

select * from Students;


insert into Students values (1,'Denisa', 'Dragota', 0);
insert into Students values (2,'Mihnea', 'Aleman', 0);
insert into Students values (3,'Raul', 'Barbat', 0);
insert into Students values (4,'Evelin', 'Bohm', 0);

insert into Teachers values (1,'Catalin', 'Rusu');
insert into Teachers values (2,'Diana', 'Cristea');

insert into Courses values (1,'OOP', 5,1,20);
insert into Courses values (2,'SDA', 5,2,30);
insert into Courses values (3,'MAP', 20,1,3);
insert into Courses values (4,'NewOptional', 20,2,3);


create table Enrolled(
studentId bigint, courseId bigint, PRIMARY KEY(studentId, courseId),
FOREIGN KEY( studentId ) REFERENCES Students (studentId), 
FOREIGN KEY( courseId ) REFERENCES Courses (courseId));


select * from Students;
select * from Teachers;
select * from Courses;
select * from Enrolled;

