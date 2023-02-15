package com.example.snake.Models;

import java.util.ArrayList;
import java.util.Map;

public class User {

    private String email;
    private String password;
    private String name;
    private ArrayList<Integer> records;
    private boolean premium;


    public User() {
        this.records = new ArrayList<>();
    }

    public User(String email, String password, String name, ArrayList<Integer> records, boolean premium) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.records = records;
        this.premium = premium;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public ArrayList<Integer> getRecords() {
        return records;
    }

    public User setRecords(ArrayList<Integer> records) {
        this.records = records;
        return this;
    }

    public boolean isPremium() {
        return premium;
    }

    public User setPremium(boolean premium) {
        this.premium = premium;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", records=" + records +
                ", premium=" + premium +
                '}';
    }
}
