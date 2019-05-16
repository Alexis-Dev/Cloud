package cloudProject;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import com.google.appengine.api.users.User;


@WebServlet(
    name = "NewPetition",
    urlPatterns = {"/newPetition"}
)
public class NewPetition extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  UserService userService = UserServiceFactory.getUserService();
	  
	  User user = userService.getCurrentUser();
	  
	  Entity petition = new Entity("Petition", "essai");
	  petition.setProperty("titre", "essai");
	  petition.setProperty("description", "essai descritption");
	  petition.setProperty("utilisateur", user);
	  
	  datastore.put(petition);
	  response.sendRedirect("/accueil.html");
  }
  /*
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {	
      try {
          // Création de l'objet
          Petition pet = new Petition("titre", "descritption");
          // Enregistrement de l'objet dans le Datastore avec Objectify
          ofy().save().entity(pet).now();

          resp.sendRedirect("/accueil.html");
      } catch (IOException e) {
          e.printStackTrace();
      }
  }*/
}
