package com.gnex.savemyride.Models;

public class User {

    public User( String firstName, String lastName, String email) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }


    public String firstName;
    public String lastName;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


}
