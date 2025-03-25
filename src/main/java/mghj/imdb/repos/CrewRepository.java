package mghj.imdb.repos;

import mghj.imdb.entities.Crew;
import mghj.imdb.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CrewRepository extends JpaRepository<Crew, Integer> {
//    @Query("""
//        SELECT m FROM Movie m
//        JOIN Crew c ON m.tconst = c.tconst
//        JOIN Person p ON c.directors = c.writers AND c.directors = p.nconst
//        WHERE p.deathYear IS NULL
//    """)
//    List<Movie> findMoviesBySameDirectorAndWriterAlive();
}
