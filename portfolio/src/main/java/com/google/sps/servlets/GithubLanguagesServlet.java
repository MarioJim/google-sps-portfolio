package com.google.sps.servlets;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.secretmanager.v1.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.sps.models.ProgrammingLanguage;
import com.google.sps.models.Repository;

@WebServlet("/github-languages")
public class GithubLanguagesServlet extends HttpServlet {
  private static final String graphqlQuery = "{%s {repositories(isFork: false, first: 50) {nodes {databaseId languages(first: 5) {edges {size node {color name}}}}}}}";

  private final String authorizationSecret;

  public GithubLanguagesServlet() throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of("mjimenezvizcaino-sps-spring21", "github_graphql_api_key", "1");
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
      authorizationSecret = response.getPayload().getData().toStringUtf8();
    }
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

    String query = createQuery("viewer");
    JsonArray repositories = fetchGithubLanguagesData(query)
        .getAsJsonObject("data")
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

    String query = createQueryFromUsername(username);
    JsonObject data = fetchGithubLanguagesData(query);
    if (data.getAsJsonObject("data").getAsJsonObject("user").isJsonNull())
      throw new IOException("User not found");

    JsonArray repositories = data
        .getAsJsonObject("data")
        .getAsJsonObject("user")
        .getAsJsonObject("repositories")
        .getAsJsonArray("nodes");
    HashMap<String, ProgrammingLanguage> languagesMap = parseAndAccumulateLanguages(repositories, new HashSet<>());
    List<ProgrammingLanguage> languages = sortLanguagesBySize(languagesMap);

    resp.setContentType("application/json");
    resp.getWriter().println(new Gson().toJson(languages));
  }

  private String createQueryFromUsername(String username) throws IOException {
    Pattern usernamePattern = Pattern.compile("^[a-z0-9-]*$", Pattern.CASE_INSENSITIVE);
    if (!usernamePattern.matcher(username).find())
      throw new IOException("Username not valid");

    return createQuery(String.format("user(login: \"%s\")", username));
  }

  private String createQuery(String userPart) {
    String graphqlQuery = String.format(
        "{%s {repositories(isFork: false, first: 50) {nodes {databaseId languages(first: 5) {edges {size node {color name}}}}}}}",
        userPart
    );
    JsonObject query = new JsonObject();
    query.addProperty("query", graphqlQuery);
    return new Gson().toJson(query);
  }

  private JsonObject fetchGithubLanguagesData(String query) throws IOException {
    URL url = new URL("https://api.github.com/graphql");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("authorization", authorizationSecret);

    connection.setDoOutput(true);
    OutputStream outStream = connection.getOutputStream();
    OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);
    outStreamWriter.write(query);
    outStreamWriter.flush();
    outStreamWriter.close();
    outStream.close();

    connection.connect();

    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
      throw new IOException("GitHub answered with an " + connection.getResponseCode() + " error.");

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    return new Gson().fromJson(reader, JsonObject.class);
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
