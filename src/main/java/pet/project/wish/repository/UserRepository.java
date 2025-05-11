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
    Mono<User> findUserByNameAndPassword(String name, String password);

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    Flux<User> findByIdsCustom(@Param("ids") Flux<Long> ids);

    @Query("""
        SELECT u2.id, u2.name, u2.last_name, u2.password, u2.birthday, u2.friends_ids, u2.present_ids, u2.url
        FROM users u1
        JOIN users u2 ON u2.id = :friendId
        WHERE u1.id = :userId AND :friendId = ANY(u1.friends_ids)
        """)
    Mono<User> getFriend(Long userId, Long friendId);

    Mono<User> findFirstByName(String name);

}
