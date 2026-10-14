package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class PermissionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String description;

    protected PermissionEntity() {}

    public PermissionEntity(String id, String name, String resource, String action, String description) {
        this.id = id;
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getResource() { return resource; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
}
