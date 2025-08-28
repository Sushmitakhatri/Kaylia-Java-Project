package com.app.Kaylia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userId;

    @Column(nullable = false)
    @JsonProperty("fName")
    private String fName;
    @Column(nullable = false)
    @JsonProperty("lName")
    private String lName;
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    //For role ROLE_USER, ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_VENDOR
    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Products> products = new ArrayList<>();
}
