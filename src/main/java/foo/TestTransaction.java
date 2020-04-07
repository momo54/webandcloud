package foo;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

/**
 * Servlet implementation class Transaction
 */
@WebServlet("/TestTransaction")
public class TestTransaction extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		for (int i=0;i<2;i++) {
		Entity e=new Entity("Person","u"+i);
		datastore.put(e);
		}
		
		// cleaning
		Transaction txn = datastore.beginTransaction(); // error -> single row transaction
		Query q = new Query("Person");
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		for (Entity entity : result) {
//			Transaction txn = datastore.beginTransaction();
			datastore.delete(entity.getKey());
//			txn.commit();

		}
		txn.commit();
	}
}
