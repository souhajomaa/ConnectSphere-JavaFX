package com.example.projetjavafx.root.jobApplications;

import javafx.beans.property.*;

public class Application {
    private final IntegerProperty applicationId = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty appliedAt = new SimpleStringProperty();
    private final IntegerProperty rewarded = new SimpleIntegerProperty();
    private final IntegerProperty jobId = new SimpleIntegerProperty();
    private final StringProperty coverLetter = new SimpleStringProperty();
    private final StringProperty resumePath = new SimpleStringProperty();
    private final IntegerProperty coverRating = new SimpleIntegerProperty();

    public Application(int applicationId, int userId, String status, String appliedAt,
                       int rewarded, int jobId, String coverLetter, String resumePath) {
        this.applicationId.set(applicationId);
        this.userId.set(userId);
        this.status.set(status);
        this.appliedAt.set(appliedAt);
        this.rewarded.set(rewarded);
        this.jobId.set(jobId);
        this.coverLetter.set(coverLetter);
        this.resumePath.set(resumePath);
    }

    // Property accessors
    public IntegerProperty applicationIdProperty() { return applicationId; }
    public int getApplicationId() { return applicationId.get(); }

    public IntegerProperty userIdProperty() { return userId; }
    public int getUserId() { return userId.get(); }

    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public StringProperty appliedAtProperty() { return appliedAt; }
    public String getAppliedAt() { return appliedAt.get(); }

    public IntegerProperty rewardedProperty() { return rewarded; }
    public int getRewarded() { return rewarded.get(); }

    public IntegerProperty jobIdProperty() { return jobId; }
    public int getJobId() { return jobId.get(); }

    public StringProperty coverLetterProperty() { return coverLetter; }
    public String getCoverLetter() { return coverLetter.get(); }

    public StringProperty resumePathProperty() { return resumePath; }
    public String getResumePath() { return resumePath.get(); }

    // Cover Rating additions
    public IntegerProperty coverRatingProperty() { return coverRating; }
    public int getCoverRating() { return coverRating.get(); }
    public void setCoverRating(int rating) { coverRating.set(rating); }
}