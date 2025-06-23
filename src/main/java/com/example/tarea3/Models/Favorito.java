package com.example.tarea3.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
public class Favorito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "article_id", nullable = false)
    private String articleId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String authors;
    
    @Column(name = "publication_name", columnDefinition = "TEXT")
    private String publicationName;
    
    @Column(name = "publication_date")
    private String publicationDate;
    
    private String doi;
    
    @Column(columnDefinition = "TEXT")
    private String url;
    
    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractText;
    
    @Column(name = "cited_by_count")
    private Integer citedByCount = 0;
    
    @Column(name = "open_access")
    private String openAccess;
    
    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado = LocalDateTime.now();
    
    // Constructores
    public Favorito() {}
    
    public Favorito(Long usuarioId, String articleId, String title, String authors, 
                   String publicationName, String publicationDate, String doi, 
                   String url, String abstractText, Integer citedByCount, String openAccess) {
        this.usuarioId = usuarioId;
        this.articleId = articleId;
        this.title = title;
        this.authors = authors;
        this.publicationName = publicationName;
        this.publicationDate = publicationDate;
        this.doi = doi;
        this.url = url;
        this.abstractText = abstractText;
        this.citedByCount = citedByCount;
        this.openAccess = openAccess;
        this.fechaAgregado = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthors() {
        return authors;
    }
    
    public void setAuthors(String authors) {
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
    
    public String getAbstractText() {
        return abstractText;
    }
    
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    
    public Integer getCitedByCount() {
        return citedByCount;
    }
    
    public void setCitedByCount(Integer citedByCount) {
        this.citedByCount = citedByCount;
    }
    
    public String getOpenAccess() {
        return openAccess;
    }
    
    public void setOpenAccess(String openAccess) {
        this.openAccess = openAccess;
    }
    
    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }
    
    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}