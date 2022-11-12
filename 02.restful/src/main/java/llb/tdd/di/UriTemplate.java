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
    //@Path("{id}") /1/orders
    interface MatchResult extends Comparable<MatchResult> {
        String getMatched(); // 1

        String getRemaining(); // orders

        Map<String, String> getMatchedPathParameters();
    }

    Optional<MatchResult> match(String path);
}

class UriTemplateString implements UriTemplate{

    private static final String LeftBracket ="\\{";
    private static final String RightBracket ="}";
    private static final String NonBracket ="[^\\{}]+";
    private static final String VariableName ="\\w[\\w\\.-]*";
    public static final String Remaining = "(/.*)?";
    public static final String defaultVariablePattern = "([^/]+?)";
    private static Pattern variable = Pattern.compile(
            LeftBracket
                    + group(VariableName)
                    + group(":"+group(NonBracket))+"?"
                    + RightBracket
    );
    private final Pattern pattern;
    private final List<String> variables = new ArrayList<>();
    private int variableGroupStartFrom;
    private int  variableNameGroup = 1;
    private int  variablePatternGroup = 3;

    private static String group(String pattern){
        return "("+pattern+")";
    }

    public UriTemplateString(String template) {
        pattern = Pattern.compile(group(variable(template)) + Remaining);
        variableGroupStartFrom = 2;
    }

    private String variable(String template) {
        return variable.matcher(template).replaceAll(result -> {
            String variableName = result.group(variableNameGroup);
            String pattern = result.group(variablePatternGroup);

            if(variables.contains(variableName)) {
                throw new IllegalArgumentException("duplicate variable" + variableName);
            }

            variables.add(variableName);
            return pattern == null ? defaultVariablePattern : group(pattern);
        });
    }

    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if(!matcher.matches()) {
            return Optional.empty();
        }
        int count = matcher.groupCount();
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            parameters.put(variables.get(i), matcher.group(variableGroupStartFrom + i));
        }


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
                return parameters;
            }
        });
    }
}
