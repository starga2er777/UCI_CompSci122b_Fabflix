package org.uci;

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
import java.sql.*;

@WebServlet(name = "DashBoardAddStarServlet", urlPatterns = "/api/add-star")
public class DashBoardAddStarServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String star_name = request.getParameter("star-name");
        String star_year = request.getParameter("star-year");
        String returnMessage = "";

        try (PrintWriter out = response.getWriter(); Connection conn = dataSource.getConnection()) {
            String query = "call add_star(?, ?);";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, star_name);
            if (star_year.equals(""))
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2, Integer.parseInt(star_year));

            ResultSet rs = statement.executeQuery();

            JsonObject responseObject = new JsonObject();

            while (rs.next())
                returnMessage = rs.getString("message");

            responseObject.addProperty("message", returnMessage);
            rs.close();
            statement.close();

            out.write(responseObject.toString());
            response.setStatus(200);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
