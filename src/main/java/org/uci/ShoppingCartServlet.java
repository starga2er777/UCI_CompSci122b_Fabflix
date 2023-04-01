package org.uci;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.uci.dto.ItemInfo;

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
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {
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
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String currentMovie = request.getParameter("movieId");
        String operation = request.getParameter("operation");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        Map<String, ItemInfo> cartList = (HashMap<String, ItemInfo>) session.getAttribute("cart");
        if (cartList == null) {
            cartList = new HashMap<>();
            //cartList.put(currentMovie, 1);
            session.setAttribute("cart", cartList);
        }
        if (currentMovie != null) {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cartList) {
                if (cartList.get(currentMovie) == null) {
                    String title;
                    float price;
                    try (Connection conn = dataSource.getConnection()) {
                        String query = "select title, price from movies\n" +
                                "join price_of_movies pom on movies.id = pom.movieId\n" +
                                "where id = ?;";
                        PreparedStatement preparedStatement = conn.prepareStatement(query);
                        preparedStatement.setString(1, currentMovie);

                        ResultSet rs = preparedStatement.executeQuery();
                        rs.next();

                        title = rs.getString(1);
                        price = rs.getFloat(2);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    cartList.put(currentMovie, new ItemInfo(title, 1, price));
                } else {
                    ItemInfo info = cartList.get(currentMovie);
                    Integer newAmount = cartList.get(currentMovie).amount + Integer.parseInt(operation);
                    if (newAmount.equals(0)) {
                        cartList.remove(currentMovie);
                    } else {
                        cartList.replace(currentMovie, new ItemInfo(info.title, newAmount, info.price));
                    }
                }
            }
        }

        //cartList.put("tt0483607", 2);//for testing

        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();

            String query = "select id, posterURL, title, price from movies, price_of_movies pr, poster p" +
                    " where pr.movieId = movies.id and p.movieId = movies.id and id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            Set<String> movies = cartList.keySet();
            for (String movie : movies) {

                preparedStatement.setString(1, movie);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    String movie_id = rs.getString("id");
                    String title = rs.getString("title");
                    double price = rs.getDouble("price");
                    String poster = rs.getString("posterURL");
                    double value = 1.0 * cartList.get(movie).amount * price;
                    String str = String.format("%.2f",value);
                    value = Double.parseDouble(str);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("title", title);
                    jsonObject.addProperty("quantity", cartList.get(movie).amount);
                    jsonObject.addProperty("price", value);
                    jsonObject.addProperty("poster", poster);

                    jsonArray.add(jsonObject);
                }
                rs.close();

            }
            preparedStatement.close();

            out.write(jsonArray.toString());

            response.setStatus(200);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String currentMovie = request.getParameter("movieId");
        String operation = request.getParameter("operation");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        Map<String, ItemInfo> cartList = (HashMap<String, ItemInfo>) session.getAttribute("cart");
        if (cartList == null) {
            cartList = new HashMap<>();
            //cartList.put(currentMovie, 1);
            session.setAttribute("cart", cartList);
        }
        if (currentMovie != null) {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cartList) {
                if (cartList.get(currentMovie) == null) {
                    String title;
                    float price;
                    try (Connection conn = dataSource.getConnection()) {
                        String query = "select title, price from movies\n" +
                                "join price_of_movies pom on movies.id = pom.movieId\n" +
                                "where id = ?;";
                        PreparedStatement preparedStatement = conn.prepareStatement(query);
                        preparedStatement.setString(1, currentMovie);

                        ResultSet rs = preparedStatement.executeQuery();
                        rs.next();

                        title = rs.getString(1);
                        price = rs.getFloat(2);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    cartList.put(currentMovie, new ItemInfo(title, 1, price));
                } else {
                    ItemInfo info = cartList.get(currentMovie);
                    Integer newAmount = cartList.get(currentMovie).amount + Integer.parseInt(operation);
                    if (newAmount.equals(0)) {
                        cartList.remove(currentMovie);
                    } else {
                        cartList.replace(currentMovie, new ItemInfo(info.title, newAmount, info.price));
                    }
                }
            }
        }

        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();

            String query = "select id, posterURL, title, price from movies, price_of_movies pr, poster p" +
                    " where pr.movieId = movies.id and p.movieId = movies.id and id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            Set<String> movies = cartList.keySet();
            for (String movie : movies) {

                preparedStatement.setString(1, movie);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    String movie_id = rs.getString("id");
                    String title = rs.getString("title");
                    double price = rs.getDouble("price");
                    String poster = rs.getString("posterURL");
                    double value = 1.0 * cartList.get(movie).amount * price;
                    String str = String.format("%.2f",value);
                    value = Double.parseDouble(str);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("title", title);
                    jsonObject.addProperty("quantity", cartList.get(movie).amount);
                    jsonObject.addProperty("price", value);
                    jsonObject.addProperty("poster", poster);

                    jsonArray.add(jsonObject);
                }
                rs.close();

            }
            preparedStatement.close();

            out.write(jsonArray.toString());

            response.setStatus(200);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }
}
