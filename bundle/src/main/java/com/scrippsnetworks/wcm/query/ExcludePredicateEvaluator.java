package com.scrippsnetworks.wcm.query;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.AbstractPredicateEvaluator;
import com.day.cq.search.eval.EvaluationContext;
import org.apache.felix.scr.annotations.Component;

import javax.jcr.query.Row;

@Component(metatype = false, factory = "com.day.cq.search.eval.PredicateEvaluator/exclude")
public class ExcludePredicateEvaluator extends AbstractPredicateEvaluator {

    @Override
    public boolean canXpath(Predicate predicate, EvaluationContext context) {
        return false;
    }

    @Override
    public boolean canFilter(Predicate predicate, EvaluationContext context) {
        return true;
    }

    @Override
    public boolean includes(Predicate predicate, Row row, EvaluationContext context) {
        String path = context.getPath(row);
        for (String pathToExclude : predicate.getParameters().values()) {
            if (path.startsWith(pathToExclude)) {
                return false;
            }
        }
        return true;
    }
}