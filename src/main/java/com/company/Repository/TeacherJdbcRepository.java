package com.company.Repository;

import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TeacherJdbcRepository implementing ICrudRepository<Teacher>
 * reading, storing, updating and saving Teachers instances in the database
 *
 * @author Denisa Dragota
 * @version 7/12/2021
 */
public class TeacherJdbcRepository implements ICrudRepository<Teacher> {
    private String dbUrl;
    private String user;
    private String password;
    private Connection connection;

    public TeacherJdbcRepository() throws SQLException {
        this.dbUrl = "jdbc:mysql://localhost/maplab5";
        this.user = "Denisa";
        this.password = "Denisa_1700";
        this.connection = DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Querying the Courses and Teachers tables in the database to find the teacher and the courses with the given teacherId
     *
     * @param id -the id of the entity to be returned id must not be null
     * @return the entity with the specified id or null - if there is no entity with the given id
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter id is NULL
     */
    @Override
    public Teacher findOne(Long id) throws SQLException, NullException {
        Teacher newTeacher;
        if (id == null) {
            throw new NullException("Null id!");
        }

        String queryTeacher = "SELECT teacherId, firstName, lastName FROM Teachers " +
                "WHERE teacherId = '" + id + "'";

        String queryCourses = "SELECT c.courseId, c.name, c.credits, c.maxEnrollment, t.teacherId, t.firstName, t.lastName " +
                "FROM Courses c inner join Teachers t on t.teacherId = c.teacherId " +
                "where t.teacherId = '" + id + "'";

        Statement statement = connection.createStatement();
        ResultSet resultTeacher = statement.executeQuery(queryTeacher);

        //if a teacher was found
        if (resultTeacher.next()) {

            newTeacher = new Teacher(resultTeacher.getLong("teacherId"),
                    resultTeacher.getString("firstName"),
                    resultTeacher.getString("lastName"));
            List<Course> teachingCourses = new ArrayList<>();

            //find the courses that have the given teacherId
            Statement statement2 = connection.createStatement();
            ResultSet resultCourses = statement2.executeQuery(queryCourses);

            while (resultCourses.next()) {
                Course course = new Course(resultCourses.getLong("courseId"),
                        resultCourses.getString("name"),
                        newTeacher,
                        resultCourses.getInt("maxEnrollment"),
                        resultCourses.getInt("credits"));

                teachingCourses.add(course);
            }
            newTeacher.setCourses(teachingCourses);

            return newTeacher;
        } else
            return null;
    }

    /**
     * For each teacher in the teachers table, we search in the courses table his courses (with his teacherId)
     *
     * @return a list with all teachers
     * @throws SQLException if connection to database could not succeed
     */
    @Override
    public List<Teacher> findAll() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        Teacher newTeacher = null;
        long id;
        String queryTeacher = "SELECT teacherId, firstName, lastName FROM Teachers ";

        Statement statement = connection.createStatement();
        ResultSet resultTeacher = statement.executeQuery(queryTeacher);

        //each teacher
        while (resultTeacher.next()) {

            newTeacher = new Teacher(resultTeacher.getLong("teacherId"),
                    resultTeacher.getString("firstName"),
                    resultTeacher.getString("lastName"));
            List<Course> teachingCourses = new ArrayList<>();

            id = resultTeacher.getLong("teacherId");
            String queryCourses = "SELECT c.courseId, c.name, c.credits, c.maxEnrollment, t.teacherId, t.firstName, t.lastName " +
                    "FROM Courses c inner join Teachers t on t.teacherId = c.teacherId " +
                    "where t.teacherId = '" + id + "'";

            Statement statement2 = connection.createStatement();
            ResultSet resultCourses = statement2.executeQuery(queryCourses);

            //each course with the current teacherId
            while (resultCourses.next()) {
                Course course = new Course(resultCourses.getLong("courseId"),
                        resultCourses.getString("name"),
                        newTeacher,
                        resultCourses.getInt("maxEnrollment"),
                        resultCourses.getInt("credits"));

                teachingCourses.add(course);
            }
            newTeacher.setCourses(teachingCourses);

            teachers.add(newTeacher);
        }
        return teachers;
    }

    /**
     * We add a new tuple in the table with teachers and in the table with courses (if he has any)
     *
     * @param obj entity must be not null
     * @return null- if the given entity is saved otherwise returns the entity (id already exists)
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter entity obj is NULL
     */
    @Override
    public Teacher save(Teacher obj) throws SQLException, NullException {
        Teacher existingTeacher = this.findOne(obj.getTeacherId());
        if (obj == null)
            throw new NullException("Null object!");

        if (existingTeacher != null) {
            return existingTeacher;
        } else {
            //insert the teacher in the Teachers table
            String insertTeacher = "INSERT INTO Teachers (teacherId, firstName, lastName) values ('" + obj.getTeacherId() + "', '"
                    + obj.getFirstName() + "', '"
                    + obj.getLastName() + "')";
            Statement insertStmt = connection.createStatement();
            insertStmt.executeUpdate(insertTeacher);

            //insert his courses in the Courses table
            for (Course c : obj.getCourses()) {
                String insertCourses = "INSERT INTO Courses (courseId, name, credits, teacherId, maxEnrollment) values " +
                        "('" + c.getCourseId() + "', '"
                        + c.getName() + "', '"
                        + c.getCredits() + "', '"
                        + obj.getTeacherId() + "', '"
                        + c.getMaxEnrollment() + "')";
                Statement enrollStmt = connection.createStatement();
                enrollStmt.executeUpdate(insertCourses);
            }
            return null;
        }
    }

    /**
     * Update teacher attributes in the Teachers table
     *
     * @param obj entity must not be null
     * @return null - if the entity is updated, otherwise returns the entity - (e.g id does not exist).
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter entity obj is NULL
     */
    @Override
    public Teacher update(Teacher obj) throws SQLException, NullException {
        if (obj == null)
            throw new NullException("Null Object");
        Teacher existingTeacher = this.findOne(obj.getTeacherId());
        if (existingTeacher == null) {
            return obj;
        } else {
            String updateSql = "UPDATE Teachers set firstName ='" + obj.getFirstName() + "', lastName = '" + obj.getLastName() + "'" +
                    " where teacherId = '" + obj.getTeacherId() + "'";
            Statement updateStmt = connection.createStatement();
            updateStmt.executeUpdate(updateSql);
            return null;
        }
    }

    /**
     * Delete a teacher from the Teachers table and set for his courses in the Courses table the teacherId to null
     *
     * @param id id must be not null
     * @return the removed entity or null if there is no entity with the given id
     * @throws SQLException  if connection to database could not succeed
     * @throws NullException if input parameter id is NULL
     */
    @Override
    public Teacher delete(Long id) throws SQLException, NullException {
        if (id == null)
            throw new NullException("Null id");
        Teacher teacher = null;
        String selectAll = "SELECT * FROM Teachers " +
                "WHERE teacherId = '" + id + "'";
        Statement deleteStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet deleteResultSet = deleteStmt.executeQuery(selectAll);
        if (deleteResultSet.next()) {
            teacher = new Teacher(deleteResultSet.getLong("teacherId"),
                    deleteResultSet.getString("firstName"),
                    deleteResultSet.getString("lastName"));

            String updateCourses = "UPDATE Courses set Courses.teacherId = null " +
                    "where Courses.teacherId = '" + id + "'";
            Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement2.executeUpdate(updateCourses);

            deleteResultSet.deleteRow();
        }
        return teacher;
    }
}
