package vn.hoidanit.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.authservice.domain.Subscriber;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT s FROM Subscriber s JOIN FETCH s.skills sk WHERE sk.id IN :skillIds AND s.active = true")
    List<Subscriber> findActiveSubscribersBySkillIds(@Param("skillIds") List<Long> skillIds);

    @Query("SELECT s FROM Subscriber s LEFT JOIN FETCH s.skills WHERE s.id = :id")
    Optional<Subscriber> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT s FROM Subscriber s LEFT JOIN FETCH s.skills WHERE s.email = :email")
    Optional<Subscriber> findByEmailWithSkills(@Param("email") String email);
}