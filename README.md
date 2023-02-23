# Election Results
This project is meant to contain functionality to calculate the results of elections based on ballots.

Implementation is mainly focussed on the local elections in Schleswig-Holstein, which is the northernmost state of Germany.

[JavaDoc](https://lars-sh.github.io/election-results/apidocs)  |  [Generated Reports](https://lars-sh.github.io/election-results/project-reports.html)

## Getting started

### Building Sources
The project uses Maven and Project Lombok. First, make sure to install Project Lombok into your IDE, then most IDEs should be able to import the Maven project with a glance.

On the shell the sources of this project can be built using `mvn clean install`. To execute that command Project Lombok does not need to be installed.

### Execution
This project's Command Line Interface (CLI) comes with a built-in help. In your IDE you only need to start the class `de.larssh.election.germany.schleswigholstein.local.cli.LocalElectionResultCli`.

As long as Maven is installed on your machine and you executed `mvn install` before, you can also execute the following shell command:

```
mvn --quiet de.lars-sh:jar-runner-maven-plugin:run -Dartifact=de.lars-sh.election-results:election-results-cli:0.9.0-SNAPSHOT -DmainClass=de.larssh.election.germany.schleswigholstein.local.cli.LocalElectionResultCli
```

## Differentiating Direct and List Nominations
Officially the order of direct and list nominations of a party could differ. We consider that a very rare case. This does no

Therefore, this project works with one order only, automatically taking the first nominations as direct nominations. Results are correct as long as all direct nominations have a place at the beginning of the party's list. In that case the order of the direct nominations does not matter.

## Open Points
* Move methods for statistic needs from the formatting classes to utility classes
* Tests for isCertain
* Test for a rare problem with DIRECT_BALANCE_SEAT or LIST_OVERHANG_SEAT
