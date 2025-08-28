// Project Repository
package com.pixelforge.nexus.repository;

import com.pixelforge.nexus.entity.Project;
import com.pixelforge.nexus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Project entity
 * Provides project-specific data access methods
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find projects by lead user
     * @param lead Project lead
     * @return List of projects led by user
     */
    List<Project> findByLead(User lead);

    /**
     * Find active projects by lead
     * @param lead Project lead
     * @return List of active projects led by user
     */
    @Query("SELECT p FROM Project p WHERE p.lead = :lead AND p.status = 'ACTIVE'")
    List<Project> findActiveProjectsByLead(@Param("lead") User lead);

    /**
     * Find projects assigned to a developer
     * @param developer Developer user
     * @return List of projects assigned to developer
     */
    @Query("SELECT p FROM Project p JOIN p.assignedDevelopers d WHERE d = :developer")
    List<Project> findProjectsAssignedToDeveloper(@Param("developer") User developer);

    /**
     * Find active projects assigned to a developer
     * @param developer Developer user
     * @return List of active projects assigned to developer
     */
    @Query("SELECT p FROM Project p JOIN p.assignedDevelopers d WHERE d = :developer AND p.status = 'ACTIVE'")
    List<Project> findActiveProjectsAssignedToDeveloper(@Param("developer") User developer);

    /**
     * Find all active projects
     * @return List of active projects
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE'")
    List<Project> findActiveProjects();

    /**
     * Find projects by status
     * @param status Project status
     * @return List of projects with specified status
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    List<Project> findByStatus(@Param("status") String status);

    /**
     * Find overdue projects
     * @param currentDate Current date
     * @return List of overdue projects
     */
    @Query("SELECT p FROM Project p WHERE p.deadline < :currentDate AND p.status = 'ACTIVE'")
    List<Project> findOverdueProjects(@Param("currentDate") LocalDate currentDate);
}