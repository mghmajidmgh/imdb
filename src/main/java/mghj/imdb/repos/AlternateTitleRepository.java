package mghj.imdb.repos;

import mghj.imdb.entities.AlternateTitle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternateTitleRepository extends JpaRepository<AlternateTitle, Integer> {
}
