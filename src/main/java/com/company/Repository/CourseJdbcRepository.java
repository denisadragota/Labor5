package com.company.Repository;

import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Student;
import com.company.Model.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseJdbcRepository implementing ICrudRepository<Course>
 * reading, storing, updating and saving Courses instances in the database
 *
 * @author Denisa Dragota
 * @version 7/12/2021
 */
public class CourseJdbcRepository implements ICrudRepository<Course> {
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;

    public CourseJdbcRepository() throws SQLException {
        this.dbUrl = "jdbc:mysql://localhost/maplab5";
        this.user = "Denisa";
        this.password = "Denisa_1700";
        this.connection = DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Querying the Courses, Teachers, Students and Enrolled tables in the database to find course and its enrolled students and the teacher with the given teacherId
     *
     * @param id -the id of the entity to be returned id must not be null
     * @return the entity with the specified id or null - if there is no entity with the given id
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter id is NULL
     */
    @Override
    public Course findOne(Long id) throws SQLException, NullException {
        if (id == null) {
            throw new NullException("Null id!");
        }
        Course newCourse;
        List<Student> enrolledStudents = new ArrayList<>();
        String queryCourse = "SELECT c.courseId,c.name,t.teacherId,t.firstName,t.lastName,c.maxEnrollment,c.credits " +
                "FROM Courses c left outer join Teachers t on c.teacherId = t.teacherId where c.courseId = '" + id + "'";

        String queryEnrolled = "SELECT s.studentId, s.firstName, s.lastName, s.totalCredits " +
                "FROM Enrolled e inner join Students s on e.studentId = s.studentId " +
                "where e.courseId = '" + id + "'";

        Statement statement = connection.createStatement();
        ResultSet resultCourse = statement.executeQuery(queryCourse);

        //if the given courseId was found
        if (resultCourse.next()) {

            newCourse = new Course(resultCourse.getLong("courseId"),
                    resultCourse.getString("name"),
                    new Teacher(resultCourse.getLong("teacherId"),
                            resultCourse.getString("firstName"),
                            resultCourse.getString("lastName")),
                    resultCourse.getInt("maxEnrollment"),
                    resultCourse.getInt("credits"));

            Statement statement2 = connection.createStatement();
            ResultSet resultEnrolled = statement2.executeQuery(queryEnrolled);

            //find the students enrolled to the given courseId
            while (resultEnrolled.next()) {
                Student student = new Student(resultEnrolled.getLong("studentId"),
                        resultEnrolled.getString("firstName"),
                        resultEnrolled.getString("lastName"),
                        resultEnrolled.getInt("totalCredits"));

                enrolledStudents.add(student);
            }
            newCourse.setStudentsEnrolled(enrolledStudents);

            return newCourse;
        } else
            return null;
    }

    /**
     * Querying the Courses, Teachers, Students and Enrolled tables in the database to retrieve all courses and their enrolled students and the teacher with the given teacherId
     *
     * @return a list with all courses
     * @throws SQLException if connection to database could not succeed
     */
    @Override
    public List<Course> findAll() throws SQLException {
        List<Course> courses = new ArrayList<>();
        Course newCourse = null;
        long id;

        String queryCourse = "SELECT c.courseId,c.name,t.teacherId,t.firstName,t.lastName,c.maxEnrollment,c.credits " +
                "FROM Courses c left outer join Teachers t on c.teacherId = t.teacherId ";

        Statement statement = connection.createStatement();
        ResultSet resultCourse = statement.executeQuery(queryCourse);

        //each course
        while (resultCourse.next()) {

            newCourse = new Course(resultCourse.getLong("courseId"),
                    resultCourse.getString("name"),
                    new Teacher(resultCourse.getLong("teacherId"),
                            resultCourse.getString("firstName"),
                            resultCourse.getString("lastName")),
                    resultCourse.getInt("maxEnrollment"),
                    resultCourse.getInt("credits"));

            id = resultCourse.getLong("courseId");

            String queryEnrolled = "SELECT s.studentId, s.firstName, s.lastName, s.totalCredits " +
                    "FROM Enrolled e inner join Students s on e.studentId = s.studentId " +
                    "where e.courseId = '" + id + "'";

            Statement statement2 = connection.createStatement();
            ResultSet resultEnrolled = statement2.executeQuery(queryEnrolled);

            //all students enrolled to the current Course
            List<Student> enrolledStudents = new ArrayList<>();
            while (resultEnrolled.next()) {
                Student student = new Student(resultEnrolled.getLong("studentId"),
                        resultEnrolled.getString("firstName"),
                        resultEnrolled.getString("lastName"),
                        resultEnrolled.getInt("totalCredits"));

                enrolledStudents.add(student);
            }
            newCourse.setStudentsEnrolled(enrolledStudents);

            courses.add(newCourse);
        }
        return courses;
    }

    /**
     * We add a new tuple in the Course table with the given course,
     * we add tuples in the Enrolled table if there are enrolled students,
     * we add a new teacher (if he is new)
     *
     * @param obj entity must be not null
     * @return null- if the given entity is saved otherwise returns the entity (id already exists)
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter entity obj is NULL
     */
    @Override
    public Course save(Course obj) throws SQLException, NullException {
        if (obj == null)
            throw new NullException("Null object!");

        Course existingCourse = this.findOne(obj.getCourseId());
        if (existingCourse != null) {
            return existingCourse;
        } else {
            //check if Teacher already exists or new
            String queryTeacher = "SELECT teacherId, firstName, lastName FROM Teachers " +
                    "WHERE teacherId = '" + obj.getTeacher().getTeacherId() + "'";

            Statement statement = connection.createStatement();
            ResultSet resultTeacher = statement.executeQuery(queryTeacher);

            //if the Teacher is new
            if (!resultTeacher.next()) {
                String insertTeacher = "INSERT INTO Teachers (teacherId, firstName, lastName) values ('" + obj.getTeacher().getTeacherId() + "', '"
                        + obj.getTeacher().getFirstName() + "', '"
                        + obj.getTeacher().getLastName() + "')";
                Statement insertStmt = connection.createStatement();
                insertStmt.executeUpdate(insertTeacher);
            }

            //insert Course
            String insertCourse = "INSERT INTO Courses (courseId, name, credits, teacherId, maxEnrollment) " +
                    "values ('" + obj.getCourseId() + "', '"
                    + obj.getName() + "', '"
                    + obj.getCredits() + "', '"
                    + obj.getTeacher().getTeacherId() + "', '"
                    + obj.getMaxEnrollment() + "')";

            Statement insertEnrollStmt = connection.createStatement();
            insertEnrollStmt.executeUpdate(insertCourse);
            //insert the Enrolled tuples with the students for the given course
            for (Student s : obj.getStudentsEnrolled()) {
                String insertEnroll = "INSERT INTO Enrolled (studentId, courseId) values ('" + s.getStudentId() + "', '"
                        + obj.getCourseId() + "')";
                Statement enrollStmt = connection.createStatement();
                enrollStmt.executeUpdate(insertEnroll);
            }
            return null;
        }
    }

    /**
     * Update in the Courses table the attributes for the given Course,
     * update the Enrolled table with the current enrolled students for the course
     *
     * @param obj entity must not be null
     * @return null - if the entity is updated, otherwise returns the entity - (e.g id does not exist).
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter entity obj is NULL
     */
    @Override
    public Course update(Course obj) throws SQLException, NullException {
        if (obj == null)
            throw new NullException("Null Object");
        Course existingCourse = this.findOne(obj.getCourseId());
        if (existingCourse == null) {
            return obj;
        } else {
            String updateSql = "UPDATE Courses set name ='" + obj.getName() + "', teacherId = '"
                    + obj.getTeacher().getTeacherId() + "', maxEnrollment = '" + obj.getMaxEnrollment()
                    + "', credits = '" + obj.getCredits() + "'where courseId = '"
                    + obj.getCourseId() + "'";
            Statement updateStmt = connection.createStatement();
            updateStmt.executeUpdate(updateSql);

            String deleteEnrolled = "SELECT *" +
                    "FROM Enrolled e " +
                    "where e.courseId = '" + obj.getCourseId() + "'";
            Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultEnrolled = statement2.executeQuery(deleteEnrolled);

            //deleting all Enrolled tuples for the given course
            while (resultEnrolled.next()) {
                resultEnrolled.deleteRow();
            }

            //adding again the current Enrolled tuples with the students for the given course
            for (Student s : obj.getStudentsEnrolled()) {
                String insertEnroll = "INSERT INTO Enrolled (studentId, courseId) values ('" + s.getStudentId() + "', '"
                        + obj.getCourseId() + "')";
                Statement enrollStmt = connection.createStatement();
                enrollStmt.executeUpdate(insertEnroll);
            }
            return null;
        }
    }

    /**
     * Remove the Course from the Courses table and all the tuples with the given courseId from the Enrolled students
     *
     * @param id id must be not null
     * @return the removed entity or null if there is no entity with the given id
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter id is NULL
     */
    @Override
    public Course delete(Long id) throws SQLException, NullException {
        if (id == null)
            throw new NullException("Null id");
        Course course = this.findOne(id);
        if (course != null) {
            String selectAll = "SELECT * FROM Courses " +
                    "WHERE courseId = '" + id + "'";
            Statement deleteStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet deleteResultSet = deleteStmt.executeQuery(selectAll);
            //if course exists in the table
            if (deleteResultSet.next()) {

                String queryEnrolled = "SELECT *" +
                        "FROM Enrolled e " +
                        "where e.courseId = '" + id + "'";
                Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet resultEnrolled = statement2.executeQuery(queryEnrolled);

                //deleting all Enrolled tuples for the given course
                while (resultEnrolled.next()) {
                    resultEnrolled.deleteRow();
                }
                deleteResultSet.deleteRow();
            }
        }
        return course;
    }
}
