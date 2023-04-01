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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddMoviePoster {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        String loginUser = "root";
        String loginPasswd = "Xuchunji1711";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();


        //calling stored procedure to insert
        try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {

            connection.setAutoCommit(false);

            int cnt = 0;

            //insert movies by calling stored procedure
            String sql = "select * from movies where id not like 'tt0%';";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery(sql);

            sql = "update poster set posterURL = ?, overview = ? where movieId = ?;";
            preparedStatement = connection.prepareStatement(sql);

            while (resultSet.next()) {



                String Title = resultSet.getString("title");
                String MovieId = resultSet.getString("id");
                String Year = resultSet.getString("year");

                preparedStatement.setString(1, "N/A");
                preparedStatement.setString(2, "N/A");
                preparedStatement.setString(3, MovieId);

                String apiURL = "https://api.themoviedb.org/3/search/movie?api_key=44910f74d917dd9e37e43b9c0e5881fd&query=" + Title.replace(' ', '+');
                SearchResult searchResult = readUrl(apiURL);

                if (searchResult != null) {
                    for (Info result : searchResult.results) {
                        if (result.title != null)
                            if (!Objects.equals(result.title.toLowerCase(), Title.toLowerCase()))
                                continue;
                        if (result.release_date != null && result.release_date.length() > 4 && Year != null)
                            if (Math.abs(Integer.parseInt(result.release_date.substring(0, 4)) - Integer.parseInt(Year)) > 1) {
                                continue;
                            }
                        if (result.poster_path != null)
                            preparedStatement.setString(1, result.poster_path);
                        if (result.overview != null)
                            preparedStatement.setString(2, result.overview);
                        break;
                    }
                }
                try {
                    cnt ++;
                    preparedStatement.addBatch();
                    if(cnt >= 500){
                        System.out.println("update 500 records");
                        preparedStatement.executeBatch();
                        cnt = 0;
                    }
                } catch (SQLException e) {
                }
            }
            try {
                preparedStatement.addBatch();
                if(cnt >= 500){
                    preparedStatement.executeBatch();
                    cnt = 0;
                }
            } catch (SQLException e) {
            }
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + "ms");
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