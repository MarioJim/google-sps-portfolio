package com.google.sps.servlets;

import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/random-fact")
public class RandomFactsServlet extends HttpServlet {
  public static final List<String> messages = Arrays.asList(
      "I took a semester's worth of classes in advance",
      "My first programming language was JavaScript",
      "My favorite videogames are Smite, Factorio and Minecraft"
  );

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String json = new Gson().toJson(messages);
    resp.setContentType("application/json;");
    resp.getWriter().println(json);
  }
}
