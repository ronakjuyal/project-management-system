package com.pixelforge.nexus.entity;

/**
 * Enum representing different user roles in the system
 * Each role has specific permissions and access levels
 */
public enum Role {
    /**
     * ADMIN: Full system access
     * - Can add/remove projects
     * - Can manage all user accounts
     * - Can upload documents for any project
     * - Can view all projects and documents
     */
    ADMIN,

    /**
     * PROJECT_LEAD: Project management access
     * - Can assign developers to their projects
     * - Can upload documents for their projects
     * - Can view projects they lead
     */
    PROJECT_LEAD,

    /**
     * DEVELOPER: Limited access
     * - Can view projects they are assigned to
     * - Can access documents for their assigned projects
     * - Cannot manage users or projects
     */
    DEVELOPER;

    /**
     * Check if the role has admin privileges
     * @return true if role is ADMIN
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if the role has project lead privileges
     * @return true if role is PROJECT_LEAD or ADMIN
     */
    public boolean canLeadProjects() {
        return this == PROJECT_LEAD || this == ADMIN;
    }

    /**
     * Check if the role can manage users
     * @return true if role is ADMIN
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }

    /**
     * Get display name for the role
     * @return formatted role name
     */
    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "Administrator";
            case PROJECT_LEAD -> "Project Lead";
            case DEVELOPER -> "Developer";
        };
    }
}