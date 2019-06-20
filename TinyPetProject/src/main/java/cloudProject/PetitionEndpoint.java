package cloudProject;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(name = "monApi",
version = "v1")
public class PetitionEndpoint {

	@ApiMethod(name = "getUser",
			path = "getUser")
	public User getUser() {

		  	UserService userService = UserServiceFactory.getUserService();  
		  	User user = userService.getCurrentUser();
		  	System.out.println(user);
			
		  	if(user != null) {
		  		return  user;
		  	}else {
		  		return null;
		  	}
			
	}
	
	@ApiMethod(name = "listMyPetitions",
			path = "mesPetitions/{userId}")
	public List<Entity> listMyPetitionEntity(@Named("userId") String userId) throws EntityNotFoundException {
		  	
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
			Key cleUser = KeyFactory.createKey("Utilisateur", userId);
			
			Entity e = datastore.get(cleUser);
			List<Entity> result;
			ArrayList<String> listePetitions = (ArrayList<String>)e.getProperty("petitionsSignees");
			if(listePetitions != null) {
				Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.IN, listePetitions));

				datastore = DatastoreServiceFactory.getDatastoreService();
				PreparedQuery pq = datastore.prepare(q);
				result = pq.asList(FetchOptions.Builder.withDefaults());
			}else {
				result = new ArrayList<Entity>();
			}
			
			return result;
	}
	
	@ApiMethod(name = "listTop",
			path = "top")
	public List<Entity> listTopPetitionEntity() {
			Query q = new Query("Petition").addSort("cptSignatures", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
			return result;
	}
	
	@ApiMethod(name = "listAll",
			path = "all")
	public List<Entity> listAllPetitionEntity() {
			Query q = new Query("Petition");

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(2));
			return result;
	}
	
	@ApiMethod(name = "suivant",
			path = "suivant/{id}")
	public List<Entity> paginationSuivant(@Named("id") String id) {
		
			Key clePetition = KeyFactory.createKey("Petition", id);
			
			Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.GREATER_THAN, id));

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(2));
			//System.out.println(result);
			return result;
	}
	
	@ApiMethod(name = "precedent",
			path = "precedent/{id}")
	public List<Entity> paginationPrecedent(@Named("id") String id) {
		
			Key clePetition = KeyFactory.createKey("Petition", id);
			
			Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.LESS_THAN, id)).addSort("titre", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(2));
			//System.out.println(result);
			return result;
	}
	
	@ApiMethod(name = "addPetition",
			path = "add/{titre}/{description}/{email}")
	public Entity addPetition(@Named("titre") String titre, @Named("description") String description, @Named("email") String email) {

			Entity e = new Entity("Petition", titre);
			e.setProperty("titre", titre);
			e.setProperty("description", description);
			e.setProperty("utilisateur", email);
			e.setProperty("cptSignatures", 0);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}
	
	@ApiMethod(name = "Signature",
			path = "signature/{id}/{userId}")
	public Entity Signature(@Named("id") String id, @Named("userId") String userId) throws EntityNotFoundException {
		  	
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
			Key cleUser = KeyFactory.createKey("Utilisateur", userId);
			Key clePetition = KeyFactory.createKey("Petition", id);
			
			ArrayList<String> listePetitions;
			
			Entity e = datastore.get(cleUser);
			Entity petition = datastore.get(clePetition);

		  	listePetitions = new ArrayList<String>();
			
		  	if (e.getProperty("petitionsSignees") == null) {
				listePetitions = new ArrayList<String>();
			}else {
				listePetitions = (ArrayList<String>)e.getProperty("petitionsSignees");
			}				
			
		  	if(!listePetitions.contains(id)) {
		  		long cpt = (long) petition.getProperty("cptSignatures");
			  	System.out.println(cpt);
			  	petition.setProperty("cptSignatures", cpt+1);
			  	System.out.println(petition.getProperty("cptSignatures"));
			  	
		  		listePetitions.add(id);
				e.setProperty("petitionsSignees", listePetitions);
				
				datastore.put(petition);
				datastore.put(e);
		  	}
		  				
			return petition;
	}
		
	@ApiMethod(name = "AddUser",
			path = "addUser/{userId}/{email}")
	public Entity addUser(@Named("userId") String userId, @Named("email") String email) {
  	
			Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			
			Entity e = new Entity("Utilisateur", userId);
			
			if(result.isEmpty()) {
			  	e.setProperty("email", email);
			  	ArrayList<String> listePetitions = new ArrayList<String>();
			  	e.setProperty("petitionsSignees", listePetitions);
				datastore = DatastoreServiceFactory.getDatastoreService();
				datastore.put(e);
			}
				
			return  e;
	}

}
