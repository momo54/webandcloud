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

@WebServlet(name = "PrefixQuery", urlPatterns = { "/prefixquery" })
public class PrefixQuery extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		response.getWriter().print("<h1> Querying Post Data </h1>");


		response.getWriter().print("<h2> 2 Posts with key prefix f1 </h2>");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key k = KeyFactory.createKey("Post", "f1:");

		// query on key prefix
		Query q = new Query("Post").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, k));

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(2));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		Entity last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey());
			last=entity;
		}

		response.getWriter().print("<h2> 10 Posts with key prefix f1:2020 </h2>");

		k = KeyFactory.createKey("Post", "f1:2020");
		q = new Query("Post").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, k));

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey());
			last=entity;
		}

		// Get all post where "f1" is in the receiver list (the 'to' list)
		response.getWriter().print("<h2> 10 posts with f1 as receiver </h2>");

		q = new Query("Post").setFilter(new FilterPredicate("to", FilterOperator.EQUAL, "f1"));

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getProperty("body"));
			last=entity;
		}
		
		// comment liker ??
		// comment liker last.getKey()???
		
		response.getWriter().print("<h2> Does f1 liked the last post </h2>");
		
		// Does "f1" liked the last post ??
		Key post=last.getKey();
		response.getWriter().print("examining:"+post);
		q = new Query("Post").setFilter(CompositeFilterOperator.and(
				new FilterPredicate("like", FilterOperator.EQUAL, "f1"),
				new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, post)));

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(1));
		if (result.size()>0) {
			response.getWriter().print("nothing to do");
		} 
		// write the entity with the "f1" in post.like...
		response.getWriter().print("adding f1 anyway");
		response.getWriter().print("last key:"+last.getKey()+", like:"+last.getProperty("like"));
//		last.setProperty("like", last.getProperty("like")+" f1"));
		//last.setProperty("count",last.)
		datastore.put(last);
	}
}
