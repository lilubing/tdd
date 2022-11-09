package llb.tdd.di;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.io.IOException;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: ResourceServlet
 * @date 2022-11-09 7:33:41
 * @ProjectName tdd
 * @Version V1.0
 */
public class ResourceServlet extends HttpServlet {
	private Runtime runtime;

	public ResourceServlet(Runtime runtime) {
		this.runtime = runtime;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ResourceRouter router = runtime.getResourceRouter();
		OutboundResponse response = router.dispatch(req, runtime.createResourceContext(req, resp));
		resp.setStatus(response.getStatus());
		MultivaluedMap<String, Object> headers = response.getHeaders();
		for (String name : headers.keySet()) {
			for (Object value : headers.get(name)) {
				RuntimeDelegate.HeaderDelegate headerDelegate = RuntimeDelegate.getInstance().createHeaderDelegate(value.getClass());
				resp.addHeader(name, headerDelegate.toString(value));
			}
		}
	}
}