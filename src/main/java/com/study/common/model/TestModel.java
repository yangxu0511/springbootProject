package com.study.common.model;

public class TestModel {

    private String color;

    private int weight;

    public TestModel(int weight) {
        this.weight = weight;
    }

    public TestModel() {

    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
