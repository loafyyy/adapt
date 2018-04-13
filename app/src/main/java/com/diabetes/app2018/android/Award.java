package com.diabetes.app2018.android;

/**
 * Created by Jackie on 2018-04-12.
 */

public class Award {
    private String name;
    private String image;
    private String description;

    public Award(String name, String image, String description) {
        this.name = name;
        this.image = image;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getImage() {
        return this.image;
    }

    public String getDescription() {
        return this.description;
    }
}
