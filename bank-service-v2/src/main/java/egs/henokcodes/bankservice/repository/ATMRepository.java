package egs.henokcodes.bankservice.repository;

import egs.henokcodes.bankservice.models.ATM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ATMRepository extends JpaRepository<ATM, Long> {
}
