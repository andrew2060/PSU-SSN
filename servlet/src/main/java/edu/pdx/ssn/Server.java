package edu.pdx.ssn;

import edu.pdx.ssn.application.Library;
import edu.pdx.ssn.application.SQLLibrary;
import edu.pdx.ssn.pages.PageManager;
import edu.pdx.ssn.pages.ServerPage;
import edu.pdx.ssn.sql.MySQLConnection;
import edu.pdx.ssn.sql.Schema;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;


public final class Server extends HttpServlet implements Sessions {

    protected static final String CONFIGURATION_FILE_NAME = "configuration.yml";

    protected static Server instance;
    protected static ServerConfiguration config;
    protected static Library library;
    protected static MySQLConnection conn;

    public static Server getInstance() {
        return instance;
    }

    public static ServerConfiguration getConfig() {
        return config;
    }

    public static Library getLibrary() {
        return library;
    }

    public static MySQLConnection getConnection() {
        return conn;
    }

    @Override
    public void init() throws ServletException {
        instance = this;
        try {
            System.setErr(new PrintStream(new File("err.log")));
            System.setOut(new PrintStream(new File("runtime.log")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File configFile = new File(CONFIGURATION_FILE_NAME);
        if (!configFile.exists()) {
            throw new ServletException("Could not load configuration! Please ensure configuration.yml is present!.");
        }
        Yaml configYml = new Yaml();
        try {
            config = new ServerConfiguration((Map<String, Object>)configYml.load(new FileInputStream(configFile)));
        } catch (FileNotFoundException e) {
            throw new ServletException("Could not load configuration! This should never be reached.");
        }

        String server = config.getValue("server");
        int port = Integer.valueOf(config.getValue("port"));
        String user = config.getValue("username");
        String pass = config.getValue("password");
        String database = config.getValue("database");
        conn = new MySQLConnection(server, port, user, pass, database);
        library = new SQLLibrary(conn);
        setupTables();
    }

    private void setupTables() {
        String createUsers = "CREATE TABLE IF NOT EXISTS `" + Schema.USERS_TABLE + "` (`" + Schema.USER_UID + "` INT " +
                "NOT NULL AUTO_INCREMENT, `"
                + Schema.USER_EMAIL + "` VARCHAR(255), `" + Schema.USER_LAST_NAME + "` VARCHAR(255), `"
                + Schema.USER_FIRST_NAME + "` VARCHAR(255), `" + Schema.USER_PASSWORD_HASH + "` VARCHAR(255), `"
                + Schema.USER_IS_ADMIN + "` INT, `" + Schema.USER_PHONE + "` VARCHAR(255), "
                + "PRIMARY KEY(" + Schema.USER_UID + ")) ";
        String createBooks = "CREATE TABLE IF NOT EXISTS `" + Schema.BOOKS_TABLE + "` (`"
                + Schema.BOOK_ISBN + "` BIGINT, `" + Schema.BOOK_TITLE + "` VARCHAR(255), `"
                + Schema.BOOK_AUTHOR_LAST + "` VARCHAR(255), `" + Schema.BOOK_AUTHOR_FIRST + "` VARCHAR(255), `"
                + Schema.BOOK_SUBJECT + "` VARCHAR(255), `" + Schema.BOOK_COURSE_NUMBER + "` INT, `"
                + Schema.BOOK_ASSIGNING_PROFESSORS + "` VARCHAR(255))";
        String createRecords = "CREATE TABLE IF NOT EXISTS `" + Schema.RECORDS_TABLE + "` (`"
                + Schema.RECORD_BARCODE + "` BIGINT, `" + Schema.RECORD_ISBN + "` BIGINT, `"
                + Schema.RECORD_CHECKED_OUT + "` TINYINT, `" + Schema.RECORD_BORROW_UID + "` BIGINT, `"
                + Schema.RECORD_DUE_DATE + "` BIGINT, `" + Schema.RECORD_LOANED + "` BOOL, `"
                + Schema.RECORD_LOANER_UID + "` BIGINT, `" + Schema.RECORD_LOAN_END + "` BIGINT)";
        conn.executeQueryRaw(createBooks);
        conn.executeQueryRaw(createUsers);
        conn.executeQueryRaw(createRecords);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getAttribute("forward") == null) {
            req.setAttribute("forward", "");
        }
        // Construct session
        HttpSession session = req.getSession();
        if (session.isNew()) {
            session.setAttribute(IS_LOGGED_IN, false);
            session.setAttribute(ADMIN, false);
        }
        // Determine page
        ServerPage page;
        String pageKey;
        Map<String, String[]> params = req.getParameterMap();
        if (params.containsKey(Params.APP.getKey())) {
            pageKey = params.get(Params.APP.getKey())[0];
            page = PageManager.getPage(pageKey);
        } else {
            page = PageManager.getPage(null);
            pageKey = PageManager.DEFAULT_KEY;
        }
        // Process the request and set appropriate attributes
        boolean forward = page.processRequest(req, resp);
        // Set meta attributes
        page.setMetaAttributes(req);

        // Redirect to page
        if (!forward) {
            req.getRequestDispatcher("/WEB-INF/index.jsp?app=" + pageKey).forward(req, resp);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getAttribute("forward") == null) {
            req.setAttribute("forward", "");
        }
        // Construct session
        HttpSession session = req.getSession();
        if (session.isNew()) {
            session.setAttribute(IS_LOGGED_IN, false);
            session.setAttribute(ADMIN, false);
        }
        // Determine page
        ServerPage page;
        String pageKey;
        Map<String, String[]> params = req.getParameterMap();
        if (params.containsKey(Params.APP.getKey())) {
            pageKey = params.get(Params.APP.getKey())[0];
            page = PageManager.getPage(pageKey);
        } else {
            page = PageManager.getPage(null);
            pageKey = PageManager.DEFAULT_KEY;
        }
        // Forward post request
        page.doPost(req, resp);
    }

}


