package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.sps.models.ProgrammingLanguage;
import com.google.sps.models.Repository;
import com.google.sps.services.GitHubGraphQLAPI;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@WebServlet("/github-languages")
public class GithubLanguagesServlet extends HttpServlet {
  private final GitHubGraphQLAPI githubApi;

  public GithubLanguagesServlet() throws IOException {
    githubApi = new GitHubGraphQLAPI();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    HashSet<Long> excludedRepos = new HashSet<>();
    excludedRepos.add(145342458L); // MarioJim/I**-T**
    excludedRepos.add(172366859L); // MarioJim/mTouch
    excludedRepos.add(183526378L); // DiegoMont/AminotecWeb
    excludedRepos.add(207170678L); // MarioJim/mariojim.github.io
    excludedRepos.add(243273100L); // MarioJim/c********-d********
    excludedRepos.add(245031161L); // KevinTMtz/GunnedDown
    excludedRepos.add(291279859L); // KevinTMtz/HackMTY2020
    excludedRepos.add(339570034L); // MarioJim/google-sps-portfolio

    String query = githubApi.createLanguagesQuery("viewer");
    JsonArray repositories = githubApi.fetchGraphQLQuery(query)
        .getAsJsonObject("viewer")
        .getAsJsonObject("repositories")
        .getAsJsonArray("nodes");
    HashMap<String, ProgrammingLanguage> languagesMap = parseAndAccumulateLanguages(repositories, excludedRepos);
    List<ProgrammingLanguage> languages = sortLanguagesBySize(languagesMap);

    resp.setContentType("application/json");
    resp.getWriter().println(new Gson().toJson(languages));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String username = req.getParameter("user");

    String query = githubApi.createLanguagesQueryFromUsername(username);
    JsonObject userData = githubApi.fetchGraphQLQuery(query).getAsJsonObject("user");
    if (userData.isJsonNull())
      throw new IOException("User not found");

    JsonArray repositories = userData.getAsJsonObject("repositories").getAsJsonArray("nodes");
    HashMap<String, ProgrammingLanguage> languagesMap = parseAndAccumulateLanguages(repositories, new HashSet<>());
    List<ProgrammingLanguage> languages = sortLanguagesBySize(languagesMap);

    resp.setContentType("application/json");
    resp.getWriter().println(new Gson().toJson(languages));
  }

  private HashMap<String, ProgrammingLanguage> parseAndAccumulateLanguages(JsonArray repositories, HashSet<Long> excludedRepos) {
    HashMap<String, ProgrammingLanguage> languagesMap = new HashMap<>();
    StreamSupport
        .stream(repositories.spliterator(), false)
        .map(jsonElement -> new Repository(jsonElement.getAsJsonObject()))
        .filter(repository -> !excludedRepos.contains(repository.getDatabaseId()))
        .flatMap(repository -> repository.getLanguages().stream())
        .forEach(language -> {
          String languageName = language.getName();
          languagesMap.putIfAbsent(languageName, new ProgrammingLanguage(language.getColor(), languageName));
          languagesMap.get(languageName).increaseSizeBy(language.getSize());
        });
    return languagesMap;
  }

  private List<ProgrammingLanguage> sortLanguagesBySize(HashMap<String, ProgrammingLanguage> languagesMap) {
    double totalSize = languagesMap
        .values()
        .stream()
        .map(ProgrammingLanguage::getSize)
        .reduce(0.0, Double::sum);
    return languagesMap
        .values()
        .stream()
        .sorted(Comparator.comparingDouble(ProgrammingLanguage::getSize).reversed())
        .peek(programmingLanguage -> programmingLanguage.transformToPercentage(totalSize))
        .collect(Collectors.toList());
  }
}
