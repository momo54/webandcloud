package foo;

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

	Random r = new Random();

	/*@ApiMethod(name = "Petition", httpMethod = HttpMethod.GET)
	public List<Entity> Petition() {
		Query q = new Query("Petition").addSort("name", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}*/
	//Charge le top 10 petitions
	@ApiMethod(name = "toppetition", httpMethod = HttpMethod.GET)
	public List<Entity> toppetition() {
		Query q = new Query("Petition").addSort("nbSignatory", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
		return result;
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
		Key lkey = KeyFactory.createKey("Petition", pm.body);
		Entity ent = new Entity("Petition","hello");
		try {
			ent = datastore.get(lkey);
			int nb =  Integer.parseInt(ent.getProperty("nbSignatory").toString());
		    System.out.println("après get");
		    @SuppressWarnings("unchecked") // Cast can't verify generic type.
		    ArrayList<String> signatories = (ArrayList<String>) ent.getProperty("signatory");
		    
		    if(!signatories.contains(user.getEmail())) {
		    	signatories.add(user.getEmail());
		    	ent.setProperty("signatory", signatories);
			    ent.setProperty("nbSignatory", nb + 1 );
		    }
			datastore.put(ent);
			return ent;
		} catch (EntityNotFoundException e) {
		// This should never happen
		}
		return ent;
	}
		
	//Crée une petition
	@ApiMethod(name = "createPetetition", httpMethod = HttpMethod.POST)
	public Entity createPetition(User owner, PostMessage pm) throws UnauthorizedException {

		if (owner == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
		Date date = new Date(); // voir score endpoint clé
		Entity e = new Entity("Petition", Long.MAX_VALUE-(new Date()).getTime() +":" + owner.getEmail());
		e.setProperty("body", pm.body);
		e.setProperty("owner", owner.getEmail());
		e.setProperty("date", date);
				
		//crée des signataire
		ArrayList<String> fset = new ArrayList<String>();
		fset.add(owner.getEmail());
		e.setProperty("signatory",fset);
		e.setProperty("nbSignatory",305);//fset.size());
				
		//crée des tags
		HashSet<String> fset2 = new HashSet<String>();
		e.setProperty("tag", fset2);
				
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = datastore.beginTransaction();
		datastore.put(e);
		txn.commit();
		return e;
	}
	
	//Charger les pétitions signées par l'utilisateur
	@ApiMethod(name = "MyPetition", httpMethod = HttpMethod.GET)
	public List<Entity> MyPetition(@Named("owner") User owner) throws UnauthorizedException {
		if (owner == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		Query q = new Query("Petition").setFilter(new FilterPredicate("signatory", FilterOperator.EQUAL, owner.getEmail()));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
		return result;
	}
	
	///Poste message
	
	@ApiMethod(name = "postMessage", httpMethod = HttpMethod.POST)
	public Entity postMessage(PostMessage pm) {

		Entity e = new Entity("Post"); // quelle est la clef ?? non specifié -> clef automatique
		e.setProperty("owner", pm.owner);
		e.setProperty("url", pm.url);
		e.setProperty("body", pm.body);
		e.setProperty("likec", 0);
		e.setProperty("date", new Date());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = datastore.beginTransaction();
		datastore.put(e);
		txn.commit();
		return e;
	}

	@ApiMethod(name = "mypost", httpMethod = HttpMethod.GET)
	public CollectionResponse<Entity> mypost(@Named("name") String name, @Nullable @Named("next") String cursorString) {

	    Query q = new Query("Post").setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, name));
	    
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    PreparedQuery pq = datastore.prepare(q);
	    
	    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);
	    
	    if (cursorString != null) {
		fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}
	    
	    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
	    cursorString = results.getCursor().toWebSafeString();
	    
	    return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	    
	}
    
	@ApiMethod(name = "getPost",
		   httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Entity> getPost(User user, @Nullable @Named("next") String cursorString)
			throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query q = new Query("Post").
		    setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, user.getEmail()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}

	@ApiMethod(name = "postMsg", httpMethod = HttpMethod.POST)
	public Entity postMsg(User user, PostMessage pm) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Entity e = new Entity("Post", Long.MAX_VALUE-(new Date()).getTime()+":"+user.getEmail());
		e.setProperty("owner", user.getEmail());
		e.setProperty("url", pm.url);
		e.setProperty("body", pm.body);
		e.setProperty("likec", 0);
		e.setProperty("date", new Date());
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = datastore.beginTransaction();
		datastore.put(e);
//		datastore.put(pi);
		txn.commit();
		return e;
	}
}
