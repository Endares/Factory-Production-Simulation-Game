package edu.duke.ece651.hw2.simulation;

/**
 * Represents the status of a production request.
 */
public enum RequestStatus {
    WAITING_FOR_INGREDIENTS,
    READY,
    IN_PROGRESS,
    COMPLETED
}