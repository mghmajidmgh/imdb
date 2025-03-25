package mghj.imdb.controllers;

import mghj.imdb.StartupRunner;
import mghj.imdb.bussiness.IMDbTitleFinder;
import mghj.imdb.bussiness.RequestCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
public class Home {

    @Autowired
    private StartupRunner startupRunner;

    @Autowired
    private IMDbTitleFinder titleFinder;

    @Autowired
    private RequestCounterService requestCounterService;

    @GetMapping("/2")
    public List<String> getMoviesBySameDirectorWriter() {
        return startupRunner.getMoviesBySameDirectorWriter();
    }

    @GetMapping("/3")
    public List<String> getMoviesWithTwoActors(
            @RequestParam String actor1,
            @RequestParam String actor2) throws Exception {

        return titleFinder.findMoviesWithTwoActors(actor1, actor2);
    }

    @GetMapping("/4")
    public Map<Integer, String> getBestMoviesByGenre(@RequestParam String genre) throws IOException {
        return titleFinder.findBestMoviesByGenre(genre);
    }

    @GetMapping("/5")
    public int getRequestCount() {
        return requestCounterService.getRequestCount();
    }
}
