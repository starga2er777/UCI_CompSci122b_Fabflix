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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.stream.Collectors;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-slave");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id fr；om url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "select movies.id,\n" +
                    "       title,\n" +
                    "       year,\n" +
                    "       director,\n" +
                    "       group_concat(distinct g.name)                    as genres,\n" +
                    "       group_concat(distinct (concat(s.id, '|', s.name))) as stars,\n" +
                    "       rating,\n" +
                    "       posterURL,\n" +
                    "       overview,\n" +
                    "       price\n" +
                    "from movies\n" +
                    "         left join genres_in_movies gim on movies.id = gim.movieId\n" +
                    "         left join genres g on g.id = gim.genreId\n" +
                    "         left join stars_in_movies sim on movies.id = sim.movieId\n" +
                    "         left join stars s on sim.starId = s.id\n" +
                    "         left join ratings r on movies.id = r.movieId\n" +
                    "         join poster p on movies.id = p.movieId\n" +
                    "         join price_of_movies pom on movies.id = pom.movieId\n" +
                    "where p.movieId = ?\n" +
                    "group by movies.id, rating, posterURL, price, overview;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieID = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                String poster = rs.getString("posterURL");
                JsonArray genres = new JsonArray(), star_name = new JsonArray(), star_id = new JsonArray();
                if(rs.getString("genres") != null) {
                    Arrays.stream(rs.getString("genres").split(",")).forEach(genres::add);
                } else {
                    genres.add("N/A");
                }
                if(rs.getString("stars") != null) {
                    Arrays.stream(rs.getString("stars").split(","))
                            .map(str -> Arrays.stream(str.split("\\|")).collect(Collectors.toList()))
                            .forEach(lst -> {
                                star_id.add(lst.get(0));
                                star_name.add(lst.get(1));
                            });
                } else {
                    star_id.add("N/A");
                    star_name.add("N/A");
                }
                String rating = rs.getObject("rating") != null ? Float.toString(rs.getFloat("rating")) : "N/A";
                String overview = rs.getString("overview");
                double price = rs.getDouble("price");


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieID);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.add("genres", genres);
                jsonObject.add("star_id", star_id);
                jsonObject.add("star_name", star_name);
                jsonObject.addProperty("rating", rating);
                jsonObject.addProperty("poster", poster);
                jsonObject.addProperty("overview", overview);
                jsonObject.addProperty("price", price);

                jsonArray.add(jsonObject);


                // Create a JsonObject based on the data we retrieve from rs


                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
