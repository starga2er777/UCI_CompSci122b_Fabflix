package org.uci;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

@WebServlet(name = "MainPageServlet", urlPatterns = "/api/mainpg")
public class MainPageServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-slave");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (PrintWriter out = response.getWriter(); Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "with top_16_movies as (select *\n" +
                    "                       from movies\n" +
                    "                                join ratings r on movies.id = r.movieId\n" +
                    "                       order by numVotes desc, rating desc\n" +
                    "                       limit 16)\n" +
                    "select top_16_movies.id              as id,\n" +
                    "       title,\n" +
                    "       year,\n" +
                    "       group_concat(distinct g.name) as genres,\n" +
                    "       posterURL,\n" +
                    "       rating\n" +
                    "from top_16_movies\n" +
                    "         join genres_in_movies gim on id = gim.movieId\n" +
                    "         join genres g on g.id = gim.genreId\n" +
                    "         join poster p on gim.movieId = p.movieId\n" +
                    "group by id, numVotes, p.posterURL, rating\n" +
                    "order by numVotes desc, rating desc;";

            // Declare statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Perform query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {

                String movieID = rs.getString("id");
                String title = rs.getString("title");
                String poster = rs.getString("posterURL");
                int year = rs.getInt("year");
                JsonArray genres = new JsonArray();
                Arrays.stream(rs.getString("genres").split(",")).forEach(genres::add);
                float rating = rs.getFloat("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieID);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("poster", poster);
                jsonObject.add("genres", genres);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);


        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
