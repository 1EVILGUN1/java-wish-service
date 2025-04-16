package pet.project.wish.repository;

import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pet.project.wish.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findFirstByNameAndPassword(String name, String password);

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    Flux<User> findByIdsCustom(@Param("ids") Flux<Long> ids);

}
