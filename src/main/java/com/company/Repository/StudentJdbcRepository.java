package com.company.Repository;

import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Student;
import com.company.Model.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentJdbcRepository implementing ICrudRepository<Student>
 * reading, storing, updating and saving Students instances in the database
 *
 * @author Denisa Dragota
 * @version 7/12/2021
 */
public class StudentJdbcRepository implements ICrudRepository<Student> {
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;

    public StudentJdbcRepository() throws SQLException {
        this.dbUrl = "jdbc:mysql://localhost/maplab5";
        this.user = "Denisa";
        this.password = "Denisa_1700";
        this.connection = DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Querying the Courses, Teachers, Students and Enrolled tables in the database to find the student and the courses he is enrolled to and the teachers
     *
     * @param id -the id of the entity to be returned id must not be null
     * @return the entity with the specified id or null - if there is no entity with the given id
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter id is NULL
     */
    @Override
    public Student findOne(Long id) throws SQLException, NullException {

        Student newStudent;
        List<Course> enrolledCourses = new ArrayList<>();

        if (id == null) {
            throw new NullException("Null id!");
        }
        String queryStudent = "SELECT studentId, firstName, lastName,totalCredits FROM Students " +
                "WHERE studentId = '" + id + "'";

        String queryEnrolled = "SELECT c.courseId, c.name, c.credits, c.maxEnrollment, t.teacherId, t.firstName, t.lastName " +
                "FROM Enrolled e inner join Courses c on c.courseId = e.courseId " +
                "inner join Teachers t on t.teacherId = c.teacherId " +
                "where e.studentId = '" + id + "'";

        Statement statement = connection.createStatement();
        ResultSet resultStudent = statement.executeQuery(queryStudent);

        //if student was found
        if (resultStudent.next()) {

            newStudent = new Student(resultStudent.getLong("studentId"),
                    resultStudent.getString("firstName"),
                    resultStudent.getString("lastName"),
                    resultStudent.getInt("totalCredits"));

            Statement statement2 = connection.createStatement();
            ResultSet resultEnrolled = statement2.executeQuery(queryEnrolled);

            //find the courses the given student is enrolled to
            while (resultEnrolled.next()) {
                Course course = new Course(resultEnrolled.getLong("courseId"),
                        resultEnrolled.getString("name"),
                        new Teacher(resultEnrolled.getLong("teacherId"),
                                resultEnrolled.getString("firstName"),
                                resultEnrolled.getString("lastName")),
                        resultEnrolled.getInt("maxEnrollment"),
                        resultEnrolled.getInt("credits"));

                enrolledCourses.add(course);
            }
            newStudent.setEnrolledCourses(enrolledCourses);

            return newStudent;
        } else
            return null;
    }


    /**
     * Querying the Courses, Teachers, Students and Enrolled tables in the database to find all the students and the courses he is enrolled to and the teachers
     *
     * @return if connection to database could not succeed
     * @throws SQLException if connection to database could not succeed
     */
    @Override
    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        Student newStudent = null;
        long id;
        String queryStudent = "SELECT studentId, firstName, lastName,totalCredits FROM Students ";

        Statement statement = connection.createStatement();
        ResultSet resultStudent = statement.executeQuery(queryStudent);

        //for each student
        while (resultStudent.next()) {

            newStudent = new Student(resultStudent.getLong("studentId"),
                    resultStudent.getString("firstName"),
                    resultStudent.getString("lastName"),
                    resultStudent.getInt("totalCredits"));

            id = resultStudent.getLong("studentId");
            String queryEnrolled = "SELECT c.courseId, c.name, c.credits, c.maxEnrollment, t.teacherId, t.firstName, t.lastName " +
                    "FROM Enrolled e inner join Courses c on c.courseId = e.courseId " +
                    "inner join Teachers t on t.teacherId = c.teacherId " +
                    "where e.studentId = '" + id + "'";

            Statement statement2 = connection.createStatement();
            ResultSet resultEnrolled = statement2.executeQuery(queryEnrolled);

            //find the courses the current student is enrolled to
            List<Course> enrolledCourses = new ArrayList<>();
            while (resultEnrolled.next()) {
                Course course = new Course(resultEnrolled.getLong("courseId"),
                        resultEnrolled.getString("name"),
                        new Teacher(resultEnrolled.getLong("teacherId"),
                                resultEnrolled.getString("firstName"),
                                resultEnrolled.getString("lastName")),
                        resultEnrolled.getInt("maxEnrollment"),
                        resultEnrolled.getInt("credits"));

                enrolledCourses.add(course);
            }
            newStudent.setEnrolledCourses(enrolledCourses);

            students.add(newStudent);
        }
        return students;
    }

