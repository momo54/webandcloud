package foo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import com.google.appengine.repackaged.com.google.datastore.v1.CompositeFilter;
import com.google.appengine.repackaged.com.google.datastore.v1.Projection;
import com.google.appengine.repackaged.com.google.datastore.v1.PropertyFilter;

@WebServlet(name = "FriendQuery", urlPatterns = { "/query" })
public class FriendQuery extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

//		Entity e = new Entity("Friend", "f" + i);
//		e.setProperty("firstName", "first" + i);
//		e.setProperty("lastName", "last" + i);
//		e.setProperty("age", r.nextInt(100) + 1);
//      e.setProperty("friends", fset);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Friend").setFilter(new FilterPredicate("firstName", FilterOperator.EQUAL, "first0"));

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getProperty("firstName") + "," + entity.getProperty("lastName")
					+ "," + entity.getProperty("age"));
		}

		q = new Query("Friend")
				.setFilter(CompositeFilterOperator.and(
						new FilterPredicate("friends", FilterOperator.EQUAL, "f94"),
						new FilterPredicate("friends", FilterOperator.EQUAL, "f93"),
						new FilterPredicate("age", FilterOperator.GREATER_THAN_OR_EQUAL, 67),
						new FilterPredicate("age", FilterOperator.LESS_THAN_OR_EQUAL, 96) //and >= ??
						)); //and >= ??
		
		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getProperty("firstName"));
		}


		long t1=System.currentTimeMillis();
		q = new Query("Friend");
		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getProperty("firstName"));
		}
		long t2=System.currentTimeMillis();

		q = new Query("Friend");
		q.addProjection(new PropertyProjection("firstName",String.class));
		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getProperty("firstName"));
		}
		long t3=System.currentTimeMillis();
		response.getWriter().print("q1:"+(t2-t1)+","+"q2:"+(t3-t2));
		
		
	}
}
