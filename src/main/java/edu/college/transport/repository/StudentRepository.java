package edu.college.transport.repository;

import edu.college.transport.entity.Student;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserUsername(String username);
    boolean existsByRegisterNumber(String registerNumber);
    @Query("select s from Student s join s.user u where lower(s.registerNumber) like lower(concat('%', :query, '%')) or lower(u.fullName) like lower(concat('%', :query, '%')) or lower(u.username) like lower(concat('%', :query, '%'))")
    Page<Student> search(@Param("query") String query, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from Student s where s.id = :id") Optional<Student> findLockedById(@Param("id") Long id);
}
