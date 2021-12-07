package com.company.Controller;

import com.company.Exceptions.InputException;
import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Student;
import com.company.Model.Teacher;

import com.company.Repository.CourseRepository;
import com.company.Repository.StudentRepository;
import com.company.Repository.TeacherRepository;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegistrationSystemTest class
 * test RegistrationSystem class
 *
 * @author Denisa Dragota
 * @version 30.10.2021
 */
class RegistrationSystemTest {
    private Teacher teacher1;
    private Teacher teacher2;
    private Teacher teacher3;

    private Course course1;
    private Course course2;
    private Course course3;
    private Course course4;
    private Course course5;

    private Student student1;
    private Student student2;
    private Student student3;
    private Student student4;
    private Student student5;

    private StudentRepository studentRepository;
    private TeacherRepository teacherRepository;
    private CourseRepository courseRepository;

    private RegistrationSystem regSystem;

    /**
     * create instances for testing before each test method
     */
    @BeforeEach
    void createInstances() throws SQLException {

        /* creating Teacher instances */
        teacher1 = new Teacher(1, "Catalin", "Rusu");
        teacher2 = new Teacher(2, "Diana", "Cristea");
        teacher3 = new Teacher(3, "Cristian", "Sacarea");

        /* creating Course instances */
        course1 = new Course(1, "OOP", teacher1, 20, 5);
        course2 = new Course(2, "SDA", teacher2, 30, 5);
        course3 = new Course(3, "MAP", teacher1, 3, 20);
        course4 = new Course(4, "NewOptional", teacher2, 3, 20);
        course5 = new Course(5, "Logik", teacher3, 10, 7);

        /* adding courses to each teacher*/
        List<Course> coursesTeacher1 = new ArrayList<Course>();
        coursesTeacher1.add(course1);
        coursesTeacher1.add(course3);
        teacher1.setCourses(coursesTeacher1);

        List<Course> coursesTeacher2 = new ArrayList<Course>();
        coursesTeacher2.add(course2);
        coursesTeacher2.add(course4);
        teacher2.setCourses(coursesTeacher2);

        /* creating Student instances */
        student1 = new Student(1, "Denisa", "Dragota",0);
        student2 = new Student(2, "Mihnea", "Aleman",0);
        student3 = new Student(3, "Raul", "Barbat",0);
        student4 = new Student(4, "Evelin", "Bohm",0);
        student5 = new Student(5, "Maria", "Morar",0);

        studentRepository = new StudentRepository();
        teacherRepository= new TeacherRepository();
        courseRepository = new CourseRepository();


        /* creating a RegistrationSystem instance */
        regSystem = new RegistrationSystem(studentRepository, teacherRepository, courseRepository);
    }

    /**
     * test sortStudents() method
     */
    @Test
    void sortStudents() throws SQLException {

        List<Student> sortedStudents = this.regSystem.sortStudents();
        /* building the expected result */
        List<Student> expectedStudentsList = new ArrayList<>();
        expectedStudentsList.add(student2);
        expectedStudentsList.add(student3);
        expectedStudentsList.add(student4);
        expectedStudentsList.add(student1);
        assertArrayEquals(expectedStudentsList.toArray(), sortedStudents.toArray());
    }

    /**
     * test sortCourse() method
     */
    @Test
    void sortCourses() throws SQLException {
        List<Course> sortedCourses = this.regSystem.sortCourses();
        /* building the expected result */
        List<Course> expectedCoursesList = new ArrayList<>();
        expectedCoursesList.add(course1);
        expectedCoursesList.add(course2);
        expectedCoursesList.add(course3);
        expectedCoursesList.add(course4);
        assertArrayEquals(expectedCoursesList.toArray(), sortedCourses.toArray());
    }

    /**
     * test filterStudents() method
     *
     * @throws InputException if course or student params not existing in repo list
     *                        or if student can not enroll to that given course
     */
    @Test
    void filterStudents() throws InputException, NullException, IOException, SQLException {
        /*enroll students to courses */
        regSystem.register(course1, student1);
        regSystem.register(course2, student1);
        regSystem.register(course3, student1);

        List<Student> filteredStudents = this.regSystem.filterStudents();
        /* building the expected result */
        List<Student> expectedStudentsList = new ArrayList<>();
        expectedStudentsList.add(student1);
        assertArrayEquals(expectedStudentsList.toArray(), filteredStudents.toArray());

        //undo changes (the file was updated)
        course1.setStudentsEnrolled(new ArrayList<>() {
        });
        course2.setStudentsEnrolled(new ArrayList<>() {
        });
        course3.setStudentsEnrolled(new ArrayList<>() {
        });
        student1.setEnrolledCourses(new ArrayList<>() {
        });
        student1.setTotalCredits(0);
        studentRepository.update(student1);
        courseRepository.update(course1);
        courseRepository.update(course2);
        courseRepository.update(course3);

    }

