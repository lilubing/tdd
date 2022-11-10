package llb.tdd.di;

import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: UriTemplate
 * @date 2022-11-10 下午8:24
 * @ProjectName tdd
 * @Version V1.0
 */
interface UriTemplate extends Comparable<MatchResult> {
    //@Path("{id}") /1/orders
    interface MatchResult {
        String getMatched(); // 1

        String getRemaining(); // orders

        Map<String, String> getMatchedPathParameters();
    }

    Optional<MatchResult> match(String path);
}
