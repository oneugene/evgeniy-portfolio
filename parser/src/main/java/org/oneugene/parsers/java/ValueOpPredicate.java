package org.oneugene.parsers.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object for AST-like tree node which contains information about an operation on a list of values
 */
public final class ValueOpPredicate implements Predicate {
    private final String operation;

    private final String attribute;

    private final List<String> values;

    public ValueOpPredicate(Builder builder) {
        this.operation = builder.operation;
        this.attribute = builder.attribute;
        this.values = builder.values == null ? Collections.<String>emptyList() : new ArrayList<>(builder.values);
    }

    /**
     *
     * @return operation on variable and values, e.g. equals
     */
    public String getOperation() {
        return operation;
    }

    /**
     *
     * @return variable name to compare with values
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     *
     * @return list of values to compare with variable
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return "ValueOpPredicate{" +
                "operation='" + operation + '\'' +
                ", attribute='" + attribute + '\'' +
                ", values=" + values +
                '}';
    }

    public static class Builder {
        private String operation;

        private String attribute;

        private List<String> values;

        public String getOperation() {
            return operation;
        }

        public Builder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public String getAttribute() {
            return attribute;
        }

        public Builder setAttribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public List<String> getValues() {
            return values;
        }

        public Builder setValues(List<String> values) {
            this.values = values;
            return this;
        }

        public ValueOpPredicate build() {
            return new ValueOpPredicate(this);
        }
    }

}
