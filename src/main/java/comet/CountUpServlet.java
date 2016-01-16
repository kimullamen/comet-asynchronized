package comet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CountUpServlet", urlPatterns = { "/countUp" }, asyncSupported = true)
public class CountUpServlet extends HttpServlet {
	// countUp対象
	int count;
	List<AsyncContext> queue = new ArrayList<>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		final AsyncContext context = req.startAsync();
		queue.add(context);
	}

	@Override
	public void init() throws ServletException {
		super.init();
		// 1sごとにインクリメントする
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				count++;
				broadcast();
			}
		};
		timer.schedule(task, 5000, 5000);
	}

	synchronized public void broadcast() {
		CopyOnWriteArrayList<AsyncContext> target = new CopyOnWriteArrayList<>(queue);
		synchronized (queue) {
			queue = new ArrayList<>();
		}

		for (AsyncContext context : target) {
			HttpServletResponse resp = (HttpServletResponse) context.getResponse();
			resp.setContentType("application/json");
			try {
				PrintWriter writer = resp.getWriter();
				writer.write("{\"count\":\"" + count + "\"}");
				writer.close();
				context.complete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
