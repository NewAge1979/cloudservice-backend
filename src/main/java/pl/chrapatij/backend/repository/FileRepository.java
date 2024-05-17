package pl.chrapatij.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pl.chrapatij.backend.entity.File;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    Optional<File> findByNameAndUserIdAndIsDeleted(String name, Long userId, boolean isDeleted);

    Optional<File> findByHashAndUserIdAndIsDeleted(String hash, Long userId, boolean isDeleted);

    @Query(value = "Select * From files Where user_id = ?1 And is_deleted = false Order By id Desc Limit ?2", nativeQuery = true)
    List<File> findFilesByUserWithLimit(Long userId, int limit);

    @Modifying
    @Transactional
    @Query(value = "Update files Set is_deleted = true Where id = ?1", nativeQuery = true)
    void removeById(Long id);
}