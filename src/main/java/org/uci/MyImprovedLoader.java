package org.uci;


import com.google.gson.Gson;
import org.uci.dto.Genre;
import org.uci.dto.Movie;
import org.uci.dto.Star;
import org.uci.dto.StarsInMovies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;

public class MyImprovedLoader {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        // parse xml files
        MovieSAXParser spm = new MovieSAXParser();
        ActorSAXParser spa = new ActorSAXParser();
        CastSAXParser spc = new CastSAXParser();
        spm.parseDocument("/home/ubuntu/stanford-movies/mains243.xml");
        spa.parseDocument("/home/ubuntu/stanford-movies/actors63.xml");
        spc.parseDocument("/home/ubuntu/stanford-movies/casts124.xml");


        String loginUser = "root";
        String loginPasswd = "123456";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        //calling stored procedure to insert
        try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {
            connection.setAutoCommit(false);
            BufferedWriter out = new BufferedWriter(new FileWriter("report.txt"));

            //insert movies by calling stored procedure
            String sql = "call InsertMovies(?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for (Movie movie : spm.newMovies) {
               /* String apiURL = "https://api.themoviedb.org/3/search/movie?api_key=44910f74d917dd9e37e43b9c0e5881fd&query=" + movie.getTitle().replace(' ', '+');
                SearchResult searchResult = readUrl(apiURL);*/
                preparedStatement.setString(5, "N/A");
                preparedStatement.setString(6, "N/A");
                /*if (searchResult != null) {
                    for (Info result : searchResult.results) {
                        if (result.title != null)
                            if (!Objects.equals(result.title.toLowerCase(), movie.getTitle().toLowerCase()))
                                continue;
                        if (result.release_date != null && result.release_date.length() > 4)
                            if (Math.abs(Integer.parseInt(result.release_date.substring(0, 4)) - movie.getYear()) > 1) {
                                continue;
                            }
                        if (result.poster_path != null)
                            preparedStatement.setString(5, result.poster_path);
                        if (result.overview != null)
                            preparedStatement.setString(6, result.overview);
                        break;
                    }
                }*/
                try {
                    preparedStatement.setString(1, movie.getId());
                    preparedStatement.setString(2, movie.getTitle());
                    if (movie.getYear() != null) preparedStatement.setInt(3, movie.getYear());
                    else preparedStatement.setNull(3, Types.INTEGER);
                    preparedStatement.setString(4, movie.getDirector());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    out.write(e.getMessage() + " " + "(id: " + movie.getId() + ", Title:" + movie.getTitle() +
                            ", Year: " + movie.getYear() + ", Director: " + movie.getDirector() + ")\n");
                    System.out.println(e.getMessage() + " " + "(id: " + movie.getId() + ", Title:" + movie.getTitle() +
                            ", Year: " + movie.getYear() + ", Director: " + movie.getDirector() + ")");
                }
            }

            // insert genres
            sql = "call InsertGenres_in_Movies(?, ?);";
            preparedStatement = connection.prepareStatement(sql);

            for (Genre genre : spm.newGenres) {
                try {
                    preparedStatement.setString(1, genre.getMovieId());
                    preparedStatement.setString(2, genre.getName());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    out.write(e.getMessage() + "(movieId: " + genre.getMovieId() +
                            ", genre: " + genre.getName() + ")\n");
                    System.out.println(e.getMessage() + "(movieId: " + genre.getMovieId() +
                            ", genre: " + genre.getName() + ")");
                }
            }

            //insert stars
            sql = "call InsertStars(?, ?, ?);";
            preparedStatement = connection.prepareStatement(sql);
            int cnt = 0;
            for (Star star : spa.actors) {
                try {
                    cnt++;
                    String Id = "p3" + String.valueOf(cnt);
                    preparedStatement.setString(1, Id);
                    preparedStatement.setString(2, star.getName());
                    if (star.getBirthYear() != null) preparedStatement.setInt(3, star.getBirthYear());
                    else preparedStatement.setNull(3, Types.INTEGER);
                    boolean Info = preparedStatement.execute();
                    if (Info) {
                        out.write("Inserting Star: " + star.getName() + "(" +
                                star.getBirthYear() + ") has already existed\n");
                        System.out.println("Inserting Star: " + star.getName() + "(" +
                                star.getBirthYear() + ") has already existed");
                    }
                } catch (SQLException e) {
                    out.write(e.getMessage() + "(starName: " + star.getName() + ")\n");
                    System.out.println(e.getMessage() + "(starName: " + star.getName() + ")");
                }
            }

            //insert stars_in_movies
            sql = "call InsertStars_in_movies(?, ?)";
            preparedStatement = connection.prepareStatement(sql);

            for (StarsInMovies starsInMovies : spc.casts) {
                try {
                    preparedStatement.setString(1, starsInMovies.getStarName());
                    preparedStatement.setString(2, starsInMovies.getMovieId());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    out.write(e.getMessage() + " (starId: " + starsInMovies.getStarId() +
                            ", movieId: " + starsInMovies.getMovieId() + ")\n");
                    System.out.println(e.getMessage() + " (starId: " + starsInMovies.getStarId() +
                            ", movieId: " + starsInMovies.getMovieId() + ")");
                }
            }
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + "ms");
    }

}