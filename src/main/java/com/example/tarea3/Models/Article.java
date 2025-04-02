package com.example.tarea3.Models;

import java.util.List;

public class Article {
    private String id;
    private String title;
    private List<String> authors;
    private String publicationName;
    private String publicationDate;
    private String doi;
    private String url;
    private String abstract_;
    private int citedByCount;
    private String openAccess;
    
    // Constructor vacío
    public Article() {
    }
    
    // Constructor con parámetros
    public Article(String id, String title, List<String> authors, String publicationName, 
                String publicationDate, String doi, String url, String abstract_, 
                int citedByCount, String openAccess) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.publicationName = publicationName;
        this.publicationDate = publicationDate;
        this.doi = doi;
        this.url = url;
        this.abstract_ = abstract_;
        this.citedByCount = citedByCount;
        this.openAccess = openAccess;
    }
    
    // Getters y setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<String> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    
    public String getPublicationName() {
        return publicationName;
    }
    
    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }
    
    public String getPublicationDate() {
        return publicationDate;
    }
    
    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    public String getDoi() {
        return doi;
    }
    
    public void setDoi(String doi) {
        this.doi = doi;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getAbstract() {
        return abstract_;
    }
    
    public void setAbstract(String abstract_) {
        this.abstract_ = abstract_;
    }
    
    public int getCitedByCount() {
        return citedByCount;
    }
    
    public void setCitedByCount(int citedByCount) {
        this.citedByCount = citedByCount;
    }
    
    public String getOpenAccess() {
        return openAccess;
    }
    
    public void setOpenAccess(String openAccess) {
        this.openAccess = openAccess;
    }
}