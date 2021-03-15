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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.datastore.v1.CompositeFilter;
import com.google.appengine.repackaged.com.google.datastore.v1.Projection;
import com.google.appengine.repackaged.com.google.datastore.v1.PropertyFilter;

@WebServlet(name = "PetQuery", urlPatterns = { "/pquery" })
public class PetitionQuery extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");


		/*response.getWriter().print("<h2> finall 5 PU where key > P0 </h2>");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key k = KeyFactory.createKey("PU", "P0");

		Query q = new Query("PU").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, k));

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(5));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		Entity last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey());
			last=entity;
		}*/

		response.getWriter().print("<h2>Q1 : Great, get the next 10 results now </h2>");

		long t1=System.currentTimeMillis();

		// One way to paginate...
		Query q = new Query("Petition");//.setFilter(new FilterPredicate("__key__", FilterOperator.GREATER_THAN, last.getKey()));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		Entity last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey()+ "/" + entity.getProperty("owner"));
			last=entity;
		}
		long t2=System.currentTimeMillis();
		response.getWriter().print("<h2> time(Q1) </h2>");		
		response.getWriter().print("q1:"+(t2-t1)+"\n");
		
		response.getWriter().print("<h2>Q2 : petition signée par l'utilisateur u290 </h2>");
		
		//Key k = KeyFactory.createKey("Petition", "9223370421632728681p496");
		q = new Query("Petition").setFilter(new FilterPredicate("signatory", FilterOperator.EQUAL, "u290"));
		datastore = DatastoreServiceFactory.getDatastoreService();

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey()+ "/" + entity.getProperty("owner"));
			last=entity;
		}
		long t3=System.currentTimeMillis();
		response.getWriter().print("<h2> time(Q2) </h2>");		
		response.getWriter().print("q2:"+(t3-t2)+"\n");
		
		response.getWriter().print("<h2>Q3 : petitions les plus signées </h2>");
		
		//k = KeyFactory.createKey("Petition", "u2");
		q = new Query("Petition").addSort("nbSignatory", SortDirection.DESCENDING);
		datastore = DatastoreServiceFactory.getDatastoreService();

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey()+ "/" + entity.getProperty("owner"));
			last=entity;
		}
		long t4=System.currentTimeMillis();
		response.getWriter().print("<h2> time(Q3) </h2>");		
		response.getWriter().print("q3:"+(t4-t3)+"\n");
		
		/*response.getWriter().print("<h2> petitions séléctionnées par tag </h2>");
		HashSet<String> fset2 = new HashSet<String>();
		fset2.add("t1");
		fset2.add("t120");
		q = new Query("Petition").setFilter(CompositeFilter.and()
		        PropertyFilter.eq("tag", "t1"), PropertyFilter.eq("tag", "t120")));
		datastore = DatastoreServiceFactory.getDatastoreService();

		pq = datastore.prepare(q);
		result = pq.asList(FetchOptions.Builder.withLimit(10));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		last=null;
		for (Entity entity : result) {
			response.getWriter().print("<li>" + entity.getKey()+ "/" + entity.getProperty("owner")+ "/" + entity.getProperty("nbSignatory"));
			last=entity;
		}*/
		
	}
}
