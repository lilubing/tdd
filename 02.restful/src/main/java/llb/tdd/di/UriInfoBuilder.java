package llb.tdd.di;

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
    /*void pushMatchedPath(String path);

    void addParameter(String string, String value);

    String getUnmatchedPath();*/

    Object getLastMatchedResource();

    void addMatchedResource(Object resource);
}
