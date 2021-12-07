package com.company.Repository;

import com.company.Exceptions.NullException;
import com.company.Model.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StudentRepositoryTest class
 * testing StudentRepository class
 *
 * @author Denisa Dragota
 * @version 13.11.2021
 */
class StudentRepositoryTest {

    private Student student1;
    private Student student2;
    private Student student3;
    private Student student4;
    private Student student5;
    private StudentRepository stud_repo;

    /**
     * create instances for testing before each test method
     */
    @BeforeEach
    void createInstances() throws SQLException {

        /*creating instances to test*/
        /* the first 4 students exist in the file */
        student1 = new Student(1, "Denisa", "Dragota", 0);
        student2 = new Student(2, "Mihnea", "Aleman", 0);
        student3 = new Student(3, "Raul", "Barbat", 0);
        student4 = new Student(4, "Evelin", "Bohm", 0);
        student5 = new Student(5, "Maria", "Morar", 0);


        stud_repo = new StudentRepository();
    }

    /**
     * test findAll() method
     */
    @Test
    void findAll() throws SQLException {

        /*creating the expected result list */
        Student[] students = new Student[4];
        students[0] = student1;
        students[1] = student2;
        students[2] = student3;
        students[3] = student4;

        for (Student stud : students) {
            assertTrue(stud_repo.findAll().contains(stud));
        }

    }

    /**
     * test findOne() method
     */
    @Test
    void findOne() throws NullException, SQLException {

        /* search for null student id */
        Assertions.assertThrows(NullException.class, () -> stud_repo.findOne(null));

        /*search for non-existing student id */
        assertNull(stud_repo.findOne(123L));

        /*search for existing student id */
        assertEquals(student1, stud_repo.findOne(1L));
    }

    /**
     * test save() method
     */
    @Test
    void save() throws NullException, IOException, SQLException {

        /* save stud_repo size at the beginning */
        int sizeBefore = ((Collection<?>) stud_repo.findAll()).size();

        /* add an already existing instance in the repo */
        /* will not be added, size remains the same */
        assertEquals(student1, stud_repo.save(student1));
        assertEquals(sizeBefore, ((Collection<?>) stud_repo.findAll()).size());

        /* add a new instance to the repo */
        /* size of the repo increments */
        assertNull(stud_repo.save(student5));
        assertEquals(sizeBefore + 1, ((Collection<?>) stud_repo.findAll()).size());

        //undo changes (the file was updated)
        this.stud_repo.delete(student5.getStudentId());
    }

    /**
     * test update() method
     */
    @Test
    void update() throws NullException, IOException, SQLException {

        /* try to update a student that does not exist in the repo */
        student5.setTotalCredits(30);
        assertEquals(student5, stud_repo.update(student5));

        /* modify the TotalCredits attribute from existing student1 in repo */
        assertEquals(0, stud_repo.findOne(student1.getStudentId()).getTotalCredits());
        student1.setTotalCredits(30);
        assertNull(stud_repo.update(student1));
        assertEquals(30, stud_repo.findOne(student1.getStudentId()).getTotalCredits());

        // undo changes (the file was updated)
        student1.setTotalCredits(0);
        stud_repo.update(student1);
    }

    /**
     * test delete() method
     */
    @Test
    void delete() throws NullException, IOException, SQLException {

        /* try to delete a non-existing studentId in the repo */
        assertNull(stud_repo.delete(student5.getStudentId()));

        /* delete a student from the repo */
        assertEquals(student1, stud_repo.delete(student1.getStudentId()));

        /* the studentId does not exist in the repo anymore */
        assertNull(stud_repo.findOne(student1.getStudentId()));

        //undo changes (the file was updated)
        stud_repo.save(student1);
    }
}