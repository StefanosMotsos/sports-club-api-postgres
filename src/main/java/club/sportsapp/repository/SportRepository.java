package club.sportsapp.repository;

import club.sportsapp.model.static_data.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportRepository extends JpaRepository<Sport, Long> {

    List<Sport> findAllByOrderByNameAsc();
}
