package mghj.imdb.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "crew")
public class Crew {
    @Id
    private String tconst;
    private String directors; // Comma-separated director IDs
    private String writers;   // Comma-separated writer IDs
}
