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
    private static final String UrlImdb = "https://datasets.imdbws.com/";
    private static final String DataDir = "data/";

    private static final String CrewFileName = "title.crew.tsv.gz";
    private static final String PeopleFileName = "name.basics.tsv.gz";
    private static final String TitleFileName = "title.basics.tsv.gz";
    private static final String RatingFileName = "title.ratings.tsv.gz";
    private static final String PrincipleFileName = "title.principals.tsv.gz";


    private Map<String, String> actorNameToIdMap  ;
    private static  Map<String, String> movieTitleMap  ;

    public IMDbTitleFinder() {
        movieTitleMap=new HashMap<>();
        actorNameToIdMap = new HashMap<>();
    }

    public void Init() throws Exception {

        downloadFilesIfNeeded();
        //loadPeopleData();


        loadMovieTitles();

        loadActorNames();
        //loadCrewData();
        //loadActorMovies();
    }

    //2
    public List<String> findMoviesBySameDirectorWriter() throws Exception {
        Map<String, String> directorWriterMovies = new HashMap<>();

        System.out.println("Processing: " + CrewFileName);
        int counter = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DataDir + CrewFileName)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                counter++;
                if (counter % 1_000_000 == 0) System.out.println("Processed " + counter + " lines...");

                String[] parts = line.split("\t");
                String movieId = parts[0];

                List<String> directors = Arrays.asList(parts[1].split(","));
                List<String> writers = Arrays.asList(parts[2].split(","));

                if (directors.size() > 1 || writers.size() > 1) continue;

                if (directors.equals(writers)) {
                    directorWriterMovies.put(directors.get(0), movieId);
                }
            }
        }

        // Step 2: Check if the person is alive while iterating `name.basics.tsv.gz`
        System.out.println("Processing: " + PeopleFileName);
        int peopleCounter = 0;
        List<String> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DataDir + PeopleFileName)), StandardCharsets.UTF_8))) {

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

        return findMoviesWithTwoActorIds(actorId1, actorId2);
    }

    public List<String> findMoviesWithTwoActorIds(String actorId1, String actorId2) throws Exception {
        File file = new File(DataDir + PrincipleFileName);
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

                String category = parts[3];
                if (!category.equals("actor") && !category.equals("actress")) {
                    continue;
                }

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
                new GZIPInputStream(new FileInputStream(DataDir + TitleFileName)), StandardCharsets.UTF_8))) {

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
                new GZIPInputStream(new FileInputStream(DataDir + RatingFileName)), StandardCharsets.UTF_8))) {

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
        File dir = new File(DataDir);
        if (!dir.exists()) dir.mkdirs();

        downloadFile(CrewFileName);
        downloadFile(PeopleFileName);
        downloadFile(TitleFileName);
        downloadFile( RatingFileName);
        downloadFile( PrincipleFileName);
    }

    private void downloadFile(String fileName) throws IOException {
        File file = new File(DataDir, fileName);
        if (!file.exists()) {
            System.out.println("Downloading: " + fileName);
            FileUtils.copyURLToFile(new URL(UrlImdb + fileName), file);
        } else {
            System.out.println("File already exists: " + fileName);
        }
    }

    private void loadActorNames() throws IOException {
        System.out.println("Loading actor names...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(DataDir + PeopleFileName)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip header

            int counter = 0;
            while ((line = reader.readLine()) != null) {
                counter++;
                if (counter % 500_000 == 0) System.out.println("Processed " + counter + " actors...");

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
        System.out.println("Loading Titles...");

        File file = new File(DataDir + TitleFileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // Skip Header

            int counter = 0;
            while ((line = reader.readLine()) != null) {
                counter++;
                if (counter % 500_000 == 0) System.out.println("Processed " + counter + " Titles...");

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



}
