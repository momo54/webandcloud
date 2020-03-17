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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@WebServlet(name = "CountServlet", urlPatterns = { "/count" })
public class Counting extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity e = new Entity("Count", "c1");
		e.setProperty("val", 0);
		datastore.put(e);

		try {
			response.getWriter().println("initial value:"+datastore.get(e.getKey()).getProperty("val")+"<br>");
		} catch (EntityNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		Thread[] th=new Thread[2];
		for (int i=0;i<th.length;i++) {			
			th[i]=ThreadManager.createThreadForCurrentRequest(new Runnable()  {
				public void run() {
					DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
					for (int j=0;j<10;j++) {
						try {
							Entity c = datastore.get(e.getKey());
							Long v=(Long)c.getProperty("val");
							 Thread.sleep(100);
							c.setProperty("val", v+1);
							response.getWriter().print("Thread:"+Thread.currentThread()+",val:"+v+"<br>");
							ds.put(c);
						} catch (EntityNotFoundException | InterruptedException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			th[i].start();
		}

		for (Thread thread : th) {
			try {
				thread.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		try {
			response.getWriter().print("final value:"+datastore.get(e.getKey()).getProperty("val"));
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}