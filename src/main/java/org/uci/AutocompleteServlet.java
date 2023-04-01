package org.uci;

import org.uci.dto.Movie;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(){
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-slave");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){

        HttpSession session = request.getSession();
        response.setContentType("application/json");
        String query = request.getParameter("query");



        try(PrintWriter out = response.getWriter(); Connection connection = dataSource.getConnection()) {

            JsonArray jsonArray;
            // autocomplete only when user has inputted more than 3 characters
            // otherwise show history
            if(query == null || query.length() < 3 || query.trim().isEmpty()) {
                jsonArray = (JsonArray) session.getAttribute("MovieHistory");
                if(jsonArray == null){
                    jsonArray = new JsonArray();
                    session.setAttribute("MovieHistory", jsonArray);
                }
            } else{
                jsonArray = new JsonArray();
                // search movies with titles that contain query keywords
                String ti = Arrays.stream(query.split(" ")).map(s -> "+" + s + "*").collect(Collectors.joining(" "));
                String sql = "select * from movies\n" +
                        " where Match(title) Against ('" + ti
                        + "' In BOOLEAN Mode) or (SOUNDEX(title) = SOUNDEX('" + query + "')) limit 10;";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();


                while(resultSet.next()){

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("value", resultSet.getString("title"));

                    // record movie id
                    JsonObject additionalInfo = new JsonObject();
                    additionalInfo.addProperty("movieId", resultSet.getString("id"));
                    jsonObject.add("data", additionalInfo);

                    // {"title": movie_title, "data": {"movieId": movie_id}}
                    jsonArray.add(jsonObject);
                }
                resultSet.close();
                preparedStatement.close();
            }
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