    /**
     * We add a new tuple in the Students table with the given student,
     * we add tuples in the Enrolled table if there are courses he is enrolled to
     *
     * @param obj entity must be not null
     * @return null- if the given entity is saved otherwise returns the entity (id already exists)
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter entity obj is NULL
     */
    @Override
    public Student save(Student obj) throws SQLException, NullException {
        if (obj == null)
            throw new NullException("Null object!");
        Student existingStudent = this.findOne(obj.getStudentId());
        if (existingStudent != null) {
            return existingStudent;
        } else {
            String insertStudent = "INSERT INTO Students (studentId, firstName, lastName, totalCredits) values ('" + obj.getStudentId() + "', '"
                    + obj.getFirstName() + "', '"
                    + obj.getLastName() + "', '"
                    + obj.getTotalCredits() + "')";
            Statement insertStmt = connection.createStatement();
            insertStmt.executeUpdate(insertStudent);
            //insert the Enrolled tuples with the courses the given student is enrolled to
            for (Course c : obj.getEnrolledCourses()) {
                String insertEnroll = "INSERT INTO Enrolled (studentId, courseId) values ('" + obj.getStudentId() + "', '"
                        + c.getCourseId() + "')";
                Statement enrollStmt = connection.createStatement();
                enrollStmt.executeUpdate(insertEnroll);
            }
            return null;
        }
    }

    /**
     * Update in the Students table the attributes for the given Student,
     * update the Enrolled table with the current enrolled courses of the student
     *
     * @param obj entity must not be null
     * @return null - if the entity is updated, otherwise returns the entity - (e.g id does not exist).
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter entity obj is NULL
     */
    @Override
    public Student update(Student obj) throws SQLException, NullException {
        if (obj == null)
            throw new NullException("Null Object");
        Student existingStudent = this.findOne(obj.getStudentId());
        if (existingStudent == null) {
            return obj;
        } else {
            String updateSql = "UPDATE Students set firstName ='" + obj.getFirstName() + "', lastName = '"
                    + obj.getLastName() + "', totalCredits = '" + obj.getTotalCredits() + "'where studentId = '"
                    + obj.getStudentId() + "'";
            Statement updateStmt = connection.createStatement();
            updateStmt.executeUpdate(updateSql);

            String deleteEnrolled = "SELECT *" +
                    "FROM Enrolled e " +
                    "where e.studentId = '" + obj.getStudentId() + "'";
            Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultEnrolled = statement2.executeQuery(deleteEnrolled);

            //deleting all Enrolled tuples for the given student
            while (resultEnrolled.next()) {
                resultEnrolled.deleteRow();
            }

            //adding again the current Enrolled tuples with the courses the given student is enrolled to
            for (Course c : obj.getEnrolledCourses()) {
                String insertEnroll = "INSERT INTO Enrolled (studentId, courseId) values ('" + obj.getStudentId() + "', '"
                        + c.getCourseId() + "')";
                Statement enrollStmt = connection.createStatement();
                enrollStmt.executeUpdate(insertEnroll);
            }
            return null;
        }
    }

    /**
     * Remove the Student from the Students table and all the tuples with the given studentId from his the Enrolled courses
     *
     * @param id id must be not null
     * @return the removed entity or null if there is no entity with the given id
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter id is NULL
     */
    @Override
    public Student delete(Long id) throws SQLException, NullException {
        if (id == null)
            throw new NullException("Null id");
        Student student = null;
        String selectAll = "SELECT * FROM Students " +
                "WHERE studentId = '" + id + "'";
        Statement deleteStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet deleteResultSet = deleteStmt.executeQuery(selectAll);

        //if the student exists in the database
        if (deleteResultSet.next()) {
            student = new Student(deleteResultSet.getLong("studentId"),
                    deleteResultSet.getString("firstName"),
                    deleteResultSet.getString("lastName"),
                    deleteResultSet.getInt("totalCredits"));

            String queryEnrolled = "SELECT *" +
                    "FROM Enrolled e " +
                    "where e.studentId = '" + id + "'";
            Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultEnrolled = statement2.executeQuery(queryEnrolled);

            //deleting all Enrolled tuples for the given student
            while (resultEnrolled.next()) {
                resultEnrolled.deleteRow();
            }
            deleteResultSet.deleteRow();
        }
        return student;
    }
}