    /**
     * test filterCourses() method
     */
    @Test
    void filterCourses() throws SQLException {

        List<Course> filteredCourses = this.regSystem.filterCourses();
        /* building the expected result */
        List<Course> expectedCoursesList = new ArrayList<>();
        System.out.println(filteredCourses);
        expectedCoursesList.add(course3);
        expectedCoursesList.add(course4);
        for (Course c : expectedCoursesList)
            assertTrue(filteredCourses.contains(c));

    }

    /**
     * test register() method
     */
    @Test
    void register() throws InputException, NullException, IOException, SQLException {
        /* 1. register a student to a non-existing course in the Course Repo */
        Assertions.assertThrows(InputException.class, () -> regSystem.register(course5, student1));

        /* 2. register a non-existing student in the Student Repo to a Course */
        Assertions.assertThrows(InputException.class, () -> regSystem.register(course1, student5));

        /* 3. register 3 students to a course */
        regSystem.register(course3, student1);
        regSystem.register(course3, student2);
        regSystem.register(course3, student3);
        regSystem.register(course1, student1);

        /* assert the updates of the instances of the course and student repo */
        assertEquals(3, regSystem.findOneCourse(course3.getCourseId()).getStudentsEnrolled().size());
        assertEquals(1, regSystem.findOneStudent(student2.getStudentId()).getEnrolledCourses().size());
        assertEquals(20, regSystem.findOneStudent(student2.getStudentId()).getTotalCredits());
        assertEquals(25, regSystem.findOneStudent(student1.getStudentId()).getTotalCredits());

        /* 4. trying to enroll a student to a course with no free places */
        /* course3 has 3 total places and 3 students have been already enrolled */
        Assertions.assertThrows(InputException.class, () -> regSystem.register(course3, student4));

        /* trying to enroll a student to a course that exceeds his credit limit (30) */
        /* student1 is already enrolled to 2 courses with 20 + 6 credits */
        Assertions.assertThrows(InputException.class, () -> regSystem.register(course4, student1));

        /* trying to enroll a already enrolled student to the same course again */
        Assertions.assertThrows(InputException.class, () -> regSystem.register(course1, student1));

        //undo changes (the file was updated)
        student1.setEnrolledCourses(new ArrayList<>() {
        });
        student1.setTotalCredits(0);
        student2.setEnrolledCourses(new ArrayList<>() {
        });
        student2.setTotalCredits(0);
        student3.setEnrolledCourses(new ArrayList<>() {
        });
        student3.setTotalCredits(0);

        course1.setStudentsEnrolled(new ArrayList<>() {
        });
        course3.setStudentsEnrolled(new ArrayList<>() {
        });

        studentRepository.update(student1);
        studentRepository.update(student2);
        studentRepository.update(student3);

        courseRepository.update(course1);
        courseRepository.update(course3);


    }

    /**
     * test retrieveCoursesWithFreePlaces() method
     */
    @Test
    void retrieveCoursesWithFreePlaces() throws InputException, NullException, SQLException {
        /*enroll students to courses */
        /* course3 will have no places free */

        regSystem.register(course3, student1);
        regSystem.register(course3, student2);
        regSystem.register(course3, student3);
        regSystem.register(course1, student1);

        /*creating the expected result list */
        Course[] freeplacesCourses = new Course[3];
        freeplacesCourses[0] = course1;
        freeplacesCourses[1] = course2;
        freeplacesCourses[2] = course4;

        for (Course c : freeplacesCourses) {
            assertTrue(regSystem.retrieveCoursesWithFreePlaces().contains(c));
        }

        //undo changes (the file was updated)
        student1.setEnrolledCourses(new ArrayList<>() {
        });
        student1.setTotalCredits(0);
        student2.setEnrolledCourses(new ArrayList<>() {
        });
        student2.setTotalCredits(0);
        student3.setEnrolledCourses(new ArrayList<>() {
        });
        student3.setTotalCredits(0);

        course1.setStudentsEnrolled(new ArrayList<>() {
        });
        course3.setStudentsEnrolled(new ArrayList<>() {
        });

        studentRepository.update(student1);
        studentRepository.update(student2);
        studentRepository.update(student3);

        courseRepository.update(course1);
        courseRepository.update(course3);

    }

