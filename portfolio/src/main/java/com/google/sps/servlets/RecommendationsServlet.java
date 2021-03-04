package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.models.Recommendation;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/recommendations")
public class RecommendationsServlet extends HttpServlet {
  private final ArrayList<Recommendation> recommendations;

  public RecommendationsServlet() {
    recommendations = new ArrayList<>();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String jsonArray = new Gson().toJson(recommendations);
    resp.setContentType("application/json;");
    resp.getWriter().println(jsonArray);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String user = req.getParameter("user");
    String recommendation = req.getParameter("recommendation");

    if (recommendation.length() != 0) {
      Recommendation newRecommendation = new Recommendation(user, recommendation);
      recommendations.add(newRecommendation);
    }

    resp.setStatus(200);
    resp.sendRedirect("/music_recommendations.html");
  }
}
