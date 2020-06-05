package foo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * Servlet implementation class MsgIndex
 */
@WebServlet("/MsgIndex")
public class MsgIndex extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MsgIndex() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.getWriter().append("<h1> Msg, MsgIndex and ParentKey</h1>");
		
		Entity e = new Entity("Msg", Long.MAX_VALUE-(new Date()).getTime()+":"+"momo44@gmail.com");
		e.setProperty("owner", "momo44@gmail.com");
		e.setProperty("url", "my URL");
		e.setProperty("body", "my Body");
		e.setProperty("likec", 0);
		e.setProperty("date", new Date());

		// 	Solution pour pas projeter les listes
		Entity pi = new Entity("MsgIndex", e.getKey()); // Notice that the message e is the parent of pi.
		HashSet<String> rec=new HashSet<String>();
		rec.add("riri"); rec.add("fifi"); rec.add("loulou");
		pi.setProperty("receivers",rec);

		// 	Solution pour pas projeter les listes
		Entity pi2 = new Entity("MsgIndex", e.getKey()); // Notice that the message e is the parent of pi.
		HashSet<String> rec2=new HashSet<String>();
		rec2.add("ruru"); rec2.add("fufu"); rec2.add("lolo");
		pi2.setProperty("receivers",rec2);

		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(e);
		datastore.put(pi);
		datastore.put(pi2);


		response.getWriter().append("<li> Key of Msg:"+e.getKey());
		response.getWriter().append("<li> Key of MsgIndex:"+pi.getKey());
		response.getWriter().append("<li> Key of MsgIndex:"+pi2.getKey());
		

		Query q = new Query("MsgIndex").setFilter(new FilterPredicate("receivers", FilterOperator.EQUAL, "fifi"));
		q.setKeysOnly(); // So receivers are not loaded in memory ie. not surfaced

		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());

		ArrayList<Key> keys=new ArrayList<Key>();
		for (Entity entity : result) {
			keys.add(entity.getParent());
		}
		
		Map<Key, Entity> msgs=datastore.get(keys); // Get all keys in parallel
		for (Entity msg : msgs.values()) {
			response.getWriter().append("<li>"+msg.getProperty("body"));
		}
		
	}


}
