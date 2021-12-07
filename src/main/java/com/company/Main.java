package com.company;

import com.company.Controller.RegistrationSystem;
import com.company.Repository.*;
import com.company.View.ConsoleView;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) throws SQLException {
        StudentRepository studRepo = new StudentRepository();
        TeacherRepository teacherRepo = new TeacherRepository();
        CourseRepository courseRepo = new CourseRepository();

        RegistrationSystem controller = new RegistrationSystem(studRepo, teacherRepo, courseRepo);
        ConsoleView view = new ConsoleView(controller);
        view.menu();
    }
}
