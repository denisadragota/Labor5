package com.company.Controller;

import com.company.Exceptions.InputException;
import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Student;
import com.company.Model.Teacher;
import com.company.Repository.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RegistrationSystem class
 * storing repo lists of: students, teachers, courses, file with current enrollment
 * <p>
 * Functionalities: enrolling a student to a course, showing courses with free places,
 * showing all courses, showing all students enrolled to a course, deleting a course from a teacher,
 * updating students' total credits number after updating a course credits, sort students by name and courses by credits numbere,
 * filter students by maximum total credit number and courses with over 10 credits,
 * reading and saving enrollment to file
 *
 * @author Denisa Dragota
 * @version 29.11.2021
 */
public class RegistrationSystem {
    private StudentRepository studentsRepo;
    private TeacherRepository teachersRepo;
    private CourseRepository coursesRepo;

    public RegistrationSystem(StudentRepository studentsRepo,
                              TeacherRepository teachersRepo,
                              CourseRepository coursesRepo) {
        this.studentsRepo = studentsRepo;
        this.teachersRepo = teachersRepo;
        this.coursesRepo = coursesRepo;
    }


    /**
     * sort the students repo list by LastName and FirstName
     *
     * @return the sorted list
     */
    public List<Student> sortStudents() throws SQLException {
        List<Student> sortedStudents = this.getAllStudents()
                .stream()
                .sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName))
                .collect(Collectors.toList());

        return sortedStudents;
    }

    /**
     * sort the course repo list by credits number ascending
     *
     * @return the sorted list
     */
    public List<Course> sortCourses() throws SQLException {
        List<Course> sortedCourses = this.getAllCourses()
                .stream()
                .sorted(Comparator.comparing(Course::getCredits)
                        .thenComparing(Course::getName))
                .collect(Collectors.toList());

        return sortedCourses;
    }

    /**
     * filter the students repo list by condition: having maximum credits number (30)
     *
     * @return the filtered list
     */
    public List<Student> filterStudents() throws SQLException {
        List<Student> filteredStudents = this.studentsRepo.findAll()
                .stream()
                .filter(stud -> stud.getTotalCredits() == 30)
                .collect(Collectors.toList());

        return filteredStudents;
    }

    /**
     * filter the course repo list by condition: having more than 10 credits
     *
     * @return the filtered list
     */
    public List<Course> filterCourses() throws SQLException {
        List<Course> filteredCourses = this.coursesRepo.findAll()
                .stream()
                .filter(course -> course.getCredits() > 10)
                .collect(Collectors.toList());

        return filteredCourses;
    }

    /**
     * desc: enroll a student to a course
     *
     * @param course   , Course object
     * @param student, Student object
     * @return true if successfully enrolled, else false
     * @throws InputException if course or student params not existing in repo lists
     *                        or if student can not enroll to that given course under the following conditions
     *                        Conditions: student can have maximal 30 credits and a course has a maximum number of enrolled students,
     *                        student can not be enrolled multiple times to the same course
     * @throws NullException  if course or student Id is null
     * @throws InputException if student can not enroll to the course due to following situations: student
     *                        is already enrolled to the course, student exceeds the maximum number of credits, course has no free places,
     *                        course or student not existing
     * @throws SQLException   if connection to database could not succeed
     */
    public boolean register(Course course, Student student) throws InputException, SQLException, NullException {

        //check if course exists in repo
        if (course == null || coursesRepo.findOne(course.getCourseId()) == null) {
            throw new InputException("Non-existing course id!");
        }

        //check if student exists in repo
        if (student == null || studentsRepo.findOne(student.getStudentId()) == null) {
            throw new InputException("Non-existing student id!");
        }
        List<Student> courseStudents = course.getStudentsEnrolled();
        //check if course has free places
        if (courseStudents.size() == course.getMaxEnrollment()) {
            throw new InputException("Course has no free places!");
        }

        //check if student is already enrolled

        boolean found = courseStudents
                .stream()
                .anyMatch(s -> s.compareTo(student));

        if (found)
            throw new InputException("Student already enrolled!");

        //if student has over 30 credits after enrolling to this course
        int studCredits = student.getTotalCredits() + course.getCredits();
        if (studCredits > 30)
            throw new InputException("Total number of credits exceeded!");


        //add student to course
        //update courses repo
        courseStudents.add(student);
        course.setStudentsEnrolled(courseStudents);
        coursesRepo.update(course);

        //update total credits of student
        student.setTotalCredits(studCredits);

        //update enrolled courses of Student
        List<Course> studCourses = student.getEnrolledCourses();
        studCourses.add(course);
        student.setEnrolledCourses(studCourses);

        //update students Repo
        studentsRepo.update(student);


        return true;
    }

    /**
     * desc: find courses from the course repo where number of enrolled students is less than maximum enroll limit
     *
     * @return courses with free places
     * @throws SQLException if connection to database could not succeed
     */
    public List<Course> retrieveCoursesWithFreePlaces() throws SQLException {
        List<Course> freePlaces = coursesRepo.findAll()
                .stream()
                .filter(c -> c.getStudentsEnrolled().size() < c.getMaxEnrollment())
                .collect(Collectors.toList());

        return freePlaces;
    }

    /**
     * desc: retrieve all students enrolled to a course
     *
     * @param course Course object
     * @return list of students enrolled to the given course, or null if course is NULL
     * @throws InputException if the course is null
     * @throws NullException  if the courseId is null
     * @throws SQLException   if connection to database could not succeed
     */
    public List<Student> retrieveStudentsEnrolledForACourse(Course course) throws InputException, SQLException, NullException {
        if (course == null) {
            throw new InputException("Non-existing course id!");
        }
        if (coursesRepo.findOne(course.getCourseId()) != null) {
            return course.getStudentsEnrolled();
        }

        return null;
    }

    /**
     * desc: Delete a course from a teacher. Removing course from the teacher's courses list, from the students' enrolled lists and from the course repo
     *
     * @param teacher Teacher object from whom we delete a course
     * @param course  Course object, from the teacher's list, to be deleted
     * @return true if successfully deleted
     * @throws InputException if teacher or course do not exist in te repo lists,
     *                        or if the course does not correspond to that teacher
     *                        deleting course from the teacher's teaching list, from the students enrolled list and from the courses repo
     * @throws NullException  if course or id  is null
     * @throws IOException    if there occurs an error with the ObjectOutputStream in the update() or remove() method
     * @throws SQLException   if connection to database could not succeed
     */
    public boolean deleteCourseFromTeacher(Teacher teacher, Course course) throws InputException, NullException, SQLException {

        //check if course exists
        if (coursesRepo.findOne(course.getCourseId()) == null) {
            throw new InputException("Non-existing course id!");
        }

        //check if teacher exists
        if (teachersRepo.findOne(teacher.getTeacherId()) == null) {
            throw new InputException("Non-existing teacher id!");
        }

        //check if course actually is in the teacher's list of courses
        List<Course> courseList = teacher.getCourses();
        Optional<Course> c = courseList
                .stream()
                .filter(el -> el.compareTo(course))
                .findFirst();

        // course not found in teacher courses list
        if (c.isEmpty())
            throw new InputException("Course id not corresponding to teacher id!");
        else {
            //delete course from Course Repo
            coursesRepo.delete(course.getCourseId());
            this.updateStudentsCredits();
            return true;
        }
    }


    /**
     * desc: Recalculate the sum of credits provided from the enrolled courses of the students
     * Update the credits sum for each student
     *
     * @throws SQLException if connection to database could not succeed
     */
    public void updateStudentsCredits() throws SQLException {

        this.getAllStudents()
                .stream()
                .forEach(s -> {s.setTotalCredits(s.getEnrolledCourses()
                        .stream()
                        .mapToInt(c -> c.getCredits())
                        .reduce(0, (c1, c2) -> c1 + c2));
                    try {
                        studentsRepo.update(s);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (NullException e) {
                        e.printStackTrace();
                    }
                });


    }


    /**
     * desc: modifying credit number for a course, that leads to updating repo with the updated course and updating students' credits
     *
     * @param c Course object, which credits were updated
     * @throws NullException if id of a course is null
     * @throws SQLException  if connection to database could not succeed
     */
    public void modifyCredits(Course c) throws NullException, SQLException {
        /* update course in the repo */
        this.coursesRepo.update(c);

        /*update all students*/
        this.updateStudentsCredits();
    }

    /**
     * save course to the Course repo,
     * save the Teacher to the Teacher repo if he is new
     * and updates the teacher's course list
     *
     * @param c course to be added
     * @throws NullException if course id is null
     * @throws SQLException  if connection to database could not succeed
     */
    public boolean addCourse(Course c) throws SQLException, NullException {

        this.coursesRepo.save(c);
        return true;
    }


    /**
     * desc: get all students from the repo
     *
     * @return student list from the student repo
     * @throws SQLException if connection to database could not succeed
     */
    public List<Student> getAllStudents() throws SQLException {

        return this.studentsRepo.findAll();
    }

    /**
     * desc: get all courses from the repo
     *
     * @return courses list from the course repo
     * @throws SQLException if connection to database could not succeed
     */
    public List<Course> getAllCourses() throws SQLException {

        return this.coursesRepo.findAll();
    }

    /**
     * get all teachers from the repo
     *
     * @return teachers list from teh teacher repo
     * @throws SQLException if connection to database could not succeed
     */
    public List<Teacher> getAllTeachers() throws SQLException {

        return this.teachersRepo.findAll();
    }

    /**
     * searching for a student in the repo by the id
     *
     * @param id of a Student object
     * @return Student object from the student repo list with the given id
     * @throws NullException if student id is null
     * @throws SQLException  if connection to database could not succeed
     */
    public Student findOneStudent(long id) throws SQLException, NullException {
        return this.studentsRepo.findOne(id);
    }

    /**
     * desc: searching for a course in the repo by the id
     *
     * @param id of a Course object
     * @return Course object from the course repo list with the given id
     * @throws NullException if course id is null
     * @throws SQLException  if connection to database could not succeed
     */
    public Course findOneCourse(long id) throws SQLException, NullException {
        return this.coursesRepo.findOne(id);
    }

    /**
     * desc: searching for a teacher in the repo by the id
     *
     * @param id of a Teacher object
     * @return Teacher object from the teacher repo list with the given id
     * @throws NullException if teacher id is null
     * @throws SQLException  if connection to database could not succeed
     */
    public Teacher findOneTeacher(long id) throws SQLException, NullException {
        return this.teachersRepo.findOne(id);
    }


}