    /**
     * test retrieveStudentsEnrolledForACourse() method
     */
    @Test
    void retrieveStudentsEnrolledForACourse() throws InputException, NullException, SQLException {
        /* register 3 students to course3 */

        regSystem.register(course3, student1);
        regSystem.register(course3, student2);
        regSystem.register(course3, student3);

        /*creating the expected result list */
        Student[] studentsEnrolled = new Student[3];
        studentsEnrolled[0] = student1;
        studentsEnrolled[1] = student2;
        studentsEnrolled[2] = student3;

        for (Student stud : studentsEnrolled) {
            assertTrue(regSystem.retrieveStudentsEnrolledForACourse(course3).contains(stud));
        }

        /* null for a non-existing course in the Course Repo */
        assertNull(regSystem.retrieveStudentsEnrolledForACourse(course5));

        /* empty list for a course with no students enrolled */
        assertEquals(new ArrayList<Student>(), regSystem.retrieveStudentsEnrolledForACourse(course2));

        //undo changes (the file was updated)
        student1.setEnrolledCourses(new ArrayList<>() {
        });
        student1.setTotalCredits(0);
        student2.setEnrolledCourses(new ArrayList<>() {
        });
        student2.setTotalCredits(0);
        student3.setEnrolledCourses(new ArrayList<>() {
        });
        student3.setTotalCredits(0);

        course3.setStudentsEnrolled(new ArrayList<>() {
        });

        studentRepository.update(student1);
        studentRepository.update(student2);
        studentRepository.update(student3);

        courseRepository.update(course3);

    }


    /**
     * test modifyCredits() method
     */
    @Test
    void modifyCredits() throws InputException, NullException, SQLException {
        /* enrolling a student to a course */

        regSystem.register(course1, student1);

        assertEquals(5, student1.getTotalCredits());

        /* modifying the credits of a course */

        course1.setCredits(course1.getCredits() + 2);

        /* update in the course repo and students credits */
        regSystem.modifyCredits(course1);

        /* assert update of students credits */
        assertEquals(7, studentRepository.findOne(1L).getTotalCredits());

        //undo changes (the file was updated)
        student1.setEnrolledCourses(new ArrayList<>() {
        });
        student1.setTotalCredits(0);

        course1.setStudentsEnrolled(new ArrayList<>() {
        });
        course1.setCredits(5);

        studentRepository.update(student1);

        courseRepository.update(course1);
    }

    /**
     * test deleteCourseFromTeacher() method
     */
    @Test
    void deleteCourseFromTeacher() throws InputException, NullException, SQLException {
        /* enroll students to a course*/

        this.regSystem.addCourse(course5);
        regSystem.register(course5, student1);
        regSystem.register(course5, student2);
        regSystem.register(course5, student3);

        //number of courses of the teacher before deleting
        System.out.println(regSystem.findOneTeacher(((Teacher) course5.getTeacher()).getTeacherId()).getCourses());
        int coursesBefore = regSystem.findOneTeacher(((Teacher) course5.getTeacher()).getTeacherId()).getCourses().size();
        assertEquals(1, coursesBefore);

        //number credits of a student enrolled before deleting
        assertEquals(7, regSystem.findOneStudent(student1.getStudentId()).getTotalCredits());

        student1.setEnrolledCourses(new ArrayList<>() {
        });
        student1.setTotalCredits(0);
        student2.setEnrolledCourses(new ArrayList<>() {
        });
        student2.setTotalCredits(0);
        student3.setEnrolledCourses(new ArrayList<>() {
        });
        student3.setTotalCredits(0);

        studentRepository.update(student1);
        studentRepository.update(student2);
        studentRepository.update(student3);
        //delete Course
        regSystem.deleteCourseFromTeacher(teacherRepository.findOne(3L), courseRepository.findOne(5L));

        //undo changes (the file was updated)

        teacherRepository.delete(teacher3.getTeacherId());

    }

    /**
     * tests the addCourse() method
     *
     * @throws NullException if course is null
     */
    @Test
    void addCourse() throws NullException, InputException, SQLException {
        //Case1: adding a new course with an existing teacher in the repo
        //building the expected result list
        List<Course> expectedCourses = new ArrayList<>();
        expectedCourses.add(course1);
        expectedCourses.add(course2);
        expectedCourses.add(course3);
        expectedCourses.add(course4);
        expectedCourses.add(course5);

        regSystem.addCourse(course5);
        for (Course c : expectedCourses) {
            assertTrue(regSystem.getAllCourses().contains(c));
        }

        //Case2: adding a new course with a new teacher in the repo
        //expected: to add the teacher in the repo
        List<Teacher> expectedTeachers = new ArrayList<>();
        expectedTeachers.add(teacher1);
        expectedTeachers.add(teacher2);
        expectedTeachers.add(teacher3);

        for (Teacher t : expectedTeachers) {
            assertTrue(regSystem.getAllTeachers().contains(t));
        }

        //undo changes (the file was updated)
        courseRepository.delete(course5.getCourseId());
        teacherRepository.delete(teacher3.getTeacherId());
    }
}