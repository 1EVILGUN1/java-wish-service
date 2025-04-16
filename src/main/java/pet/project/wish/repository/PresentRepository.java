package pet.project.wish.repository;

import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pet.project.wish.model.Present;
import reactor.core.publisher.Flux;

@Repository
public interface PresentRepository extends ReactiveCrudRepository<Present, Long> {
    @Query("SELECT * FROM presents WHERE id IN (:ids)")
    Flux<Present> findByIdsCustom(@Param("ids") Flux<Long> ids);

}
