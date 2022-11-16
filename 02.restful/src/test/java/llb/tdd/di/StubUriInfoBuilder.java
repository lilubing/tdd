package llb.tdd.di;

import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: StubUriInfoBuilder
 * @date 2022-11-14 上午6:34
 * @ProjectName tdd
 * @Version V1.0
 */
class StubUriInfoBuilder implements UriInfoBuilder {
    private List<Object> matchedResult = new ArrayList<>();

    public StubUriInfoBuilder() {
    }

    @Override
    public Object getLastMatchedResource() {
        return matchedResult.get(matchedResult.size() - 1);
    }

    @Override
    public void addMatchedResource(Object resource) {
        matchedResult.add(resource);
    }

    @Override
    public UriInfo createUriInfo() {
        return null;
    }
}
