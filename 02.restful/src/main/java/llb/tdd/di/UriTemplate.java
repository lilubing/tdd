package llb.tdd.di;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

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

class PathTemplate implements UriTemplate {

    private final Pattern pattern;
    private PathVariables pathVariables = new PathVariables();
    private int variableGroupStartFrom;

    public PathTemplate(String template) {
        pattern = Pattern.compile(group(pathVariables.template(template)) + "(/.*)?");
        variableGroupStartFrom = 2;
    }

    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new PathMatchResult(matcher, pathVariables));
    }

    class PathVariables implements Comparable<PathVariables> {
        private static final String LeftBracket = "\\{";
        private static final String RightBracket = "}";
        private static final String VariableName = "\\w[\\w\\.-]*";
        private static final String NonBracket = "[^\\{}]+";
        private static Pattern variable = Pattern.compile(LeftBracket + group(VariableName) +
                group(":" + group(NonBracket)) + "?" + RightBracket);
        private int variableNameGroup = 1;
        private int variablePatternGroup = 3;
        public static final String defaultVariablePattern = "([^/]+?)";
        private final List<String> variables = new ArrayList<>();
        private int specificPatterCount = 0;

        private String template(String template) {
            return variable.matcher(template).replaceAll(pathVariables::replace);
        }

        private String replace(java.util.regex.MatchResult result) {
            String variableName = result.group(variableNameGroup);
            String pattern = result.group(variablePatternGroup);

            if (variables.contains(variableName))
                throw new IllegalArgumentException("duplicate variable" + variableName);

            variables.add(variableName);
            if (pattern != null) {
                specificPatterCount++;
                return group(pattern);
            }
            return defaultVariablePattern;
        }

        public Map<String, String> extract(Matcher matcher) {
            Map<String, String> tmp = new HashMap<>();
            for (int i = 0; i < variables.size(); i++) {
                tmp.put(variables.get(i), matcher.group(variableGroupStartFrom + i));
            }
            return tmp;
        }

        @Override
        public int compareTo(PathVariables o) {
            if (variables.size() > o.variables.size()) return -1;
            if (variables.size() < o.variables.size()) return 1;
            return Integer.compare(o.specificPatterCount, specificPatterCount);
        }
    }

    class PathMatchResult implements MatchResult {
        private int matchLiteralCount;
        private PathVariables variables;
        private Matcher matcher;
        private Map<String, String> parameters;

        public PathMatchResult(Matcher matcher, PathVariables variables) {
            this.matcher = matcher;
            this.variables = variables;
            this.parameters = variables.extract(matcher);

            this.matchLiteralCount = matcher.group(1).length();
            IntStream.range(0, variables.variables.size()).forEach(i -> matchLiteralCount -= matcher.group(variableGroupStartFrom + i).length());
        }

        @Override
        public int compareTo(MatchResult o) {
            PathMatchResult result = (PathMatchResult) o;
            if (matchLiteralCount > result.matchLiteralCount) return -1;
            if (matchLiteralCount < result.matchLiteralCount) return 1;
            return variables.compareTo(result.variables);
            /*if (parameters.size() > result.parameters.size()) return -1;
            if (parameters.size() < result.parameters.size()) return 1;
            if (specificParameterCount > result.specificParameterCount) return -1;
            if (specificParameterCount < result.specificParameterCount) return 1;
            return 0;*/
        }

        @Override
        public String getMatched() {
            return matcher.group(1);
        }

        @Override
        public String getRemaining() {
            return matcher.group(matcher.groupCount());
        }

        @Override
        public Map<String, String> getMatchedPathParameters() {
            return parameters;
        }
    }

    private static String group(String pattern) {
        return "(" + pattern + ")";
    }
}
