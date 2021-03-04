package com.google.sps.models;

public class Recommendation {
  String username, content;
  long date;

  public Recommendation(String username, String content) {
    if (username.length() == 0) {
      this.username = "Anonymous";
    } else {
      this.username = username;
    }
    this.content = content;
    this.date = System.currentTimeMillis();
  }
}
