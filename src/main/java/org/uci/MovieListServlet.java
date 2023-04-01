package org.uci;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            super.init(config);
        } catch (ServletException e) {
            e.printStackTrace();
        }
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long start_time_TS = System.nanoTime();
        response.setContentType("application/json");
        String Genres = request.getParameter("genre");
        String StartWith = request.getParameter("startingWith");
        String Title = request.getParameter("title");
        String Year = request.getParameter("year");
        String Director = request.getParameter("director");
        String Star = request.getParameter("star");
        String Page = request.getParameter("page");
        String Size = request.getParameter("size");
        String Order = request.getParameter("order");
        String Order2 = request.getParameter("second-order");
        if (Order2 != null && !Order2.equals("null")) {
            Order += ", " + Order2;
        }

        HttpSession session = request.getSession();

        if (Objects.equals(Page, "null") || Page == null || Page.equals("")) Page = "1";
        if (Objects.equals(Size, "null") || Size == null || Size.equals("")) Size = "20";
        if (Objects.equals(Order, "null") || Order == null || Order.equals("")) Order = "rating desc";
        session.setAttribute("size", Size);
        session.setAttribute("order", Order);
        int lowerBound = (Integer.parseInt(Page) - 1) * Integer.parseInt(Size);

        // Output stream to STDOUT

        // Get a connection from dataSource and let resource manager close the connection after usage.
        long start_time_TJ, end_time_TJ;
        start_time_TJ = System.nanoTime();
        try (PrintWriter out = response.getWriter(); Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            // Construct a query with parameter represented by "?"
            String query = "select TEMP.movieId,\n" +
                    "       title,\n" +
                    "       year,\n" +
                    "       director,\n" +
                    "       group_concat(distinct g.name)           as genres,\n" +
                    "       group_concat(concat(s.id, '|', s.name)) as stars,\n" +
                    "       rating,\n" +
                    "       posterURL,\n" +
                    "       price\n" +
                    "from (";
            String innerQuery1 = "select\n" +
                    "       movies.id as movieId,\n" +
                    "       title,\n" +
                    "       year,\n" +
                    "       director,\n" +
                    "       rating\n" +
                    "from movies\n" +
                    "         left join ratings r on movies.id = r.movieId\n";
            String innerQuery2 = "";
            if (Genres != null) {
                // search movie through genres
                innerQuery1 += "         join genres_in_movies gim on movies.id = gim.movieId\n" +
                        "         join genres g on g.id = gim.genreId\n";
                innerQuery2 += "where g.name = " + "'" + Genres + "'\n";
            } else if (StartWith != null) {
                innerQuery2 += "where title like '" + StartWith + "%'\n";
            } else {
                innerQuery2 += "where true ";
                if (Title != null) {
                    // using full-text search instead of 'like'
                    String ti = Arrays.stream(Title.split(" ")).map(s -> ("+" + s + "*")).collect(Collectors.joining(" "));
                    innerQuery2 += "and ((Match(title) Against ('" + ti + "' IN BOOLEAN MODE))" +
                            " or soundex(title) = soundex('" + Title + "'))\n";
                }
                if (Year != null) {
                    innerQuery2 += "and year = " + Year + "\n";
                }
                if (Director != null) {
                    innerQuery2 += "and Match(director) Against ('" + Director + "') IN BOOLEAN MODE)\n";
                }
                if (Star != null) {
                    innerQuery1 += "         join stars_in_movies sim on movies.id = sim.movieId\n" +
                            "         join stars s on sim.starId = s.id\n";
                    innerQuery2 += "and Match(s.name) Against ('" + Star + "' IN BOOLEAN MODE)\n";
                }
            }
            innerQuery2 += "group by movies.id, title, year, director, rating\n" +
                    "order by " + Order + " \n" +
                    "limit " + Size + " offset " + lowerBound;

            String InnerQuery = innerQuery1 + innerQuery2;

            query += InnerQuery +
                    ") AS TEMP\n" +
                    "join genres_in_movies gim on gim.movieId = TEMP.movieId\n" +
                    "join genres g on g.id = gim.genreId\n" +
                    "join stars_in_movies sim on sim.movieId = TEMP.movieId\n" +
                    "join stars s on sim.starId = s.id\n" +
                    "join poster pt on pt.movieId = TEMP.movieId\n" +
                    "join price_of_movies pom on pom.movieId = TEMP.movieId\n" +
                    "group by movieId, title, year, director, rating, posterURL, price\n" +
                    "order by " + Order + ";";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            // statement.setString(1, id);

            // Perform the query

            ResultSet rs = statement.executeQuery();
            end_time_TJ = System.nanoTime();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {

                String movieID = rs.getString("movieId");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                String poster = rs.getString("posterURL");
                JsonArray genres = new JsonArray(), star_name = new JsonArray(), star_id = new JsonArray();
                Arrays.stream(rs.getString("genres").split(",")).limit(3).forEach(genres::add);
                Arrays.stream(rs.getString("stars").split(",")).limit(3)
                        .map(str -> Arrays.stream(str.split("\\|")).collect(Collectors.toList()))
                        .forEach(lst -> {
                            star_id.add(lst.get(0));
                            star_name.add(lst.get(1));
                        });
                String rating = rs.getObject("rating") != null ? Float.toString(rs.getFloat("rating")) : "N/A";
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
                jsonObject.addProperty("price", price);

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
        long end_time_TS = System.nanoTime();
        long TS_time = end_time_TS - start_time_TS;
        long TJ_time = end_time_TJ - start_time_TJ;
        //logTime(1, 2);
        logTime(TS_time, TJ_time);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long start_time_TS = System.nanoTime();
        response.setContentType("application/json");
        String Genres = request.getParameter("genre");
        String StartWith = request.getParameter("startingWith");
        String Title = request.getParameter("title");
        String Year = request.getParameter("year");
        String Director = request.getParameter("director");
        String Star = request.getParameter("star");
        String Page = request.getParameter("page");
        String Size = request.getParameter("size");
        String Order = request.getParameter("order");
        String Order2 = request.getParameter("second-order");
        if (Order2 != null && !Order2.equals("null")) {
            Order += ", " + Order2;
        }

        HttpSession session = request.getSession();

        if (Objects.equals(Page, "null") || Page == null || Page.equals("")) Page = "1";
        if (Objects.equals(Size, "null") || Size == null || Size.equals("")) Size = "20";
        if (Objects.equals(Order, "null") || Order == null || Order.equals("")) Order = "rating desc";
        session.setAttribute("size", Size);
        session.setAttribute("order", Order);
        int lowerBound = (Integer.parseInt(Page) - 1) * Integer.parseInt(Size);

        // Output stream to STDOUT

        // Get a connection from dataSource and let resource manager close the connection after usage.
        long start_time_TJ, end_time_TJ;
        start_time_TJ = System.nanoTime();
        try (PrintWriter out = response.getWriter(); Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            // Construct a query with parameter represented by "?"
            String query = "select TEMP.movieId,\n" +
                    "       title,\n" +
                    "       year,\n" +
                    "       director,\n" +
                    "       group_concat(distinct g.name)           as genres,\n" +
                    "       group_concat(concat(s.id, '|', s.name)) as stars,\n" +
                    "       rating,\n" +
                    "       posterURL,\n" +
                    "       price\n" +
                    "from (";
            String innerQuery1 = "select\n" +
                    "       movies.id as movieId,\n" +
                    "       title,\n" +
                    "       year,\n" +
                    "       director,\n" +
                    "       rating\n" +
                    "from movies\n" +
                    "         left join ratings r on movies.id = r.movieId\n";
            String innerQuery2 = "";
            if (Genres != null) {
                // search movie through genres
                innerQuery1 += "         join genres_in_movies gim on movies.id = gim.movieId\n" +
                        "         join genres g on g.id = gim.genreId\n";
                innerQuery2 += "where g.name = " + "'" + Genres + "'\n";
            } else if (StartWith != null) {
                innerQuery2 += "where title like '" + StartWith + "%'\n";
            } else {
                innerQuery2 += "where true ";
                if (Title != null) {
                    // using full-text search instead of 'like'
                    String ti = Arrays.stream(Title.split(" ")).map(s -> ("+" + s + "*")).collect(Collectors.joining(" "));
                    innerQuery2 += "and ((Match(title) Against ('" + ti + "' IN BOOLEAN MODE))" +
                            " or soundex(title) = soundex('" + Title + "'))\n";
                }
                if (Year != null) {
                    innerQuery2 += "and year = " + Year + "\n";
                }
                if (Director != null) {
                    innerQuery2 += "and Match(director) Against ('" + Director + "') IN BOOLEAN MODE)\n";
                }
                if (Star != null) {
                    innerQuery1 += "         join stars_in_movies sim on movies.id = sim.movieId\n" +
                            "         join stars s on sim.starId = s.id\n";
                    innerQuery2 += "and Match(s.name) Against ('" + Star + "' IN BOOLEAN MODE)\n";
                }
            }
            innerQuery2 += "group by movies.id, title, year, director, rating\n" +
                    "order by " + Order + " \n" +
                    "limit " + Size + " offset " + lowerBound;

            String InnerQuery = innerQuery1 + innerQuery2;

            query += InnerQuery +
                    ") AS TEMP\n" +
                    "join genres_in_movies gim on gim.movieId = TEMP.movieId\n" +
                    "join genres g on g.id = gim.genreId\n" +
                    "join stars_in_movies sim on sim.movieId = TEMP.movieId\n" +
                    "join stars s on sim.starId = s.id\n" +
                    "join poster pt on pt.movieId = TEMP.movieId\n" +
                    "join price_of_movies pom on pom.movieId = TEMP.movieId\n" +
                    "group by movieId, title, year, director, rating, posterURL, price\n" +
                    "order by " + Order + ";";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            // statement.setString(1, id);

            // Perform the query

            ResultSet rs = statement.executeQuery();
            end_time_TJ = System.nanoTime();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {

                String movieID = rs.getString("movieId");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                String poster = rs.getString("posterURL");
                JsonArray genres = new JsonArray(), star_name = new JsonArray(), star_id = new JsonArray();
                Arrays.stream(rs.getString("genres").split(",")).limit(3).forEach(genres::add);
                Arrays.stream(rs.getString("stars").split(",")).limit(3)
                        .map(str -> Arrays.stream(str.split("\\|")).collect(Collectors.toList()))
                        .forEach(lst -> {
                            star_id.add(lst.get(0));
                            star_name.add(lst.get(1));
                        });
                String rating = rs.getObject("rating") != null ? Float.toString(rs.getFloat("rating")) : "N/A";
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
                jsonObject.addProperty("price", price);

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
        long end_time_TS = System.nanoTime();
        long TS_time = end_time_TS - start_time_TS;
        long TJ_time = end_time_TJ - start_time_TJ;
        logTime(TS_time, TJ_time);
    }

    public void logTime(long TS_time, long TJ_time) throws IOException {
        //String contextPath = getServletContext().getRealPath("/");
        //String filePath = contextPath + "log.csv";
        String filePath = "/home/ubuntu/cs122b-fall-team-45/WebContent/log.csv";
        File file = new File(filePath);
        file.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        bufferedWriter.write(TS_time +",");
        bufferedWriter.write(Long.toString(TJ_time));
        bufferedWriter.write("\n");
        bufferedWriter.close();
    }
}