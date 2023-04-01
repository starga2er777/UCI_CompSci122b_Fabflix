package org.uci;


import com.google.gson.Gson;
import org.uci.dto.Genre;
import org.uci.dto.Movie;
import org.uci.dto.Star;
import org.uci.dto.StarsInMovies;

import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.List;

public class MyLoader{
    public static void main(String[] args) throws Exception{
        long startTime = System.currentTimeMillis();
        // parse xml files
        MovieSAXParser spm = new MovieSAXParser();
        ActorSAXParser spa = new ActorSAXParser();
        CastSAXParser spc = new CastSAXParser();
        spm.parseDocument("mains243.xml");
        spa.parseDocument("actors63.xml");
        spc.parseDocument("casts124.xml");


        String loginUser = "root";
        String loginPasswd = "Xuchunji1711";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        //calling stored procedure to insert
        try(Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)){
            connection.setAutoCommit(false);


            //insert movies by calling stored procedure
            String sql = "call InsertMovies(?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for(Movie movie : spm.newMovies){
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
                try{
                    preparedStatement.setString(1, movie.getId());
                    preparedStatement.setString(2, movie.getTitle());
                    if(movie.getYear() != null)preparedStatement.setInt(3, movie.getYear());
                    else preparedStatement.setNull(3, Types.INTEGER);
                    preparedStatement.setString(4, movie.getDirector());
                    boolean info = preparedStatement.execute();
                    if(info){
                        System.out.println("Inserting Movies: " + movie.getId() + "(" + movie.getTitle() + ", " +
                                movie.getYear() + ", " + movie.getDirector() + ") has already existed");
                    }
                } catch (SQLException e){
                    System.out.println(e.getMessage() + " " + preparedStatement);
                }
            }

            // insert genres
            sql = "call InsertGenres_in_Movies(?, ?);";
            preparedStatement = connection.prepareStatement(sql);

            for(Genre genre : spm.newGenres){
                try{
                    preparedStatement.setString(1, genre.getMovieId());
                    preparedStatement.setString(2, genre.getName());
                    boolean Info = preparedStatement.execute();
                    if(Info){
                        System.out.println("Inserting Genre: " + "MovieID(" +
                                genre.getMovieId() + ") not exist");
                    }
                } catch (SQLException e){
                    System.out.println(e.getMessage() + "(movieId: " + genre.getMovieId() +
                            ", genre: " + genre.getName() + ")");
                }
            }

            //insert stars
            sql = "call InsertStars(?, ?);";
            preparedStatement = connection.prepareStatement(sql);

            for(Star star : spa.actors){
                try{
                    preparedStatement.setString(1, star.getName());
                    if(star.getBirthYear() != null)preparedStatement.setInt(2, star.getBirthYear());
                    else preparedStatement.setNull(2, Types.INTEGER);
                    boolean Info = preparedStatement.execute();
                    if(Info){
                        System.out.println("Inserting Star: " + star.getName() + "(" +
                                star.getBirthYear() + ") has already existed");
                    }
                }catch (SQLException e){
                    System.out.println(e.getMessage() + "(starName: " + star.getName() + ")");
                }
            }

            //insert stars_in_movies
            sql = "call InsertStars_in_movies(?, ?)";
            preparedStatement = connection.prepareStatement(sql);

            for(StarsInMovies starsInMovies : spc.casts){
                try{
                    preparedStatement.setString(1, starsInMovies.getStarName());
                    preparedStatement.setString(2, starsInMovies.getMovieId());
                    boolean Info = preparedStatement.execute();
                    if(Info){
                        System.out.println("Inserting stars_in_movies: " + starsInMovies.getStarName() + " not exist");
                    }
                }catch (SQLException e){
                    System.out.println(e.getMessage() + " (starId: " + starsInMovies.getStarId() +
                            ", movieId: " + starsInMovies.getMovieId());
                }
            }
            connection.commit();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    private static SearchResult readUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            SearchResult result = new Gson().fromJson(reader, SearchResult.class);
            reader.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

class SearchResult {
    int total_results;
    List<Info> results;
}

class Info {
    String title;
    String release_date;
    String poster_path;
    String overview;
}