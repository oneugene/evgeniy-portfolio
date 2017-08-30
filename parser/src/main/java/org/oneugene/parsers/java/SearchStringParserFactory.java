package org.oneugene.parsers.java;

import org.codehaus.jparsec.Parser;

/**
 * Describes methods to create parser to parse search string into AST-like structure
 */
public interface SearchStringParserFactory {
    Parser<Predicate> expression();
}
