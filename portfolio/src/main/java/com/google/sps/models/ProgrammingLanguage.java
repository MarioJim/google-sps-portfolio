package com.google.sps.models;

import com.google.gson.JsonObject;

public class ProgrammingLanguage {
  private final String color;
  private final String name;
  private double size;

  public ProgrammingLanguage(JsonObject language) {
    color = language.getAsJsonObject("node").get("color").getAsString();
    name = language.getAsJsonObject("node").get("name").getAsString();
    size = language.get("size").getAsDouble();
  }

  public ProgrammingLanguage(String color, String name) {
    this.color = color;
    this.name = name;
    size = 0;
  }

  public String getColor() {
    return color;
  }

  public String getName() {
    return name;
  }

  public double getSize() {
    return size;
  }

  public void increaseSizeBy(double size) {
    this.size += size;
  }

  public void transformToPercentage(double totalSize) {
    this.size /= totalSize;
  }
}
