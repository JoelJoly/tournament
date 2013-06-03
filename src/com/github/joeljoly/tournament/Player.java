package com.github.joeljoly.tournament;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/2/13
 * Time: 12:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class Player implements Serializable {
    private String firstName;
    private String lastName;
    private Integer id;
    private Integer points;

    public Player(Integer id_, String firstName, String lastName, Integer points) {
        this.id = id_;
        this.firstName = firstName;
        this.lastName = lastName;
        this.points = points;
    }

    public Player(Integer id) {
        this(id, "John", "Doe", 500);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


}
