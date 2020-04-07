package foo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * Servlet implementation class Consistency
 */
@WebServlet("/Consistency")
public class Consistency extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Consistency() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void cleaning() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		// cleaning
		for (int i=0;i<3;i++) {
		Query q = new Query("Person");
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		for (Entity entity : result) {
			datastore.delete(entity.getKey());
			}	
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		response.getWriter().append("Served at: ").append(request.getContextPath());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		this.cleaning();

		Thread t1 = ThreadManager.createThreadForCurrentRequest(new Runnable() {
			public void run() {
				DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
				// populating
				for (int i = 0; i < 10; i++) {
					Entity e = new Entity("Person", "p" + i);
					e.setProperty("lastname", "l" + i);
					e.setProperty("firstname", "f" + i);
					datastore.put(e);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		Thread tparent = ThreadManager.createThreadForCurrentRequest(new Runnable() {
			public void run() {
				DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
				Entity dad = new Entity("Human");
				datastore.put(dad);
				// populating
				Transaction txn = datastore.beginTransaction();

				for (int i = 0; i < 10; i++) {
					Entity e = new Entity("Person", dad.getKey()); // p1:Human
					e.setProperty("lastname", "l" + i);
					e.setProperty("firstname", "f" + i);
					datastore.put(e);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				txn.commit();
			}
		});

		Thread[] th=new Thread[2];
		for (int i=0;i<th.length;i++) {
			th[i] = ThreadManager.createThreadForCurrentRequest(new Runnable() {
				public void run() {
					Query q = new Query("Person");
					PreparedQuery pq = datastore.prepare(q);
	
	//				txn = datastore.beginTransaction();
					FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
					Iterator<Entity> results = pq.asIterator(fetchOptions);
					while (results.hasNext()) {
						try {
							Thread.sleep(100);
							response.getWriter().append("<li>" + results.next().getKey());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	//				txn.commit();
				}
			});
		}

		response.getWriter().append("<h1> Inserting while quering Single TR</h1>");

		t1.start();
		th[0].start();

		try {
			t1.join();
			th[0].join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		response.getWriter().append("<h1> Inserting while quering Entity Group</h1>");

		
		this.cleaning();
		tparent.start();
		th[1].start();

		try {
			tparent.join();
			th[1].join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
