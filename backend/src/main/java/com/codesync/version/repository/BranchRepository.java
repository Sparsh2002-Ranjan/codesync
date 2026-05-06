package com.codesync.version.repository;

import com.codesync.version.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String> {
    List<Branch> findByProjectId(String projectId);
    Optional<Branch> findByProjectIdAndName(String projectId, String name);
    boolean existsByProjectIdAndName(String projectId, String name);
}
