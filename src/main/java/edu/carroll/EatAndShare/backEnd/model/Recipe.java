package edu.carroll.EatAndShare.backEnd.model;


import jakarta.persistence.*;
import org.springframework.core.SpringVersion;

@Entity
@Table(name = "recipe")
public class Recipe {
    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    public User getUser() {
        return user;
    }

    @Column(name = "prep_time_mins", length = 1000)
    private Integer prepTimeMins;

    @Column(name = "cooking_time_mins", length = 1000)
    private Integer cookTimeMins;

    @Column
    private String difficulty;

    @Column(nullable = false, length = 1000)
    private String imgURL;

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPrepTimeMins() {
        return prepTimeMins;
    }

    public void setPrepTimeMins(Integer prepTimeMins) {
        this.prepTimeMins = prepTimeMins;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public Integer getCookTimeMins() {
        return cookTimeMins;
    }

    public void setCookTimeMins(Integer cookTimeMins) {
        this.cookTimeMins = cookTimeMins;
    }

    @Column(unique = true)

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
