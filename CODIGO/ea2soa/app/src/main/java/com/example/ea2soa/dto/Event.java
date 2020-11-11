package com.example.ea2soa.dto;

public class Event {
    private String typeEvents;
    private Long dni;
    private String description;
    private Long id;

    public String getTypeEvents() {return typeEvents;}

    public void setTypeEvents(String type_events) {this.typeEvents = typeEvents;}

    public long getDni() {return dni;}

    public void setDni(Long dni) {this.dni = dni;}

    public String getDescription() {return description;}

    public void setDescription(String description) { this.description = description;}

    public Long getId() { return id;}

    public void setId(Long id) {this.id = id;}
}
