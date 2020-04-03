package foo;


import java.util.List;
import java.util.Random;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
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


@Api(name = "myApi",
version = "v1",
namespace = @ApiNamespace(ownerDomain = "helloworld.example.com",
    ownerName = "helloworld.example.com",
    packagePath = ""))

public class ScoreEndpoint {
	
	Random r=new Random();

	@ApiMethod(name = "getRandom",	httpMethod = HttpMethod.GET)
	public RandomResult random() {
			return new RandomResult(r.nextInt(6)+1);
	}

	
	@ApiMethod(name = "scores",	httpMethod = HttpMethod.GET)
	public List<Entity> scores() {
			Query q =new Query("Score").addSort("score", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
			return result;
	}

	@ApiMethod(name = "topscores",
			httpMethod = HttpMethod.GET)
	public List<Entity> topscores() {
			Query q =new Query("Score").addSort("score", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
			return result;
	}

	@ApiMethod(name = "myscores",	httpMethod = HttpMethod.GET)
	public List<Entity> myscores(@Named("name") String name) {
			Query q =
			    new Query("Score")
			        .setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name))
			        .addSort("score", SortDirection.DESCENDING);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
			return result;
	}
	
	@ApiMethod(name = "addScore",	httpMethod = HttpMethod.GET)
	public Entity addScore(@Named("score") int score, @Named("name") String name) {
			
			Entity e = new Entity("Score", ""+name+score);
			e.setProperty("name", name);
			e.setProperty("score", score);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}
	@ApiMethod(name = "mypost",	httpMethod = HttpMethod.GET)
	public List<Entity> mypost(@Named("name") String name,@Named("lastkey") String lastkey) {
		Entity lk=new Entity("Post",lastkey);
			Query q =
			    new Query("Post").setFilter(CompositeFilterOperator.and(
			        new FilterPredicate("owner", FilterOperator.EQUAL, name),
					new FilterPredicate("__key__",FilterOperator.GREATER_THAN,lk.getKey())));
//			q.addProjection(new PropertyProjection(Entity.KEY_RESERVED_PROPERTY,String.class));
			q.addProjection(new PropertyProjection("body",String.class));
//			q.addProjection(new PropertyProjection("url",String.class));
			q.addProjection(new PropertyProjection("date",java.util.Date.class));
			q.addProjection(new PropertyProjection("likec",Integer.class));
//			q.addSort("date", SortDirection.DESCENDING);
			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(20));
			return result;
	}


}