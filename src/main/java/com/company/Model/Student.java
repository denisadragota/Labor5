package com.company.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * class Student extends abstract class Person
 * stores and provides information about a student's : id, first name, last name,
 * total credits number and enrolled courses
 *
 * @author Denisa Dragota
 * @version 13.11.2021
 */
public class Student extends Person{
    private long studentId; //unique identifier of an object
    private transient int totalCredits;
    private transient List<Course> enrolledCourses;

    public Student(long studentId, String firstName, String lastName, int totalCredits) {
        this.studentId = studentId;
        this.totalCredits = totalCredits;
        this.enrolledCourses = new ArrayList<>() {
        };
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Student() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId == student.studentId;
    }


    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<Course> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    /**
     * comparation based on id (unique identifier)
     *
     * @param other, Student object to compare with
     * @return true if objects are equal, else false
     */
    public boolean compareTo(Student other) {

        /* comparing id */
        return this.studentId == other.getStudentId();
    }

    @Override
    public String toString() {
        return "Student{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", studentId=" + studentId +
                ", totalCredits=" + totalCredits +
                '}';
    }
}
