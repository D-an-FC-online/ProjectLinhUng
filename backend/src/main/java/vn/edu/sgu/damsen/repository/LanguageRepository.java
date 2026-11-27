package vn.edu.sgu.damsen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.damsen.domain.Language;

public interface LanguageRepository extends JpaRepository<Language, String> {
}
