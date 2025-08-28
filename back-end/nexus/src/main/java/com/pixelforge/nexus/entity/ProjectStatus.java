package com.pixelforge.nexus.entity;

import lombok.Getter;

/**
 * Enum representing project status
 */
@Getter
enum ProjectStatus {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

}