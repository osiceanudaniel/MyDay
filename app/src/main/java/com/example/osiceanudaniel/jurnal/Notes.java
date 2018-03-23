package com.example.osiceanudaniel.jurnal;

public class Notes {

    private String picture;
    private String text;
    private String data;

    public Notes() {

    }

    public Notes(String picture, String text, String data) {
        this.picture = picture;
        this.text = text;
        this.data = data;
    }

    public String getPicture() {
        return picture;
    }

    public String getText() {
        return text;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {

        return data;
    }
}
