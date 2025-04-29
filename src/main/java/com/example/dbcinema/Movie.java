package org.example.cinemadb;

public class Movie {
    private final int movieId;
    private final String name;
    private final String language;
    private final int duration;
    private final String genre;

    public Movie(int movieId, String name, String language, int duration, String genre) {
        this.movieId = movieId;
        this.name = name;
        this.language = language;
        this.duration = duration;
        this.genre = genre;
    }




    // Getters
    public int getMovieId() { return movieId; }
    public String getName() { return name; }
    public String getLanguage() { return language; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }

    @Override
    public String toString() { return name; }
}

