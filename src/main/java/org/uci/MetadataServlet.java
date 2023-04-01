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
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@WebServlet(name = "MetadataServlet", urlPatterns = "/api/metadata")
public class MetadataServlet extends HttpServlet {
    private DataSource dataSource;
    private static Map<Integer, String> dataTypeMap;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        getJdbcTypeName();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter(); Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet resultSet = databaseMetaData.getTables(conn.getCatalog(), null, null, new String[]{"TABLE"});

            while (resultSet.next()) {
                JsonObject tableObject = new JsonObject();
                String tableName = resultSet.getString("TABLE_NAME");
                tableObject.addProperty("table_name", tableName);
                ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null);
                JsonArray columnJsonArray = new JsonArray();
                // Add column info into the JSON object for the current table
                while (columns.next()) {
                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("column_name", columns.getString("COLUMN_NAME"));
                    columnObject.addProperty("data_type", dataTypeMap.get(columns.getInt("DATA_TYPE")));
                    columnJsonArray.add(columnObject);
                }
                columns.close();
                tableObject.add("column_info", columnJsonArray);
                jsonArray.add(tableObject);
            }

            resultSet.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getJdbcTypeName() {
        dataTypeMap = new HashMap<>();
        Field[] fields = java.sql.Types.class.getFields();
        for (Field field : fields) {
            try {
                String name = field.getName();
                Integer value = (Integer) field.get(null);
                dataTypeMap.put(value, name);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
