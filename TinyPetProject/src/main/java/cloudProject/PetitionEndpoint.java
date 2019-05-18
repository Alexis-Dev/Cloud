package cloudProject;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.ArrayList;
import java.util.List;

@Api(name = "monApi",
version = "v1")
public class PetitionEndpoint {

	@ApiMethod(name = "listMyPetitions",
			path = "mesPetitions")
	public List<Entity> listMyPetitionEntity() throws EntityNotFoundException {
		  	UserService userService = UserServiceFactory.getUserService();  
		  	User user = userService.getCurrentUser();
		  	String userId = user.getUserId();
		  	
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
			Key cleUser = KeyFactory.createKey("Utilisateur", userId);
			
			Entity e = datastore.get(cleUser);
			ArrayList<String> listePetitions = (ArrayList<String>)e.getProperty("petitionsSignees");
			
			Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.IN, listePetitions));;

			datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
	}
	
	@ApiMethod(name = "listTop",
			path = "top")
	public List<Entity> listTopPetitionEntity() {
			Query q = new Query("Petition");

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
	}
	
	@ApiMethod(name = "listAll",
			path = "all")
	public List<Entity> listAllPetitionEntity() {
			Query q = new Query("Petition");

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
	}	
	
	@ApiMethod(name = "addPetition",
			path = "add/{titre}/{description}")
	public Entity addPetition(@Named("titre") String titre, @Named("description") String description) {

			UserService userService = UserServiceFactory.getUserService();
		  
		  	User user = userService.getCurrentUser();
		  
			Entity e = new Entity("Petition", titre+description);
			e.setProperty("titre", titre);
			e.setProperty("description", description);
			e.setProperty("utilisateur", user);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}
	
	@ApiMethod(name = "Signature",
			path = "signature/{id}")
	public Entity Signature(@Named("id") String id) throws EntityNotFoundException {

		  	UserService userService = UserServiceFactory.getUserService();  
		  	User user = userService.getCurrentUser();
		  	String userId = user.getUserId();
		  	
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
			Key cleUser = KeyFactory.createKey("Utilisateur", userId);
			
			ArrayList<String> listePetitions;
			
			Entity e = datastore.get(cleUser);

		  	listePetitions = new ArrayList<String>();
			
		  	if (e.getProperty("petitionsSignees") == null) {
				listePetitions = new ArrayList<String>();
			}else {
				listePetitions = (ArrayList<String>)e.getProperty("petitionsSignees");
			}				
						
			listePetitions.add(id);
			e.setProperty("petitionsSignees", listePetitions);

			datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}
		
	@ApiMethod(name = "AddUser",
			path = "addUser")
	public Entity addUser() {

		  	UserService userService = UserServiceFactory.getUserService();  
		  	User user = userService.getCurrentUser();
		  	String userId = user.getUserId();
		  	String email = user.getEmail();
		  	
			Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			
			Entity e = new Entity("Utilisateur", userId);
			
			if(result.isEmpty()) {
			  	e.setProperty("email", email);
				datastore = DatastoreServiceFactory.getDatastoreService();
				datastore.put(e);
			}
				
			return  e;
	}

}
