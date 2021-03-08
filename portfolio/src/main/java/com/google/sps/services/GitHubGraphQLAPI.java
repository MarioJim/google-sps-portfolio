package com.google.sps.services;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class GitHubGraphQLAPI {
  private final static String languagesQuery =
      "{%s {repositories(isFork: false, first: 50) {nodes {databaseId languages(first: 5) {edges {size node {color name}}}}}}}";

  private final String authorizationSecret;

  public GitHubGraphQLAPI() throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of("mjimenezvizcaino-sps-spring21", "github_graphql_api_key", "1");
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
      authorizationSecret = response.getPayload().getData().toStringUtf8();
    }
  }

  public String createLanguagesQueryFromUsername(String username) throws IOException {
    Pattern usernamePattern = Pattern.compile("^[a-z0-9-]*$", Pattern.CASE_INSENSITIVE);
    if (!usernamePattern.matcher(username).find())
      throw new IOException("Username not valid");

    return createLanguagesQuery(String.format("user(login: \"%s\")", username));
  }

  public String createLanguagesQuery(String userPart) {
    return String.format(languagesQuery, userPart);
  }

  public JsonObject fetchGraphQLQuery(String graphqlQuery) throws IOException {
    JsonObject jsonQuery = new JsonObject();
    jsonQuery.addProperty("query", graphqlQuery);
    String query = new Gson().toJson(jsonQuery);

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
    return new Gson().fromJson(reader, JsonObject.class).getAsJsonObject("data");
  }
}
