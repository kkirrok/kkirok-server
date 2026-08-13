package com.kkirok.server.domain.terms.dao;

import com.kkirok.server.domain.terms.domain.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    @Query("SELECT t FROM Terms t WHERE t.version = (SELECT MAX(t2.version) FROM Terms t2 WHERE t2.type = t.type)")
    List<Terms> findAllLatest();
}
