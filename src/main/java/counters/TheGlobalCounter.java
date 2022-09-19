package counters;

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

import com.google.appengine.api.ThreadManager;
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
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@WebServlet(name = "TheCount", urlPatterns = { "/thecount" })
public class TheGlobalCounter extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Transaction txn = ds.beginTransaction();

		Entity e = new Entity("Counter", "TheCounter");
		try {
			Entity c = ds.get(e.getKey());
			Long v = (Long) c.getProperty("val");

			Thread.sleep(100);

			c.setProperty("val", v + 1);
			ds.put(c);

			txn.commit();

			response.getWriter().print("final value:" + ds.get(e.getKey()).getProperty("val"));
		} catch (EntityNotFoundException e2) {
			// TODO Auto-generated catch block
			e.setProperty("val", 0);
			ds.put(e);
			txn.commit();
			e2.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (txn.isActive()) {
				try {
					response.getWriter().print("Thread:" + Thread.currentThread() + " abort ");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				txn.rollback();
			}
		}

	}

}
