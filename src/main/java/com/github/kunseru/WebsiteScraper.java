package com.github.kunseru;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WebsiteScraper {
    private static final int totalPages = 387;
    private static final String progressFileName = "scraping_progress.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        if (args.length != 4 || !args[0].equals("-year") || !args[2].equals("-week")) {
            String jarName = new java.io.File(WebsiteScraper.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            System.out.println("Please start this application with arguments java " + jarName + " -year <year> -week <week>");
            System.exit(1); // Exit with an error code
        }

        int year = Integer.parseInt(args[1]);
        int week = Integer.parseInt(args[3]);

        String baseUrl = "https://tracker.gg/bloodhunt/leaderboards/weekly/all/Kills?page=%d&week=" + year + "_" + week + "&playlist=Bloodhunt_Casual";

        List<Integer> killsList = new ArrayList<>();

        int currentPage;

        Progress progress = loadProgress();

        if (progress != null) {
            currentPage = progress.getCurrentPage();
            killsList = progress.getKillsList();
        } else {
            currentPage = 1;
        }

        try {
            for (; currentPage <= totalPages; currentPage++) {
                String url = String.format(baseUrl, currentPage);

                try {
                    Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3").get();

                    Elements trElements = document.select("tbody tr");

                    for (Element tr : trElements) {
                        Elements tdElements = tr.select("td");

                        if (tdElements.size() >= 3) {
                            String username = tdElements.get(1).text();
                            String killsStr = tdElements.get(3).text();

                            try {
                                int kills = Integer.parseInt(killsStr.replace(",", ""));
                                killsList.add(kills);
                                System.out.println("Username: " + username + "\t Kills: " + kills);
                            } catch (NumberFormatException e) {
                                System.out.println("Error parsing kills for " + username);
                            }
                        }
                    }

                    System.out.println("Page " + currentPage + " scraped.\n");

                } catch (HttpStatusException e) {
                    System.out.println("Rate limit error encountered. Saving progress.");
                    break;
                }
            }
            saveProgress(currentPage, killsList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Progress loadProgress() {
        try (FileReader fileReader = new FileReader(progressFileName)) {
            try (Reader reader = new BufferedReader(fileReader)) {
                Progress progress = gson.fromJson(reader, Progress.class);

                if (progress != null && progress.getKillsList() == null) {
                    progress.setKillsList(new ArrayList<>());
                }

                return progress;
            }
        } catch (IOException ignore) {
            return null;
        }
    }

    private static void saveProgress(int currentPage, List<Integer> killsList) {
        Progress progress = new Progress(currentPage, killsList);
        try (FileWriter writer = new FileWriter(progressFileName)) {
            gson.toJson(progress, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int calculateTotalKills(List<Integer> killsList) {
        int totalKills = 0;
        for (int kills : killsList) {
            totalKills += kills;
        }
        return totalKills;
    }
}