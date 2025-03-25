package mghj.imdb.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    private String tconst; // IMDB ID

    private String titleType;
    @Lob
    private String primaryTitle;
    @Lob
    private String originalTitle;
    private boolean isAdult;
    private int startYear;
    private int endYear;
    private int runtimeMinutes;
    private String genres;

    // Getters & Setters
}
