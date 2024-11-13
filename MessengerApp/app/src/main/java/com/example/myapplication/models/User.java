package com.example.myapplication.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String image;
    String password;
    public String token;
    public String email;
    public String id;
    public User(String name, String email,String password ) {
        this.name = name;
        this.email = email;
        this.password = password;

    }
    public User(){
    }
    }