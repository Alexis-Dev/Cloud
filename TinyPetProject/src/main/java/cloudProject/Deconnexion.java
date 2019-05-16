package cloudProject;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.*;

@WebServlet(
    name = "Deconnexion",
    urlPatterns = {"/deconnexion"}
)
public class Deconnexion extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {

	  UserService userService = UserServiceFactory.getUserService();

	  response.sendRedirect(userService.createLogoutURL("/index.html"));

  }
}