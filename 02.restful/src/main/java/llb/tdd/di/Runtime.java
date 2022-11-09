package llb.tdd.di;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.ext.Providers;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: Runtime
 * @date 2022-11-09 7:36:41
 * @ProjectName tdd
 * @Version V1.0
 */
public interface Runtime {
	Providers getProviders();

	ResourceContext createResourceContext(HttpServletRequest request, HttpServletResponse response);

	Context getApplicationContext();

	ResourceRouter getResourceRouter();

}
