package com.codecanvas.model;

public class AlgorithmItem {
    private String id;
    private String name;
    private String category;
    private String timeComplexity;
    private String spaceComplexity;
    private String description;
    private String githubRepoOwner;
    private String githubRepoName;
    private String githubDocPath;
    private String githubCodePath;
    private String videoFileName;

    public AlgorithmItem() {
    }

    public AlgorithmItem(String id, String name, String category, String timeComplexity, 
                         String spaceComplexity, String description, String githubRepoOwner, 
                         String githubRepoName, String githubDocPath, String githubCodePath, 
                         String videoFileName) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.description = description;
        this.githubRepoOwner = githubRepoOwner;
        this.githubRepoName = githubRepoName;
        this.githubDocPath = githubDocPath;
        this.githubCodePath = githubCodePath;
        this.videoFileName = videoFileName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTimeComplexity() { return timeComplexity; }
    public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }

    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGithubRepoOwner() { return githubRepoOwner; }
    public void setGithubRepoOwner(String githubRepoOwner) { this.githubRepoOwner = githubRepoOwner; }

    public String getGithubRepoName() { return githubRepoName; }
    public void setGithubRepoName(String githubRepoName) { this.githubRepoName = githubRepoName; }

    public String getGithubDocPath() { return githubDocPath; }
    public void setGithubDocPath(String githubDocPath) { this.githubDocPath = githubDocPath; }

    public String getGithubCodePath() { return githubCodePath; }
    public void setGithubCodePath(String githubCodePath) { this.githubCodePath = githubCodePath; }

    public String getVideoFileName() { return videoFileName; }
    public void setVideoFileName(String videoFileName) { this.videoFileName = videoFileName; }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
