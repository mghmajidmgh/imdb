package mghj.imdb.bussiness;


import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

record People(String name,boolean isAlive){};

@Service
public class IMDbTitleFinder {
    private static final String BASE_URL = "https://datasets.imdbws.com/";
    private static final String DATA_DIR = "data/";
    private static final String CACHE_DIR = "cache/";

    private static final String CREW_FILE = "title.crew.tsv.gz";
    private static final String PEOPLE_FILE = "name.basics.tsv.gz";
    private static final String TITLE_FILE = "title.basics.tsv.gz";
    private static final String RATINGS_FILE = "title.ratings.tsv.gz";

    private static final String PRINCIPALS_FILE = "title.principals.tsv.gz";

    private static final String DIRECTOR_WRITER_CACHE = CACHE_DIR + "DIRECTOR_WRITER.ser";
    private static final String ALIVE_PEOPLE_CACHE = CACHE_DIR + "ALIVE_PEOPLE.ser";
    private static final String MOVIE_TITLE_CACHE = CACHE_DIR + "movie_titles.ser";


    private static Map<String, Set<String>> directorWriterMap;

    private Map<String, String> actorNameToIdMap = new HashMap<>();
    private static  Map<String, String> movieTitleMap  ;

    public IMDbTitleFinder() throws Exception {

        downloadFilesIfNeeded();
        //loadPeopleData();

        movieTitleMap=new HashMap<>();
        loadMovieTitles();

        loadActorNames();
        //loadCrewData();
        //loadActorMovies();


    }

    //2
    public List<String> findMoviesBySameDirectorWriter() throws Exception {
        Map<String, String> directorWriterMovies = new HashMap<>();

        System.out.println("Processing: " + CREW_FILE);
        int crewCounter = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DATA_DIR + CREW_FILE)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                crewCounter++;
                if (crewCounter % 1_000_000 == 0) System.out.println("Processed " + crewCounter + " lines...");

                String[] parts = line.split("\t");
                String movieId = parts[0];

                List<String> directors = Arrays.asList(parts[1].split(","));
                List<String> writers = Arrays.asList(parts[2].split(","));

                if (directors.size() > 1 || writers.size() > 1) continue; // Skip if multiple directors or writers

