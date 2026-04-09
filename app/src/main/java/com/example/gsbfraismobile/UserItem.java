package com.example.gsbfraismobile;

public class UserItem {
    private final int id;
    private final String nomComplet;
    private final String email;
    private final String role;

    public UserItem(int id, String nomComplet, String email, String role) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}