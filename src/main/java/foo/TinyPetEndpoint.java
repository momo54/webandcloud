package foo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.common.collect.Table;

@Api(name = "myApi",
     version = "v1",
     audiences = "666941423950-75griipv5tc10il429v8hv8k17g5vbgp.apps.googleusercontent.com",
  	 clientIds = "666941423950-75griipv5tc10il429v8hv8k17g5vbgp.apps.googleusercontent.com",
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld1.example.com",
		   ownerName = "helloworld1.example.com",
		   packagePath = "")
     )

public class TinyPetEndpoint {
		
	//Crée une petition
	@ApiMethod(name = "createPetetition", httpMethod = HttpMethod.POST)
	public Entity createPetition(User owner, PostMessage pm) throws UnauthorizedException, InterruptedException {

		if (owner == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
		Entity e = new Entity("Petition", Long.MAX_VALUE-(new Date()).getTime() +":" + owner.getEmail());
		e.setProperty("body", pm.body);
		e.setProperty("owner", owner.getEmail());
				
		//crée des signataire
		ArrayList<String> fset = new ArrayList<String>();
		fset.add(owner.getEmail());
		e.setProperty("signatory",fset);
		e.setProperty("nbSignatory",400);
				
		//crée des tags
		/*ArrayList<String> fset2 = new ArrayList<String>();
		System.out.print(pm.tag);
		fset2.add();*/
		e.setProperty("tags", pm.tag);
				
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		return e;
	}
	
	//Charge le top 5 petitions puis les 5 suivantes et ainsi de suite
		@ApiMethod(name = "toppetition", httpMethod = HttpMethod.GET)
			public CollectionResponse<Entity> toppetition(@Named("filtre") String Tags, @Nullable @Named("next") String cursorString, User user)
					throws UnauthorizedException {

				if (user == null) {
					throw new UnauthorizedException("Invalid credentials");
				}

				Query q = new Query("Petition");
				
				/*q.addProjection(new PropertyProjection("body",String.class));
				q.addProjection(new PropertyProjection("nbSignatory", Integer.class));
				q.addProjection(new PropertyProjection("tags", String.class));*/
				
				
				if(!Tags.equals("tous")) {
				q.setFilter(new FilterPredicate("tags", FilterOperator.EQUAL, Tags));
				}
				q.addSort("nbSignatory", SortDirection.DESCENDING);

				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				PreparedQuery pq = datastore.prepare(q);

				FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

				if (cursorString != null) {
					fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
				}

				QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
				cursorString = results.getCursor().toWebSafeString();

				return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
			}
		
		//Charger les pétitions signées par l'utilisateur
		@ApiMethod(name = "MyPetition", httpMethod = HttpMethod.GET)
		public CollectionResponse<Entity> MyPetition(@Named("owner") User owner,@Nullable @Named("next") String cursorString) throws UnauthorizedException {
			if (owner == null) {
				throw new UnauthorizedException("Invalid credentials");
			}
			Query q = new Query("Petition")
					.setFilter(new FilterPredicate("signatory", FilterOperator.EQUAL, owner.getEmail()));
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

			PreparedQuery pq = datastore.prepare(q);
			FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

			if (cursorString != null) {
				fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
			}

			QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
			cursorString = results.getCursor().toWebSafeString();

			return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
		}
	
		//Signe une petition
		@ApiMethod(name = "signPetition", httpMethod = HttpMethod.POST)
		public Entity signPetition(User user, PostMessage pm) throws UnauthorizedException {
			if (user == null) {
				throw new UnauthorizedException("Invalid credentials");
			}
			 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			///utiliser coutingSH pour implémenter
				
			//https://cloud.google.com/datastore/docs/concepts/entities#updating_an_entity
			//mettre à jours une entité
			Key lkey = KeyFactory.createKey("Petition", pm.key);
			Entity ent = new Entity("Petition","hello");
			Transaction txn=datastore.beginTransaction();
			try {
				ent = datastore.get(lkey);
				int nb =  Integer.parseInt(ent.getProperty("nbSignatory").toString());
			    @SuppressWarnings("unchecked") // Cast can't verify generic type.
			    ArrayList<String> signatories = (ArrayList<String>) ent.getProperty("signatory");
			    
			    if(!signatories.contains(user.getEmail())) {
			    	signatories.add(user.getEmail());
			    	ent.setProperty("signatory", signatories);
				    ent.setProperty("nbSignatory", nb + 1 );
			    }
				datastore.put(ent);
				txn.commit();
				return ent;
			} catch (EntityNotFoundException e) {
					// This should never happen
					e.printStackTrace();
				}
			  finally {
				if (txn.isActive()) {
				    txn.rollback();
				  }
			}
			return ent;
		}
}
