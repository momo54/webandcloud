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

		k = KeyFactory.createKey("Post", "f1:2019");
		q = new Query("Post").setFilter(new FilterPredicate("__key__", FilterOperator.GREATER_THAN, k));

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey());
			last=entity;
		}

		
	}
}
