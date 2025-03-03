package com.xuangand.rhythmoflife.model;

public class Contact {

    public static final int FACEBOOK = 0;
    public static final int INSTAGRAM = 1;

    private int id;
    private int image;

    public Contact(int id, int image) {
        this.id = id;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
