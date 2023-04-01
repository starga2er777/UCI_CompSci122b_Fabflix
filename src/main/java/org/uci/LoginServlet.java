package org.uci;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.uci.dto.User;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

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
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String source = request.getParameter("source") == null ? "website" : request.getParameter("source");
        // Get reCaptcha response
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        // System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
        JsonObject responseJsonObject = new JsonObject();
        // Do something when they fail bot test
        if (source.equals("android") || verify(gRecaptchaResponse)) {
            int userId = 0;
            boolean loginFlag = false, userExists = false;
            try (Connection conn = dataSource.getConnection()) {
                String query = "select id,\n" +
                        "       firstName,\n" +
                        "       lastName,\n" +
                        "       password\n" +
                        "from customers\n" +
                        "where email = ?;";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setString(1, username);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    if (new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"))) {
                        loginFlag = true;
                        userId = rs.getInt("id");
                    }
                    userExists = true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (loginFlag) {
                // Login success:
                // set this user into the session
                request.getSession().setAttribute("user", new User(userId, username));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                // request.getServletContext().log("Login failed");

                if (userExists) {
                    responseJsonObject.addProperty("message", "Incorrect password.");
                } else {
                    responseJsonObject.addProperty("message", "User " + username + " doesn't exist.");
                }
            }
        } else {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Please complete verification.");
        }

        response.getWriter().write(responseJsonObject.toString());
    }

    // recaptcha constants
    public static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String RECAPTCHA_SECRET_KEY = "6LdR9rwiAAAAAG5RxaHe13WsS23UYg_7lOUNSsLB";


    // recaptcha verify method
    public static boolean verify(String gRecaptchaResponse) {
        try {
            URL verifyUrl = new URL(RECAPTCHA_VERIFY_URL);

            // Open Connection to URL
            HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();

            // Add Request Header
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            // Data will be sent to the server.
            String postParams = "secret=" + RECAPTCHA_SECRET_KEY + "&response=" + gRecaptchaResponse;

            // Send Request
            conn.setDoOutput(true);

            // Get the output stream of Connection
            // Write data in this stream, which means to send data to Server.
            OutputStream outStream = conn.getOutputStream();
            outStream.write(postParams.getBytes());

            outStream.flush();
            outStream.close();

            // Get the InputStream from Connection to read data sent from the server.
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);

            inputStreamReader.close();

            if (jsonObject.get("success").getAsBoolean()) {
                // verification succeed
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // verification failed
        return false;
    }
}
