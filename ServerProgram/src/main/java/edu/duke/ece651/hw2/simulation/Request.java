package edu.duke.ece651.hw2.simulation;

/**
 * Represents a production request in the simulation.
 */
public class Request {
    private int id;
    private Recipe recipe;
    private Building requestor;
    private boolean isUserRequest;
    private int timeRequested;
    private RequestStatus status;

    /**
     * Constructs a Request.
     *
     * @param id            unique request identifier.
     * @param recipe        the recipe to be produced.
     * @param requestor     the building that requested this production.
     * @param isUserRequest indicates if this is a user-initiated request.
     * @param timeRequested the time step when this request was made.
     */
    public Request(int id, Recipe recipe, Building requestor, boolean isUserRequest, int timeRequested) {
        this.id = id;
        this.recipe = recipe;
        this.requestor = requestor;
        this.isUserRequest = isUserRequest;
        this.timeRequested = timeRequested;
        this.status = RequestStatus.WAITING_FOR_INGREDIENTS;
    }

    /**
     * Gets the request ID.
     *
     * @return the request ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the recipe associated with this request.
     *
     * @return the recipe.
     */
    public Recipe getRecipe() {
        return recipe;
    }

    /**
     * Gets the building that made this request.
     *
     * @return the requestor building.
     */
    public Building getRequestor() {
        return requestor;
    }

    /**
     * Checks if this is a user request.
     *
     * @return true if this is a user request, false otherwise.
     */
    public boolean isUserRequest() {
        return isUserRequest;
    }

    /**
     * Gets the time step when this request was made.
     *
     * @return the time step.
     */
    public int getTimeRequested() {
        return timeRequested;
    }

    /**
     * Gets the current status of this request.
     *
     * @return the request status.
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of this request.
     *
     * @param status the new status.
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Checks if this request is completed.
     *
     * @return true if the request is completed, false otherwise.
     */
    public boolean completed() {
        return status == RequestStatus.COMPLETED;
    }
}