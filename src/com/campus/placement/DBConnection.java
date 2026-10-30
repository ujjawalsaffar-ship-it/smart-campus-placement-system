package com.campus.placement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static final String DB_FILE = "placement.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeDatabase(boolean forceReset) {
        File dbFile = new File(DB_FILE);
        if (!forceReset && dbFile.exists() && dbFile.length() > 0) {
            return;
        }

        System.out.println("Initializing database from SQL scripts...");
        try (Connection conn = getConnection()) {
            File schemaFile = new File("sql" + File.separator + "01_schema.sql");
            File seedFile = new File("sql" + File.separator + "02_seed_data.sql");

            if (schemaFile.exists()) {
                executeSqlScript(conn, schemaFile);
                System.out.println("  ✓ Schema and triggers loaded successfully.");
            } else {
                System.err.println("  ✗ Schema file not found: " + schemaFile.getAbsolutePath());
            }

            if (seedFile.exists()) {
                executeSqlScript(conn, seedFile);
                System.out.println("  ✓ Sample seed data loaded successfully.");
            } else {
                System.err.println("  ✗ Seed file not found: " + seedFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /**
     * Executes SQL scripts, correctly parsing triggers (BEGIN ... END;) without breaking on nested semicolons.
     */
    private static void executeSqlScript(Connection conn, File file) throws IOException, SQLException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignore whole-line comments
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
            }
        }

        List<String> statements = splitSqlStatements(sb.toString());
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String cleanSql = sql.trim();
                if (!cleanSql.isEmpty()) {
                    try {
                        stmt.execute(cleanSql);
                    } catch (SQLException e) {
                        System.err.println("Error executing SQL:\n" + cleanSql + "\n--> " + e.getMessage());
                        throw e;
                    }
                }
            }
        }
    }

    private static List<String> splitSqlStatements(String fullScript) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideTriggerBlock = false;

        String[] lines = fullScript.split("\n");
        for (String line : lines) {
            String upper = line.trim().toUpperCase();

            if (upper.startsWith("CREATE TRIGGER")) {
                insideTriggerBlock = true;
            }

            current.append(line).append("\n");

            if (insideTriggerBlock) {
                if (upper.equals("END;") || upper.startsWith("END;")) {
                    statements.add(current.toString().trim());
                    current.setLength(0);
                    insideTriggerBlock = false;
                }
            } else {
                if (line.trim().endsWith(";")) {
                    statements.add(current.toString().trim());
                    current.setLength(0);
                }
            }
        }

        if (current.length() > 0 && !current.toString().trim().isEmpty()) {
            statements.add(current.toString().trim());
        }

        return statements;
    }
}
