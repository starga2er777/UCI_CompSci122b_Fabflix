package org.uci;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.uci.dto.ItemInfo;
import org.uci.dto.User;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

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
        User user = (User) request.getSession().getAttribute("user");
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String cardNumber = request.getParameter("credit_card");
        String expirationDate = request.getParameter("expiration_date");
        boolean isPaymentSuccess = true;
        JsonObject responseJsonObject = new JsonObject();

        String errorMessage = "Sorry, cannot find your information.";

        try (Connection conn = dataSource.getConnection()) {
            String query = "select c.id,\n" +
                    "       c.firstName,\n" +
                    "       c.lastName,\n" +
                    "       expiration\n" +
                    "from customers join creditcards c on c.id = customers.ccId\n" +
                    "where email = ?;";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, user.getUsername());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                if (!(rs.getString("firstName").equals(firstName) && rs.getString("lastName").equals(lastName))) {
                    isPaymentSuccess = false;
                    errorMessage = "Please check your name.";
                } else if (!rs.getString("id").equals(cardNumber)) {
                    isPaymentSuccess = false;
                    errorMessage = "Please check your card number.";
                } else if (!rs.getDate("expiration").toString().equals(expirationDate)) {
                    isPaymentSuccess = false;
                    errorMessage = "Please check your expiration date.";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (isPaymentSuccess) {
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");
        } else {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", errorMessage);
            request.getServletContext().log("Payment failed");
        }
        response.getWriter().write(responseJsonObject.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        // Get shopping cart
        Map<String, ItemInfo> cartList = (HashMap<String, ItemInfo>) request.getSession().getAttribute("cart");
        if (cartList == null) {
            response.setStatus(500);
            return;
        }
        JsonArray responseJsonArray = new JsonArray();
        try (Connection conn = dataSource.getConnection()) {
            // Get start id from sale
            int startId;
            String getIdQuery = "select count(*) from sales;";
            PreparedStatement preparedStatement = conn.prepareStatement(getIdQuery);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            startId = rs.getInt(1);

            for (Map.Entry<String, ItemInfo> entry : cartList.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                startId++;
                String query = "insert into sales (id, customerId, movieId, saleDate, amount) values (" + startId + ", ?, ?, CURDATE(), ?);";
                preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, user.getUserId());
                preparedStatement.setString(2, entry.getKey());
                preparedStatement.setInt(3, entry.getValue().amount);
                preparedStatement.execute();

                jsonObject.addProperty("saleId", startId);
                jsonObject.addProperty("movieId", entry.getKey());
                jsonObject.addProperty("title", entry.getValue().title);
                jsonObject.addProperty("amount", entry.getValue().amount);
                jsonObject.addProperty("price", entry.getValue().price);
                responseJsonArray.add(jsonObject);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // drop shopping cart
        request.getSession().removeAttribute("cart");
        response.getWriter().write(responseJsonArray.toString());
        response.setStatus(200);
    }
}
