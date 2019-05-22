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

@WebServlet(name = "FbServlet", urlPatterns = { "/fb" })
public class FriendBlockServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

	response.setContentType("text/html");
	response.setCharacterEncoding("UTF-8");

	Random r = new Random();

	// Create users
	for (int i = 0; i < 500; i++) {
		Entity e = new Entity("Friend", "f" + i);
		e.setProperty("firstName", "first" + i);
		e.setProperty("lastName", "last" + i);
		e.setProperty("age", r.nextInt(100) + 1);


		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		response.getWriter().print("<li> created friend:" + e.getKey() + "<br>" );

		
		for (int j=0;j<10;j++) {
		// Create user friends
			Entity amis = new Entity("Amis","a"+j,e.getKey()); 
			HashSet<String> fset = new HashSet<String>();
			while (fset.size() < 200) {
				fset.add("f" + r.nextInt(500));
			}
			amis.setProperty("friends", fset);
			datastore.put(amis);
			response.getWriter().print("<li> created amis:"+ amis.getKey() + "<br>" );
		}

	}
}
}