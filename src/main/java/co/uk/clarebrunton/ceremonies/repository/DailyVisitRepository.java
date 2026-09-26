package co.uk.clarebrunton.ceremonies.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import co.uk.clarebrunton.ceremonies.model.DailyVisit;
public interface DailyVisitRepository extends JpaRepository<DailyVisit, LocalDate> { }