                if (directors.equals(writers)) { // Same person is both
                    directorWriterMovies.put(directors.get(0), movieId);
                }
            }
        }

        // Step 2: Check if the person is alive while iterating `name.basics.tsv.gz`
        System.out.println("Processing: " + PEOPLE_FILE);
        int peopleCounter = 0;
        List<String> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DATA_DIR + PEOPLE_FILE)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                peopleCounter++;
                if (peopleCounter % 1_000_000 == 0) System.out.println("Processed " + peopleCounter + " lines...");

                String[] parts = line.split("\t");
                String personId = parts[0];
                String deathYear = parts[3];

                if (directorWriterMovies.containsKey(personId) && deathYear.equals("\\N")) {
                    String movieTitle = movieTitleMap.get(directorWriterMovies.get(personId));
                    if (movieTitle != null) {
                        result.add(movieTitle);
                    }
                }
            }
        }

        return result;
    }
    private static List<String> fastSplit(String line) {
        List<String> tokens = new ArrayList<>();
        int start = 0;
        int end;

        while ((end = line.indexOf('\t', start)) != -1) {
            tokens.add(line.substring(start, end));
            start = end + 1;
        }
        tokens.add(line.substring(start)); // Add last token
        return tokens;
    }



    //3
    public List<String> findMoviesWithTwoActors(String actorName1, String actorName2) throws Exception {
        // Convert actor names to lowercase for case-insensitive lookup
        actorName1 = actorName1.toLowerCase();
        actorName2 = actorName2.toLowerCase();

        // Get actor IDs from name
        String actorId1 = actorNameToIdMap.get(actorName1);
        String actorId2 = actorNameToIdMap.get(actorName2);

        if (actorId1 == null || actorId2 == null) {
            throw new IllegalArgumentException("Actor(s) not found: " + actorName1 + " or " + actorName2);
        }

        System.out.println("Searching movies for actors: " + actorName1 + " (" + actorId1 + "), " + actorName2 + " (" + actorId2 + ")");

        // Find movies both actors played in
        return findMoviesWithTwoActorIds(actorId1, actorId2);
    }

    public List<String> findMoviesWithTwoActorIds(String actorId1, String actorId2) throws Exception {
        File file = new File(DATA_DIR + PRINCIPALS_FILE);
        Set<String> actor1Movies = new HashSet<>();
        Set<String> actor2Movies = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip Header

            int counter=0;
            while ((line = reader.readLine()) != null) {

                counter++;
                if (counter % 1_000_000 == 0) System.out.println("Processed " + counter + " lines...");

                String[] parts = line.split("\t");
                String movieId = parts[0];
                String personId = parts[2];

                if (personId.equals(actorId1)) {
                    actor1Movies.add(movieId);
                } else if (personId.equals(actorId2)) {
                    actor2Movies.add(movieId);
                }
            }
        }

        // Find common movies
        actor1Movies.retainAll(actor2Movies);

        // Convert movie IDs to Titles
        List<String> result = new ArrayList<>();
        for (String movieId : actor1Movies) {
            String title = movieTitleMap.get(movieId);
            if (title != null) {
                result.add(title);
            }
        }

        return result;
    }

    //4
    public Map<Integer, String> findBestMoviesByGenre(String genre) throws IOException {
        genre = genre.toLowerCase(); // Normalize genre for case-insensitive search

        Map<String, Integer> movieYears = new HashMap<>();  // Movie ID → Year
        Map<String, Double> movieRatings = new HashMap<>(); // Movie ID → Rating
        Map<String, Integer> movieVotes = new HashMap<>();  // Movie ID → Number of votes

        System.out.println("Loading movies with genre: " + genre);

        // Step 1: Load movies of the given genre
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DATA_DIR + TITLE_FILE)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header
            int counter = 0;

            while ((line = reader.readLine()) != null) {
                counter++;
                if (counter % 1_000_000 == 0) System.out.println("Processed " + counter + " lines...");

                String[] parts = line.split("\t");
                if (parts.length < 9) continue;

                String movieId = parts[0];
                String titleType = parts[1];
                String yearStr = parts[5];
                String genres = parts[8].toLowerCase();

                if (!titleType.equals("movie") || !genres.contains(genre) || yearStr.equals("\\N")) continue;

                int year = Integer.parseInt(yearStr);
                movieYears.put(movieId, year);
            }
        }

        // Step 2: Load ratings for those movies
        System.out.println("Loading ratings...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DATA_DIR + RATINGS_FILE)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header
            int counter = 0;

            while ((line = reader.readLine()) != null) {
                counter++;
                if (counter % 500_000 == 0) System.out.println("Processed " + counter + " ratings...");

                String[] parts = line.split("\t");
                if (parts.length < 3) continue;

                String movieId = parts[0];
                double rating = Double.parseDouble(parts[1]);
                int votes = Integer.parseInt(parts[2]);

                if (movieYears.containsKey(movieId)) {
                    movieRatings.put(movieId, rating);
                    movieVotes.put(movieId, votes);
                }
            }
        }

        // Step 3: Find best movie per year
        System.out.println("Finding best movie per year...");
        Map<Integer, String> bestMoviesByYear = new HashMap<>();

        for (String movieId : movieRatings.keySet()) {
            int year = movieYears.get(movieId);
            double rating = movieRatings.get(movieId);
            int votes = movieVotes.get(movieId);

            String currentBestMovieId = bestMoviesByYear.get(year);
            Double currentBestRating = (currentBestMovieId != null) ? movieRatings.get(currentBestMovieId) : null;
            Integer currentBestVotes = (currentBestMovieId != null) ? movieVotes.get(currentBestMovieId) : null;

            if (currentBestRating == null || rating > currentBestRating ||
                    (rating == currentBestRating && votes > currentBestVotes)) {

                bestMoviesByYear.put(year, movieTitleMap.get(movieId)); // Store movie title instead of ID
            }
        }


        return bestMoviesByYear;
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

    private void loadActorNames() throws IOException {
        System.out.println("Loading actor names...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DATA_DIR + PEOPLE_FILE)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");

                if (parts.length < 2) continue;

                String actorId = parts[0];
                String actorName = parts[1].toLowerCase(); // Convert to lowercase for case-insensitive search
                actorNameToIdMap.put(actorName, actorId);
            }
        }

        System.out.println("Loaded " + actorNameToIdMap.size() + " actor names.");
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






    private void saveCache() {
        saveObject(directorWriterMap, DIRECTOR_WRITER_CACHE );
        saveObject(actorNameToIdMap, ALIVE_PEOPLE_CACHE );
        saveObject(movieTitleMap, MOVIE_TITLE_CACHE);
        System.out.println("✅ Saved data to cache!");
    }

    private boolean loadCache() {
        directorWriterMap=loadObject(DIRECTOR_WRITER_CACHE);
        actorNameToIdMap=loadObject(ALIVE_PEOPLE_CACHE);
        movieTitleMap = loadObject(MOVIE_TITLE_CACHE);
        return directorWriterMap!=null && actorNameToIdMap!=null && movieTitleMap != null ;
    }

    private void saveObject(Object object, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(object);
        } catch (IOException e) {
            System.err.println("⚠️ Failed to save cache: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T loadObject(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("⚠️ Failed to load cache: " + e.getMessage());
            return null;
        }
    }
}
