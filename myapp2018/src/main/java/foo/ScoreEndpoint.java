package foo;


import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;


@Api(name = "myApi",
version = "v1",
namespace = @ApiNamespace(ownerDomain = "helloworld.example.com",
    ownerName = "helloworld.example.com",
    packagePath = ""))

public class ScoreEndpoint {
	
	

	@ApiMethod(name = "listScore")
	public List<Entity> listScoreEntity(@Named("name") String name) {
			Query q =
			    new Query("Score")
			        .setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name));

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
	}
	
	@ApiMethod(name = "addScore")
	public Entity addScore(@Named("score") int score, @Named("name") String name) {
			
			Entity e = new Entity("Score", ""+name+score);
			e.setProperty("name", name);
			e.setProperty("score", score);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}

}