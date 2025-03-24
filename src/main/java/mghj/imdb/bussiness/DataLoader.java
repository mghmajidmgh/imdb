package mghj.imdb.bussiness;
import mghj.imdb.entities.*;
import mghj.imdb.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Service
public class DataLoader {
    private static final String DATA_DIR = "data/";

    @Autowired private MovieRepository movieRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private CrewRepository crewRepository;
    @Autowired private PersonRepository personRepository;

    public void loadAllData() throws Exception {
//        title.basics.tsv.gz	Movie details (ID, title, genres, etc.)
//        title.akas.tsv.gz	Alternative movie titles in different languages
//        title.crew.tsv.gz	Directors and writers for each movie
//        title.episode.tsv.gz	Episodes and their parent series
//        title.principals.tsv.gz	Cast and crew members for each movie
//        title.ratings.tsv.gz	IMDb user ratings for movies
//        name.basics.tsv.gz	Actor, director, writer, and crew details

        loadMovies();
        loadRatings();
//        loadCrew();
//        loadPeople();
    }

    private void loadMovies() throws Exception {
        String filePath = DATA_DIR + "title.basics.tsv.gz";
        try (BufferedReader reader = getReader(filePath)) {
            String line;
            reader.readLine(); // Skip Header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");

                Movie movie = new Movie();
                movie.setTconst(Integer.parseInt( parts[0] ) );
                movie.setTitleType(parts[1]);
                movie.setPrimaryTitle(parts[2]);
                movie.setOriginalTitle(parts[3]);
                movie.setAdult(parts[4].equals("1"));
                movie.setStartYear(parseInt(parts[5]));
                movie.setEndYear(parseInt(parts[6]));
                movie.setRuntimeMinutes(parseInt(parts[7]));
                movie.setGenres(parts[8]);

                movieRepository.save(movie);
            }
        }
    }

    private void loadRatings() throws Exception {
        String filePath = DATA_DIR + "title.ratings.tsv.gz";
        try (BufferedReader reader = getReader(filePath)) {
            String line;
            reader.readLine(); // Skip Header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");

                Rating rating = new Rating();
                rating.setTconst(parts[0]);
                rating.setAverageRating(Double.parseDouble(parts[1]));
                rating.setNumVotes(Integer.parseInt(parts[2]));

                ratingRepository.save(rating);
            }
        }
    }

    private BufferedReader getReader(String filePath) throws Exception {
        return new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(filePath)), StandardCharsets.UTF_8));
    }

    private int parseInt(String value) {
        return value.matches("\\d+") ? Integer.parseInt(value) : 0;
    }
}
