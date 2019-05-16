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

	@ApiMethod(name = "listTop",
			path = "top")
	public List<Entity> listPetitionEntity() {
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
		  
			Entity e = new Entity("Petition", ""+titre+description);
			e.setProperty("titre", titre);
			e.setProperty("description", description);
			e.setProperty("utilisateur", user);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}

}
