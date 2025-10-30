package vn.hoidanit.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hoidanit.notificationservice.domain.Subscriber;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, JpaSpecificationExecutor<Subscriber>{
    boolean existsByEmail(String email);
    Subscriber findByEmail(String email);

    @Query("SELECT s FROM Subscriber s LEFT JOIN FETCH s.skillIds WHERE s.email = :email")
    Subscriber findByEmailWithSkills(@Param("email") String email);
}


