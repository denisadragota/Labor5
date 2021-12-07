package com.company.View;

import com.company.Controller.RegistrationSystem;
import com.company.Exceptions.InputException;
import com.company.Exceptions.NullException;
import com.company.Model.Course;
import com.company.Model.Student;
import com.company.Model.Teacher;

import java.io.IOException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * class ConsoleView uses the RegistrationSystem controller
 * provides a menu with student and teacher side, with specific actions
 * provides Input validation methods, stores currently logged In student or teacher
 *
 * @author Denisa Dragota
 * @version 13.11.2021
 */
public class ConsoleView {

    //storing the current logged In student or teacher
    private final RegistrationSystem controller;
    private final Scanner in;
    private Long loggedStudentId;
    private Long loggedTeacherId;

    public ConsoleView(RegistrationSystem regSystem) {
        this.controller = regSystem;
        in = new Scanner(System.in);
        loggedStudentId = null;
        loggedTeacherId = null;
    }

    /**
     * validates that the input is a Long number
     * asks for a new input as long as the given input is not a Long number
     *
     * @param message shows the message when asking for Input
     * @return the Long number given by the user
     */
    public Long validateNumberInput(String message) {

        long nr = 0;
        boolean option;
        //repeat until input is a Long number
        do {
            option = true;
            try {
                System.out.print(message);
                nr = in.nextLong();
            } catch (InputMismatchException e) {
                System.out.println("Wrong number! Try again...");
                option = false;
                //prepares the Scanner for a new input
                in.reset();
                in.next();
            }
        } while (!option);

        return nr;
    }

    /**
     * finds the Student with the given id
     * if the input id does not belong to any Student in the Repo, the input repeats
     *
     * @return the Student that has id the input number
     */
    public Student validateStudentInput() {

        boolean validStudentId;
        Student givenStudent = null;
        //repeats until input is a valid Student id
        do {
            validStudentId = true;
            long stud_id = this.validateNumberInput("\nChoose id of the student you want to enroll: ");

            try {
                givenStudent = controller.findOneStudent(stud_id);
                //no student found
                if (givenStudent == null)
                    validStudentId = false;
            } catch (NullException e) {
                System.out.println(e.getMessage());
                validStudentId = false;
            } catch (SQLException s) {
                System.out.println("Unsuccessful connection to Database ");
            }
        } while (!validStudentId);

        return givenStudent;
    }

    /**
     * finds the Course with the given id
     * if the input id does not belong to any Course in the Repo, the input repeats
     *
     * @return the Course that has id the input number
     */
    public Course validateCourseInput() {

        Course givenCourse = null;
        boolean validCourseId;
        //repeats until input is a validCourse id
        do {
            validCourseId = true;

            long course_id = this.validateNumberInput("\nChoose course id: ");

            try {
                givenCourse = controller.findOneCourse(course_id);
                //no course found
                if (givenCourse == null)
                    validCourseId = false;
            } catch (NullException e) {
                System.out.println(e.getMessage());
                validCourseId = false;
            } catch (SQLException s) {
                System.out.println("Unsuccessful connection to Database ");
            }
        } while (!validCourseId);
        return givenCourse;
    }

    /**
     * gets input from the user the Student id and the Course id,
     * validates the input,
     * enrolls the student to the course
     */
    public void option1() {
        Student givenStudent = null;
        //if the teacher is logged, he can choose the student to enroll
        if (loggedStudentId == null) {
            System.out.println();
            try {
                Stream.of(controller.getAllStudents())
                        .forEach(System.out::println);

            } catch (SQLException s) {
                System.out.println("Unsuccessful connection to Database1 ");
            }

            //Choosing a Student id
            givenStudent = this.validateStudentInput();
        } else //if the student is logged in, he will be enrolled
        {
            try {
                givenStudent = this.controller.findOneStudent(loggedStudentId);
            } catch (NullException e) {
                System.out.println(e.getMessage());
            } catch (SQLException s) {
                System.out.println("Unsuccessful connection to Database2 ");
            }
        }

        try {
            System.out.println();
            Stream.of(controller.getAllCourses())
                    .forEach(System.out::println);

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database3 ");
        }
        //Choosing a Course id
        Course givenCourse = this.validateCourseInput();

        //register the student to the course
        try {
            controller.register(givenCourse, givenStudent);
            System.out.println("\nSuccessfully enrolled " + givenStudent.getFirstName() + " " + givenStudent.getLastName() + " to course: " + givenCourse.getName());

        } catch (NullException | InputException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database4 ");
        }
    }


