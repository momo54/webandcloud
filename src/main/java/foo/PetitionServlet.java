package foo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

@WebServlet(name = "PetServlet", urlPatterns = { "/petition" })
public class PetitionServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		Random r = new Random();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		// Create users
			for (int i = 0; i < 500; i++) {
					Date date = new Date(); // voir score endpoint clé
					Entity e = new Entity("Petition", Long.MAX_VALUE-(new Date()).getTime() + "p" + i);
					e.setProperty("body", "hello world");
					e.setProperty("owner", "u"+r.nextInt(1000));
					e.setProperty("date", date);
					
					//crée des signataire
					HashSet<String> fset = new HashSet<String>();
					for (int j=0; j < 200;j++) {
						fset.add("u" + r.nextInt(1000));
					}
					e.setProperty("signatory",fset);
					e.setProperty("nbSignatory", fset.size());
					
					//crée des tags
					HashSet<String> fset2 = new HashSet<String>();
					while (fset2.size() < 10) {
						fset2.add("t" + r.nextInt(200));
					}
					e.setProperty("tag", fset2);
					
					datastore.put(e);
					response.getWriter().print("<li> created post:" + e.getKey() + "<br>");
			}
	}
}