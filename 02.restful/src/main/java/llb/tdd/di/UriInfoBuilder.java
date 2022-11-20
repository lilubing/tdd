package llb.tdd.di;

import jakarta.ws.rs.core.UriInfo;

import java.util.Map;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: UriInfoBuilder
 * @date 2022-11-10 下午8:22
 * @ProjectName tdd
 * @Version V1.0
 */
public interface UriInfoBuilder {

    Object getLastMatchedResource();

    void addMatchedResource(Object resource);

    void addMatchedPathParameters(Map<String, String> pathParameters);

    UriInfo createUriInfo();
}
