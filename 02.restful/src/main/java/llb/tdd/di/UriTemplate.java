package llb.tdd.di;

import java.util.*;
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
    interface MatchResult extends Comparable<MatchResult> {
        String getMatched(); // 1

        String getRemaining(); // orders

        Map<String, String> getMatchedPathParameters();
    }

    Optional<MatchResult> match(String path);
}

class PathTemplate implements UriTemplate{
    private static final String LeftBracket ="\\{";
    private static final String RightBracket ="}";
    private static final String NonBracket ="[^\\{}]+";
    private static final String VariableName ="\\w[\\w\\.-]*";
    public static final String Remaining = "(/.*)?";
    private int  variableNameGroup = 1;
    private int  variablePatternGroup = 3;
    public static final String defaultVariablePattern = "([^/]+?)";
    private static Pattern variable = Pattern.compile(
            LeftBracket
                    + group(VariableName)
                    + group(":"+group(NonBracket))+"?"
                    + RightBracket
    );
    private final Pattern pattern;
    private final List<String> variables = new ArrayList<>();
    private int specificPatterCount = 0;
    private int variableGroupStartFrom;

    private static String group(String pattern){
        return "("+pattern+")";
    }

    public PathTemplate(String template) {
        pattern = Pattern.compile(group(template(template)) + Remaining);
        variableGroupStartFrom = 2;
    }

    private String template(String template) {
        return variable.matcher(template).replaceAll(result -> replace(result));
    }

    private String replace(java.util.regex.MatchResult result) {
        String variableName = result.group(variableNameGroup);
        String pattern = result.group(variablePatternGroup);

        if(variables.contains(variableName)) {
            throw new IllegalArgumentException("duplicate variable" + variableName);
        }

        variables.add(variableName);
        if(pattern != null) {
            specificPatterCount++;
            return group(pattern);
        }
        return defaultVariablePattern;
    }

    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if(!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new PathMatchResult(matcher));
    }

    class PathMatchResult implements MatchResult {
        private final int specificParameterCount;
        private int matchLiteralCount;
        private Matcher matcher;
        private int count;
        private Map<String, String> parameters;

        public PathMatchResult(Matcher matcher) {
            this.matcher = matcher;
            this.count = matcher.groupCount();
            this.matchLiteralCount = matcher.group(1).length();
            this.specificParameterCount = specificPatterCount;
            this.parameters = extracted(matcher);
        }

        private Map<String, String> extracted(Matcher matcher) {
            Map<String, String> tmp = new HashMap<>();
            for (int i = 0; i < variables.size(); i++) {
                tmp.put(variables.get(i), matcher.group(variableGroupStartFrom + i));
                matchLiteralCount -= matcher.group(variableGroupStartFrom + i).length();
            }
            return tmp;
        }

        @Override
        public int compareTo(MatchResult o) {
            PathMatchResult result = (PathMatchResult) o;
            if(matchLiteralCount > result.matchLiteralCount) return -1;
            if(matchLiteralCount < result.matchLiteralCount) return 1;
            if(parameters.size() > result.parameters.size()) return -1;
            if(parameters.size() < result.parameters.size()) return 1;
            if (specificParameterCount > result.specificParameterCount) return -1;
            if (specificParameterCount < result.specificParameterCount) return 1;
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
            return parameters;
        }
    }
}
