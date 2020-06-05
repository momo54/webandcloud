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
import com.google.appengine.api.datastore.Transaction;

@WebServlet(name = "CountingSH", urlPatterns = { "/countsh" })
public class CountingSh extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> mycounter = new ArrayList<>(); 
		for (int i=0;i<20;i++) {
			Entity e;
			mycounter.add(e=new Entity("Count", i+"c1")); // 0c1, 2c1, 3c1 ... , 19c1
			e.setProperty("val", 0);
			datastore.put(mycounter);
		}


		Thread[] th=new Thread[2];
		Random random=new Random();
		for (int i=0;i<th.length;i++) {			
			th[i]=ThreadManager.createThreadForCurrentRequest(new Runnable()  {
				public void run() {
					DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
					for (int j=0;j<10;j++) {
						Transaction txn=ds.beginTransaction();
						try {
							int randomc=random.nextInt(mycounter.size());
							Entity c = datastore.get(mycounter.get(randomc).getKey());
							Long v=(Long)c.getProperty("val");
							// UN SLEEP DE CONTENTION
							Thread.sleep(100);
							c.setProperty("val", v+1);
							response.getWriter().print("Thread:"+Thread.currentThread()+",entity"+c.getKey()+",val:"+(v)+"<br>");
							ds.put(c);
							txn.commit();
						} catch (EntityNotFoundException | IOException  e) {
//						} catch (EntityNotFoundException | IOException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							if (txn.isActive()) {
							    txn.rollback();
							  }
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
			Long count=(long) 0;
			for (Entity e : mycounter) {
				count+=(long)datastore.get(e.getKey()).getProperty("val");
			} 
			response.getWriter().print("final value:"+count);
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}