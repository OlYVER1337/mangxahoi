package com.example.myapplication.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name, image, email, token, id;
    String password;

    public User(String name, String email,String password ) {
        this.name = name;
        this.email = email;
        this.password = password;

    }
    public User(){
    }
    }