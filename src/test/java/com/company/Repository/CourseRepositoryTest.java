package com.company.Repository;

import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Student;
import com.company.Model.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CourseRepositoryTest class
 * testing CourseRepository class
 *
 * @author Denisa Dragota
 * @version 13.11.2021
 */
class CourseRepositoryTest {
    private Teacher teacher1;
    private Teacher teacher2;
    private Teacher teacher3;

    private Course course1;
    private Course course2;
    private Course course3;
    private Course course4;
    private Course course5;

    private CourseRepository course_repo;

    /**
     * create instances for testing before each test method
     */
    @BeforeEach
    void createInstances() throws IOException, SQLException {

        /* creating instances */
        teacher1 = new Teacher(1, "Catalin", "Rusu");
        teacher2 = new Teacher(2, "Diana", "Cristea");
        teacher3 = new Teacher(3, "Christian", "Sacarea");

        /* the first 4 courses exist in the file */
        course1 = new Course(1, "OOP", teacher1, 20, 5);
        course2 = new Course(2, "SDA", teacher2, 30, 5);
        course3 = new Course(3, "MAP", teacher1, 3, 20);
        course4 = new Course(4, "NewOptional", teacher2, 3, 20);
        course5 = new Course(5, "Logik", teacher3, 10, 7);

        /* set a course list to the repo*/

        course_repo = new CourseRepository();
        System.out.println(course_repo.findAll());
    }

    /**
     * test findAll() method
     */
    @Test
    void findAll() throws SQLException {
        /*creating the expected result list */
        Course[] courses = new Course[3];
        courses[0] = course1;
        courses[1] = course2;
        courses[2] = course3;

        for (Course course : courses) {
            assertTrue(course_repo.findAll().contains(course));
        }
    }

    /**
     * test findOne() method
     */
    @Test
    void findOne() throws NullException, SQLException {
        /* search for existing course id */
        assertEquals(course1, course_repo.findOne(1L));

        /*search for non-existing course id */
        assertNull(course_repo.findOne(123L));
    }

    /**
     * test save() method
     */
    @Test
    void save() throws NullException, SQLException {

        /* save course_repo size at the beginning */
        int sizeBefore = ((Collection<?>) course_repo.findAll()).size();

        /* add an already existing instance in the repo */
        /* will not be added, size remains the same */
        assertEquals(course1, course_repo.save(course1));
        assertEquals(sizeBefore, ((Collection<?>) course_repo.findAll()).size());

        /* add a new instance to the repo */
        /* size of the repo increments */
        assertNull(course_repo.save(course5));
        assertEquals(sizeBefore + 1, ((Collection<?>) course_repo.findAll()).size());
        //undo changes (the file was updated)
        this.course_repo.delete(course5.getCourseId());
    }

    /**
     * test update() method
     */
    @Test
    void update() throws NullException, SQLException {

        /* try to update a course that does not exist in the repo */
        course5.setCredits(course5.getCredits() + 1);
        assertEquals(course5, course_repo.update(course5));

        /* modify the Credits attribute from existing course1 in repo */
        int creditsBefore = course1.getCredits();
        int creditsAfter = creditsBefore + 1;
        course1.setCredits(creditsAfter);

        assertNull(course_repo.update(course1));
        assertEquals(creditsAfter, course_repo.findOne(course1.getCourseId()).getCredits());

        // undo changes (the file was updated)
        course1.setCredits(creditsBefore);
        course_repo.update(course1);
    }

    /**
     * test delete() method
     */
    @Test
    void delete() throws NullException, SQLException {

        /* try to delete a non-existing courseId in the repo */
        assertNull(course_repo.delete(course5.getCourseId()));


        /* delete a course from the repo */
        assertEquals(course1, course_repo.delete(course1.getCourseId()));

        /* the courseId does not exist in the repo anymore */
        assertNull(course_repo.findOne(course1.getCourseId()));

        //undo changes (the file was updated)
        this.course_repo.save(course1);
    }
}