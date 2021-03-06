package br.com.les.amstore.repository;

import br.com.les.amstore.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface States extends JpaRepository<State, Long> {
}
