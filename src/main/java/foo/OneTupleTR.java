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
import com.google.appengine.api.datastore.TransactionOptions;

//Exception in thread "Thread-18" java.lang.IllegalArgumentException: cross-group transaction need to be explicitly specified, see TransactionOptions.Builder.withXGfound both Element {
//	  type: "Reg"
//	  name: "x"
//	}
//	 and Element {
//	  type: "Reg"
//	  name: "y"
//	}


@WebServlet(name = "OneTupleTR", urlPatterns = { "/onetuple" })
public class OneTupleTR extends HttpServlet {
	public static int j =5;
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity xe = new Entity("Reg", "x");
		xe.setProperty("val", 0);
		datastore.put(xe);

		Entity ye = new Entity("Reg", "y");
		ye.setProperty("val", 0);
		datastore.put(ye);

		
		try {
			response.getWriter().println("initial value: x("+datastore.get(xe.getKey()).getProperty("val")+"),y("+datastore.get(ye.getKey()).getProperty("val")+")<br>");
		} catch (EntityNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		Thread[] th=new Thread[2];
		for (int i=0;i<th.length;i++) {			
			th[i]=ThreadManager.createThreadForCurrentRequest(new Runnable()  {
				public void run() {
					DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
					// Cross Group transaction
					// without -> exception Cross Group TR
					TransactionOptions options = TransactionOptions.Builder.withXG(true);
					Transaction txn=ds.beginTransaction(options);
					try {
						Entity x = datastore.get(xe.getKey());
						Long xv=(Long)x.getProperty("val");
						Entity y = datastore.get(ye.getKey());
						Long yv=(Long)x.getProperty("val");

						// UN SLEEP DE CONTENTION
						Thread.sleep(100);
						x.setProperty("val", xv+1);
						response.getWriter().print("Thread:"+Thread.currentThread()+",xval:"+xv+",yval:"+yv+"<br>");
						y.setProperty("val", xv+1);
						ds.put(x);
						ds.put(y);
						txn.commit();
					} catch (EntityNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} finally {
						if (txn.isActive()) {
							try {
								response.getWriter().print("Thread:"+Thread.currentThread()+" abort ");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
									}
							txn.rollback();
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
			response.getWriter().print("final value x(:"+datastore.get(xe.getKey()).getProperty("val")+")");
			response.getWriter().print("final value y(:"+datastore.get(ye.getKey()).getProperty("val")+")");
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}