package mghj.imdb.bussiness;


import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
public class IMDbTitleFinder {
    private static final String BASE_URL = "https://datasets.imdbws.com/";
    private static final String DATA_DIR = "data/";
    private static final String CREW_FILE = "title.crew.tsv.gz";
    private static final String PEOPLE_FILE = "name.basics.tsv.gz";
    private static final String TITLE_FILE = "title.basics.tsv.gz";

    private static final String PRINCIPALS_FILE = "title.principals.tsv.gz";


    private final Map<String, Set<String>> directorWriterMap = new HashMap<>();
    private final Set<String> alivePeople = new HashSet<>();
    private final Map<String, String> movieTitleMap = new HashMap<>();

    private final Map<String, Set<String>> actorMovies = new HashMap<>();

    public IMDbTitleFinder() throws Exception {
        downloadFilesIfNeeded();
        loadPeopleData();
        loadMovieTitles();
        loadCrewData();

        loadActorMovies();
    }

    //2
    public List<String> findMoviesBySameDirectorWriter() throws Exception {


        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : directorWriterMap.entrySet()) {
            if (entry.getValue().size() == 1) { // Only one person is both writer & director
                String personId = entry.getValue().iterator().next();
                if (alivePeople.contains(personId)) {
                    String movieTitle = movieTitleMap.get(entry.getKey());
                    if (movieTitle != null) {
                        result.add(movieTitle); // Return movie title instead of ID
                    }
                }
            }
        }

        return result;
    }

    //3
    public List<String> findMoviesByActors(String actor1, String actor2) {
        Set<String> movies1 = actorMovies.getOrDefault(actor1, Collections.emptySet());
        Set<String> movies2 = actorMovies.getOrDefault(actor2, Collections.emptySet());

        movies1.retainAll(movies2); // Find common movies

        List<String> result = new ArrayList<>();
        for (String movieId : movies1) {
            String title = movieTitleMap.get(movieId);
            if (title != null) {
                result.add(title);
            }
        }
        return result;
    }

    private void downloadFilesIfNeeded() throws IOException {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();

        downloadFile(CREW_FILE);
        downloadFile(PEOPLE_FILE);
        downloadFile(TITLE_FILE);
    }

    private void downloadFile(String fileName) throws IOException {
        File file = new File(DATA_DIR, fileName);
        if (!file.exists()) {
            System.out.println("Downloading: " + fileName);
            FileUtils.copyURLToFile(new URL(BASE_URL + fileName), file);
        } else {
            System.out.println("File already exists: " + fileName);
        }
    }

    private void loadPeopleData() throws Exception {
        File file = new File(DATA_DIR + PEOPLE_FILE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip Header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String personId = parts[0];
                String deathYear = parts[3];

                if (deathYear.equals("\\N")) { // Still alive
                    alivePeople.add(personId);
                }
            }
        }
        System.out.println("Loaded " + alivePeople.size() + " alive people.");
    }

    private void loadMovieTitles() throws Exception {
        File file = new File(DATA_DIR + TITLE_FILE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip Header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String movieId = parts[0];
                String titleType = parts[1];

                // We only care about movies and TV shows, ignore episodes
                if (titleType.equals("movie") || titleType.equals("tvMovie")) {
                    movieTitleMap.put(movieId, parts[2]); // primaryTitle
                }
            }
        }
        System.out.println("Loaded " + movieTitleMap.size() + " movie titles.");
    }

    private void loadCrewData() throws Exception {
        File file = new File(DATA_DIR + CREW_FILE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip Header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String movieId = parts[0];
                String[] directors = parts[1].split(",");
                String[] writers = parts[2].split(",");

                Set<String> directorSet = new HashSet<>(Arrays.asList(directors));
                Set<String> writerSet = new HashSet<>(Arrays.asList(writers));

                directorSet.retainAll(writerSet); // Find common people

                if (!directorSet.isEmpty()) {
                    directorWriterMap.put(movieId, directorSet);
                }
            }
        }
        System.out.println("Loaded " + directorWriterMap.size() + " potential movies.");
    }

    private void loadActorMovies() throws Exception {
        File file = new File(DATA_DIR + PRINCIPALS_FILE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip Header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String movieId = parts[0];
                String personId = parts[2];
                String category = parts[3];

                if (category.equals("actor") || category.equals("actress")) {
                    actorMovies.computeIfAbsent(personId, k -> new HashSet<>()).add(movieId);
                }
            }
        }
        System.out.println("Loaded " + actorMovies.size() + " actors.");
    }
}
