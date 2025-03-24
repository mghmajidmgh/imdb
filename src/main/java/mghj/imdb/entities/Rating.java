package mghj.imdb.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    private String tconst;
    private double averageRating;
    private int numVotes;
}
