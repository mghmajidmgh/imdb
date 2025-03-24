package mghj.imdb.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "episodes")
public class Episode {
    @Id
    private String tconst;
    private String parentTconst;
    private int seasonNumber;
    private int episodeNumber;
}
