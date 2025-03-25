package mghj.imdb;


import lombok.Getter;
import mghj.imdb.bussiness.FileDownloader;
import mghj.imdb.bussiness.IMDbTitleFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupRunner implements CommandLineRunner {


    @Autowired
    private IMDbTitleFinder titleFinder;


    private List<String> moviesBySameDirectorWriter; // Cached results

    public List<String> getMoviesBySameDirectorWriter() {
        return moviesBySameDirectorWriter;
    }

    @Override
    public void run(String... args) throws Exception {
        titleFinder.Init();

        System.out.println("Loading movies where director and writer are the same and alive...");
        moviesBySameDirectorWriter = titleFinder.findMoviesBySameDirectorWriter();
        System.out.println("Loaded " + moviesBySameDirectorWriter.size() + " movies.");



        System.out.println("http://localhost:8080/2");
        System.out.println("http://localhost:8080/3?actor1=Leonardo%20DiCaprio&actor2=Brad%20Pitt");
        System.out.println("http://localhost:8080/4?genre=Sci-Fi");
        System.out.println("http://localhost:8080/5");
    }

}
