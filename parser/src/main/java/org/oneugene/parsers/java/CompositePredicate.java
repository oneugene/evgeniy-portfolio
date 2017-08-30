package org.oneugene.parsers.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object for AST-like tree node which contains information about an operation on other AST-like nodes
 */
public final class CompositePredicate implements Predicate {
    private final String operation;

    private final List<Predicate> children;

    public CompositePredicate(Builder builder) {
        this.operation = builder.operation;
        this.children = builder.children == null ? Collections.<Predicate>emptyList() : new ArrayList<Predicate>(builder.children);
    }

    /**
     *
     * @return operation which should be applied to children
     */
    public String getOperation() {
        return operation;
    }

    /**
     *
     * @return list of AST-nodes to use as parameters for the @link CompositePredicate#getOperation
     */
    public List<Predicate> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public String toString() {
        return "CompositePredicate{" +
                "operation='" + operation + '\'' +
                ", children=" + children +
                '}';
    }

    public static class Builder {
        private String operation;

        private List<Predicate> children;

        public String getOperation() {
            return operation;
        }

        public Builder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public List<Predicate> getChildren() {
            return children;
        }

        public Builder setChildren(List<Predicate> children) {
            this.children = children;
            return this;
        }

        public CompositePredicate build() {
            return new CompositePredicate(this);
        }
    }
}
