package co.vgw.techtonic;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Embedded", urlPatterns = "/")
public class Main extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws LifecycleException, InterruptedException, ServletException {
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase","true");
        Tomcat tomcat = new Tomcat();
        tomcat.setHostname("localhost");
        tomcat.setPort(8080);

        Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "Embedded", new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                logger.info("REQUEST: Received form request");
                logger.info(req.getParameter("firstName"));
                logger.info(req.getParameter("lastName"));
                logger.info(req.getParameter("emailId"));
                logger.info(req.getParameter("password"));

                Writer w = resp.getWriter();
                w.write("<html><head><title>Registered!</title></head><body><p>You have successfully completed the form!");
                w.write(" <a href=\"/\">Go back, "+ req.getParameter("firstName")+"</a>.</body></html>");
                w.flush();
                w.close();
            }

            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                Writer w = resp.getWriter();
                w.write("<html>");
                w.write("<head>");
                w.write("<meta charset='ISO-8859-1'>");
                w.write("<title>Registration Page</title>");
                w.write("</head>");
                w.write("<body>");
                w.write("<h1> Registration Page</h1>");
                w.write("<form action='/' method='post'>");
                w.write("First Name: <input type='text' name='firstName'>");
                w.write("<br> <br>");
                w.write("Last Name: <input type='text' name='lastName'>");
                w.write("<br> <br>");
                w.write("Email ID: <input type='email' name='emailId'>");
                w.write("<br> <br>");
                w.write("Password: <input type='password' name='password'>");
                w.write("<br> <br>");
                w.write("<input type='submit' value='register'>");
                w.write("</form>");
                w.write("</body>");
                w.write("</html>");
                w.flush();
                w.close();
            }
        });

        ctx.addServletMappingDecoded("/*", "Embedded");

        tomcat.start();
        tomcat.getServer().await();
    }
}
