package com.dashboard.cuttingrisk.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "role")
    private String role;

    // Getters
    public UUID getId()       { return id; }
    public String getFullName(){ return fullName; }
    public String getRole()   { return role; }
}