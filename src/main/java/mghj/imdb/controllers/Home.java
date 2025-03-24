package mghj.imdb.controllers;

import mghj.imdb.entities.*;
import mghj.imdb.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class Home {

    @Autowired
    private CrewRepository crewRepository;

    @GetMapping("/2")
    public List<Movie> getMoviesWithSameDirectorWriterAlive() {
        return crewRepository.findMoviesBySameDirectorAndWriterAlive();
    }
}
