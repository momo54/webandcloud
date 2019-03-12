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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

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

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Friend").setFilter(new FilterPredicate("firstName", FilterOperator.EQUAL, "first0"));

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + result.size() +"<br>");
		for (Entity entity : result) {
			response.getWriter().print("<li>"+ 
					entity.getProperty("firstName")+","+ 
					entity.getProperty("lastName")+","+
					entity.getProperty("age")
					);
		}
	}
}
