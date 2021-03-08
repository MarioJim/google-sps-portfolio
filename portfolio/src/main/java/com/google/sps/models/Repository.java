package com.google.sps.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class Repository {
  private final long databaseId;
  private final ArrayList<ProgrammingLanguage> languages;

  public Repository(JsonObject repo) {
    databaseId = repo.get("databaseId").getAsLong();
    languages = new ArrayList<>();
    for (JsonElement jsonElement : repo.getAsJsonObject("languages").getAsJsonArray("edges")) {
      if (!jsonElement.getAsJsonObject().getAsJsonObject("node").get("color").isJsonNull()) {
        languages.add(new ProgrammingLanguage(jsonElement.getAsJsonObject()));
      }
    }
  }

  public long getDatabaseId() {
    return databaseId;
  }

  public ArrayList<ProgrammingLanguage> getLanguages() {
    return languages;
  }
}