    /**
     * gets input from the user the course id,
     * validates the input,
     * shows the students enrolled to that course
     */
    public void option2() {

        System.out.println();
        try {
            Stream.of(controller.getAllCourses())
                    .forEach(System.out::println);

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }

        //Choosing a course id
        Course searchedCourse = this.validateCourseInput();

        try {
            //show students enrolled
            Stream.of(controller.retrieveStudentsEnrolledForACourse(searchedCourse))
                    .forEach(System.out::println);

            System.out.println();
        } catch (NullException | InputException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }

    }

    /**
     * shows the list of students sorted ascending by last name and first name
     */
    public void option3() {
        System.out.println();
        try {
            Stream.of(controller.sortStudents())
                    .forEach(System.out::println);

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * shows the list of students filtered by having maximum total credits number
     */
    public void option4() {
        System.out.println();
        try {
            Stream.of(controller.filterStudents())
                    .forEach(System.out::println);

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * shows the courses with free places and how many
     */
    public void option5() {
        int freePlaces;
        try {
            for (Course course : controller.retrieveCoursesWithFreePlaces()) {
                freePlaces = course.getMaxEnrollment() - course.getStudentsEnrolled().size();
                System.out.println(freePlaces + " free places in: " + course);
            }
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
        System.out.println();
    }

    /**
     * Adds a new course to the Course Repo,
     * the teacher can choose if the course's teacher is new too,
     * and adds the teacher or updates his courses list
     */
    public void option6() {

        Teacher newCourseTeacher = null;
        String answear;

        //asks user if the teacher is new or not
        do {
            System.out.println("Teacher of the course is new? Y/N");
            answear = in.next();
        } while (!answear.equals("Y") && !(answear.equals("N")));

        // add new teacher to the repo
        if (answear.equals("Y")) {
            System.out.println("Enter First name of the teacher: ");
            String newTeacherFirstName = in.next();

            System.out.println("Enter Last name of the teacher: ");
            String newTeacherLastName = in.next();

            try {
                long newTeacherId = ((long) controller.getAllTeachers().size()) + 1;
                newCourseTeacher = new Teacher(newTeacherId, newTeacherFirstName, newTeacherLastName);
            } catch (SQLException s) {
                System.out.println("Unsuccessful connection to Database ");
            }
        }
        //course will be added to the logged in teacher
        else {
            try {
                newCourseTeacher = this.controller.findOneTeacher(this.loggedTeacherId);
            } catch (NullException e) {
                System.out.println(e.getMessage());
            } catch (SQLException s) {
                System.out.println("Unsuccessful connection to Database ");
            }
        }

        System.out.println("Enter name of the course");
        String newCourseName = in.next();

        System.out.println("Enter credits number of the course: ");
        int newCourseCredits = in.nextInt();

        System.out.println("Enter maximum enrollment number of the course: ");
        int newCourseMaxEnrollment = in.nextInt();

        try {
            int numberCourses = controller.getAllCourses().size();
            long lastCourseId = ((long) controller.getAllCourses().get(numberCourses - 1).getCourseId());
            long newCourseId = lastCourseId + 1;
            Course newCourse = new Course(newCourseId, newCourseName, newCourseTeacher, newCourseMaxEnrollment, newCourseCredits);
            try {
                System.out.println(newCourse);
                //adds course to the repo
                controller.addCourse(newCourse);
            } catch (NullException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * teacher can modify the credits number for one of his courses,
     * teacher gives the course id and the new credits number,
     * input is validated
     */
    public void option7() {

        //choose from the loggedIn teacher's courses
        System.out.println();
        try {
            Stream.of(controller.findOneTeacher(loggedTeacherId).getCourses())
                    .forEach(System.out::println);

        } catch (NullException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }

        Course foundCourse = this.validateCourseInput();
        int new_credits = 0;
        boolean okCredits = true;
        do {
            System.out.println("\nEnter the new credits number: ");
            //verify input to be an int number
            try {
                new_credits = in.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Wrong input number!");
                okCredits = false;
                in.reset();
                in.next();
            }
            //credits can be a positive number only
            if (new_credits < 0)
                okCredits = false;

        } while (!okCredits);

        //updates the course
        foundCourse.setCredits(new_credits);
        try {
            controller.modifyCredits(foundCourse);
        } catch (NullException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * Delete a course from the logged in Teacher,
     * teacher enters the course id,
     * Input is validated,
     * and the course is deleted from the teacher, from the course repo and from the students
     */
    public void option8() {
        //the logged In teacher
        Teacher givenTeacher = null;
        try {
            givenTeacher = this.controller.findOneTeacher(this.loggedTeacherId);
        } catch (NullException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }


        //Print all courses from teachers
        List<Course> courseList = givenTeacher.getCourses();
        for (Course c : courseList) {
            System.out.println(c.getCourseId() + "  " + c.getName());
        }

        //Choose course id
        Course choosenCourse = this.validateCourseInput();

        try {
            if (controller.deleteCourseFromTeacher(givenTeacher, choosenCourse)) {
                System.out.println("Course was deleted from teacher " + givenTeacher.getFirstName());
            }
        } catch (NullException | InputException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
        System.out.println();
    }


    /**
     * shows the list of courses sorted ascending by credits number
     */
    public void option9() {
        System.out.println();
        try {
            Stream.of(controller.sortCourses())
                    .forEach(System.out::println);

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * shows the list of courses filtered by having more than 10 credits
     */
    public void option10() {
        System.out.println();
        try {
            Stream.of(controller.filterCourses())
                    .forEach(System.out::println);

        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * shows the options of the Main Menu
     */
    public void printStartMenu() {
        System.out.println("---------LOG IN---------");
        System.out.println("1. Student Menu");
        System.out.println("2. Teacher Menu");
        System.out.println("0. Exit.");
    }

    /**
     * Log In by Id for the Student, the entered id is validated if it exists in the Repo
     *
     * @return true if the studentId exists in the Repo, else false
     */
    public boolean logInStudent() {
        System.out.println("---------LOG IN STUDENT---------");
        System.out.println("Enter your ID: ");
        long studId = in.nextLong();
        try {
            if (this.controller.findOneStudent(studId) == null) {
                System.out.println("Incorrect ID!");
                return false;
            } else
                this.loggedStudentId = studId;
            return true;
        } catch (NullException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
            return false;
        }
    }

    /**
     * Log In by Id for the Teacher, the entered id is validated if it exists in the Repo
     *
     * @return true if the teacherId exists in the Repo, else false
     */
    public boolean logInTeacher() {
        System.out.println("---------LOG IN TEACHER---------");
        System.out.println("Enter your ID: ");
        long teacherId = in.nextLong();
        try {
            if (this.controller.findOneTeacher(teacherId) == null) {
                System.out.println("Incorrect ID!");
                return false;
            } else
                this.loggedTeacherId = teacherId;
            return true;
        } catch (NullException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
            return false;
        }

    }

    /**
     * Show Menu for the logged In Student, showing details about his credits and enrolled courses,
     * showing possible actions
     */
    public void printStudentMenu() {
        System.out.println("---------STUDENT MENU---------");
        try {
            Student loggedInStudent = this.controller.findOneStudent(loggedStudentId);
            System.out.println("Welcome, " + loggedInStudent.getFirstName() + " " + loggedInStudent.getLastName());
            System.out.println("Your status: ");
            System.out.println("Credits: " + loggedInStudent.getTotalCredits());
            System.out.println("Enrolled courses: ");
            Stream.of(loggedInStudent.getEnrolledCourses())
                    .forEach(System.out::println);
            System.out.println();
            System.out.println("1. Enroll to a course");
            System.out.println("2. Show available courses and the number of places");
            System.out.println("3. Sort courses by number credits");
            System.out.println("4. Filter courses by > 10 credits");
            System.out.println("0. Exit.");
        } catch (NullException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * Show Menu for the logged In Teacher, showing details about his teaching courses,
     * showing possible actions
     */
    public void printTeacherMenu() {
        System.out.println("---------TEACHER MENU---------");
        try {

            Teacher loggedInTeacher = this.controller.findOneTeacher(loggedTeacherId);
            System.out.println("Welcome, " + loggedInTeacher.getFirstName() + " " + loggedInTeacher.getLastName());
            System.out.println("Your status: ");
            System.out.println("Teaching courses: ");
            Stream.of(loggedInTeacher.getCourses())
                    .forEach(System.out::println);

            System.out.println("---------STUDENTS---------");
            System.out.println("1. Enroll a student to a course");
            System.out.println("2. Show students enrolled to a given course");
            System.out.println("3. Sort students by name");
            System.out.println("4. Filter students by maximal credits number (30)");
            System.out.println("---------COURSES---------");
            System.out.println("5. Show available courses and the number of places");
            System.out.println("6. Add a new course");
            System.out.println("7. Update a course credits number");
            System.out.println("8. Delete a certain course");
            System.out.println("9. Sort courses by number credits");
            System.out.println("10. Filter courses by > 10 credits");
            System.out.println("0. Exit.");
        } catch (NullException e) {
            System.out.println(e.getMessage());
        } catch (SQLException s) {
            System.out.println("Unsuccessful connection to Database ");
        }
    }

    /**
     * Teacher Menu with actions:
     * enrolling a student to a course, showing students enrolled to a course,
     * sorting students by name, filtering student by having maximum total credits number,
     * showing available courses, adding a new course, updating credits to a course,
     * deleting a course, sorting courses by credits number,
     * filtering courses by having more than 10 credits, pressing 0 for Exit
     */
    public void teacherMenu() {
        boolean stay = true;
        int key = 0;

        while (stay) {
            //validating input (has to be an int number between 0 and 10)
            boolean option;
            do {
                printTeacherMenu();
                option = true;
                try {
                    System.out.print("Enter option: ");
                    key = in.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Wrong number! Try again...");
                    option = false;
                    in.reset();
                    in.next();
                }
            } while (!option);

            //each option has its own method
            switch (key) {
                //Exit, program ends
                case 0:
                    System.out.println("Goodbye!");

                    stay = false;
                    break;

                //Enroll a student to a course
                case 1:
                    this.option1();
                    break;

                //Show students enrolled to a given course
                case 2:
                    this.option2();
                    break;

                // Sort students by name
                case 3:
                    this.option3();
                    break;

                //Filter students by maximal credits number (30)
                case 4:
                    this.option4();
                    break;

                //Show available courses and the number of places
                case 5:
                    this.option5();
                    break;

                //Add a new course
                case 6:
                    this.option6();
                    break;

                // Update a course with a new credits number
                case 7:
                    this.option7();
                    break;

                // Delete a given course
                case 8:
                    this.option8();
                    break;

                //Sort courses by number credits
                case 9:
                    this.option9();
                    break;

                //Filter courses by > 10 credits
                case 10:
                    this.option10();
                    break;
            }
        }
    }

    /**
     * Student Menu with actions:
     * enrolling to a course, showing available courses, sorting courses by credits number,
     * filtering courses by having more than 10 credits, pressing 0 for Exit
     */
    public void studentMenu() {
        boolean stay = true;
        int key = 0;

        while (stay) {
            //validating input (has to be an int number between 0 and 4)
            boolean option;
            do {
                printStudentMenu();
                option = true;
                try {
                    System.out.print("Enter option: ");
                    key = in.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Wrong number! Try again...");
                    option = false;
                    in.reset();
                    in.next();
                }
            } while (!option);

            //each option has its own method
            switch (key) {
                //Exit, program ends
                case 0:
                    System.out.println("Goodbye!");

                    stay = false;
                    break;

                //Enroll a student to a course
                case 1:
                    this.option1();
                    break;
                //Show available courses and the number of places
                case 2:
                    this.option5();
                    break;
                //Sort courses by number credits
                case 3:
                    this.option9();
                    break;

                //Filter courses by > 10 credits
                case 4:
                    this.option10();
                    break;
            }
        }
    }

    /**
     * The (Start) Main Menu gets user input choosing the user type (student or teacher),
     * calls the proper Menu for the chosen option,
     * program ends when pressing 0
     */
    public void menu() {

        boolean stay = true;
        int key = 0;

        while (stay) {
            //validating input (has to be an int number between 0 and 10)
            boolean option;
            do {
                printStartMenu();
                option = true;
                try {
                    System.out.print("Enter option: ");
                    key = in.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Wrong number! Try again...");
                    option = false;
                    in.reset();
                    in.next();
                }
            } while (!option);

            //each option has its own method
            switch (key) {
                //Exit, program ends
                case 0:
                    System.out.println("Goodbye!");

                    stay = false;
                    break;

                //LogIn in Student Menu
                case 1:
                    if (logInStudent())
                        this.studentMenu();
                    this.loggedStudentId = null;
                    break;

                //LogIn in Teacher Menu
                case 2:
                    if (logInTeacher())
                        this.teacherMenu();
                    this.loggedTeacherId = null;
                    break;
            }
        }
    }
}
