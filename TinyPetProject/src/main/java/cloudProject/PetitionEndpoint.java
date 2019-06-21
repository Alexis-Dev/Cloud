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

	//Méthode qui récupère toutes les pétitions signées par l'utilisateur connecté
	@ApiMethod(name = "listMyPetitions",
			path = "mesPetitions/{userId}/{email}")
	public List<Entity> listMyPetitionEntity(@Named("userId") String userId,@Named("email") String email) throws EntityNotFoundException {
		  	
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			List<Entity> result = new ArrayList<Entity>();
			
			Query qUser = new Query("Utilisateur").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
			
			PreparedQuery pqUser = datastore.prepare(qUser);
			List<Entity> resultUser = pqUser.asList(FetchOptions.Builder.withLimit(100));
			
			Entity user;
			ArrayList<String> listePetitions;
			
			//On vérifie si l'utilisateur est déjà stocké dans le datastore
			if(resultUser.isEmpty()) {
				user = new Entity("Utilisateur", userId);
			  	user.setProperty("email", email);
			  	listePetitions = new ArrayList<String>();
				datastore.put(user);
			}else {
				user = resultUser.get(0);
				listePetitions = (ArrayList<String>)user.getProperty("petitionsSignees");
			}
			
			//Verification afin de savoir si la liste des pétitions est non vide 
			if(!listePetitions.isEmpty()) {
				Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.IN, listePetitions));

				datastore = DatastoreServiceFactory.getDatastoreService();
				PreparedQuery pq = datastore.prepare(q);
				result = pq.asList(FetchOptions.Builder.withDefaults());
			}
			
			return result;
	}
	
	//Permet d'obtenir le top 100 des petitions en fonction du nombre de signatures
	@ApiMethod(name = "listTop",
			path = "top")
	public List<Entity> listTopPetitionEntity() {
			Query q = new Query("Petition").addSort("cptSignatures", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
			return result;
	}
	
	//Permet d'obtenir les 100 premières pétitions
	@ApiMethod(name = "listAll",
			path = "all")
	public List<Entity> listAllPetitionEntity() {
			Query q = new Query("Petition");

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(3));
			return result;
	}
	
	//Pagination => Permet d'obtenir les 100 petitions suivantes
	@ApiMethod(name = "suivant",
			path = "suivant/{id}")
	public List<Entity> paginationSuivant(@Named("id") String id) {
		
			Key clePetition = KeyFactory.createKey("Petition", id);
			
			Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.GREATER_THAN, id));

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(3));
			return result;
	}
	
	//Pagination => Permet d'obtenir les 100 petitions précédentes
	@ApiMethod(name = "precedent",
			path = "precedent/{id}")
	public List<Entity> paginationPrecedent(@Named("id") String id) {
		
			Key clePetition = KeyFactory.createKey("Petition", id);
			
			Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre", Query.FilterOperator.LESS_THAN, id)).addSort("titre", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(3));
			return result;
	}
	
	//Permet d'ajouter une pétition
	@ApiMethod(name = "addPetition",
			path = "add/{titre}/{description}/{email}/{userId}")
	public Entity addPetition(@Named("titre") String titre, @Named("description") String description, @Named("email") String email, @Named("userId") String userId) {
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Query qUser = new Query("Utilisateur").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
			
			PreparedQuery pqUser = datastore.prepare(qUser);
			List<Entity> resultUser = pqUser.asList(FetchOptions.Builder.withDefaults());
			
			Entity user;
			ArrayList<String> listePetitions;
			if(resultUser.isEmpty()) {
				user = new Entity("Utilisateur", userId);
			  	user.setProperty("email", email);
			  	listePetitions = new ArrayList<String>();
				//datastore.put(user);
			}else {
				user = resultUser.get(0);
				listePetitions = (ArrayList<String>)user.getProperty("petitionsSignees");
				if(listePetitions == null) {
					listePetitions = new ArrayList<String>();
				}
			}
			
			Entity e = new Entity("Petition", titre);
			e.setProperty("titre", titre);
			e.setProperty("description", description);
			e.setProperty("utilisateur", email);
			e.setProperty("cptSignatures", 1);
			
			listePetitions.add(titre);
			user.setProperty("petitionsSignees", listePetitions);
			
			datastore.put(e);
			datastore.put(user);
			return  e;
	}
	
	//Permet de signer une pétition
	@ApiMethod(name = "Signature",
			path = "signature/{id}/{userId}/{email}")
	public Entity Signature(@Named("id") String id, @Named("userId") String userId, @Named("email") String email) throws EntityNotFoundException {
		  	
			
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
			
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		
		Entity user;
		
		ArrayList<String> listePetitions;
		
		//On verifie si l'utilisateur est déjà stocké dans le datastore
		if(result.isEmpty()) {
			user = new Entity("Utilisateur", userId);
		  	user.setProperty("email", email);
		  	listePetitions = new ArrayList<String>();
		}else {
			user = result.get(0);
			listePetitions = new ArrayList<String>();
			if (user.getProperty("petitionsSignees") == null) {
				listePetitions = new ArrayList<String>();
			}else {
				listePetitions = (ArrayList<String>)user.getProperty("petitionsSignees");
			}
		}
					
		Key clePetition = KeyFactory.createKey("Petition", id);
			
		Entity petition = datastore.get(clePetition);		
			
		if(!listePetitions.contains(id)) {
		 	long cpt = (long) petition.getProperty("cptSignatures");
		  	System.out.println(petition.getProperty("cptSignatures"));
		  	cpt++;
			  	
			listePetitions.add(id);
			System.out.println(listePetitions);
			user.setProperty("petitionsSignees", listePetitions);
			petition.setProperty("cptSignatures", cpt);
				
			datastore.put(petition);
			datastore.put(user);
	  	}
		  				
		return petition;
	}

}
