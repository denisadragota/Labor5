package com.company.Model;
import java.util.ArrayList;
import java.util.List;

/**
 * class Teacher extends abstract class Person
 * stores and provides information about a teacher's: id, first name, last name, list of courses
 *
 * @author Denisa Dragota
 * @version 13.11.2021
 */
public class Teacher extends Person{
    private long teacherId; //unique identifier of an object
    private transient List<Course> courses;

    public Teacher(long teacherId, String firstName, String lastName) {
        this.teacherId = teacherId;
        this.courses = new ArrayList<>() {
        };
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Teacher() {
    }


    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", teacherId=" + teacherId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return teacherId == teacher.teacherId;
    }


}
