# Just For Fun Code

The project contains just for fun code I write when I have free time.

## Getting Started

This is [sbt]http://www.scala-sbt.org/ project you can import it into your IDE to browser code.

### Prerequisites

You should have sbt installed to compile the scala and java code in this project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Modules

The root sbt project contains several sub projects

### Parser Project

The project contains sample use of [Scala Parser Combinators]https://github.com/scala/scala-parser-combinators to
parse a sample "query" strings.

I often have to write a service which receives a query for some data, parses it and creates SQL expression or
use hibernate query builders based on the query.
Usually this task is implemented as AST like structure as input which is translated into SQL.
This approach is very good for computers, but it's hard to read such queries for user.
Parser Combinators allow to quickly write code which can read and parse your own DSL for queries.
And it is easier to read DSL than object model for human.

### Join Project

It's often required to implement INNER JOIN operation in java or scala code.
The code in the Join Project contains an abstract service for joining collections in memory.

### Changelog Project

Sometime it's required to track changes to value objects.
For example master service is changing some entities and slave service should listen to changes
from the first service and maintain a mirror database of the entities.
I implemented an idea for such mirroring using Van Laarhoven lenses.

### Performance Tests Project

Just contains [JMH]http://openjdk.java.net/projects/code-tools/jmh/ benchmarks for other projects.
Performance tests could be run from sbt shell with following command;
```
jmh:run -i 20 -wi 20 -f1 -t1
```
