package com.company.Model;
/**
 * Abstract class Person
 * stores and provides information about a person's: first and last name
 *
 * @author Denisa Dragota
 * @version 13.11.2021
 */
public abstract class Person{

    protected String firstName;
    protected String lastName;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

