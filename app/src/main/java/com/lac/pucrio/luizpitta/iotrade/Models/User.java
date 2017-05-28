package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 3L;

    private String name;
    private String email;
    private double budget;

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public double getBudget() {
        return budget;
    }


}
