package llb.tdd.di;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: UriTemplate
 * @date 2022-11-10 下午8:24
 * @ProjectName tdd
 * @Version V1.0
 */
interface UriTemplate {
    //@Path("{id}") /1/orders
    interface MatchResult extends Comparable<MatchResult> {
        String getMatched(); // 1

        String getRemaining(); // orders

        Map<String, String> getMatchedPathParameters();
    }

    Optional<MatchResult> match(String path);
}

class UriTemplateString implements UriTemplate{

    private static Pattern variable = Pattern.compile("\\{\\w[\\w\\.-]*\\}");
    private final Pattern pattern;

    public UriTemplateString(String template) {
        pattern = Pattern.compile("(" + variable(template) + ")" + "(/.*)?");
    }

    private static String variable(String template) {
        return variable.matcher(template).replaceAll("([^/]+?)");
    }

    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if(!matcher.matches()) {
            return Optional.empty();
        }
        int count = matcher.groupCount();
        return Optional.of(new MatchResult() {

            @Override
            public int compareTo(MatchResult o) {
                return 0;
            }

            @Override
            public String getMatched() {
                return matcher.group(1);
            }

            @Override
            public String getRemaining() {
                return matcher.group(count);
            }

            @Override
            public Map<String, String> getMatchedPathParameters() {
                return null;
            }
        });
    }
}
