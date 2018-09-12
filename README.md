# FYW Furman: Working with Digital Editions in Scala

## Getting Started

You need to have [SBT](http://www.scala-sbt.org) installed and working. If you are running in the [Fall 2018 VM](https://eumaeus.github.io/fall2018vm/), you are all set.

You will need to be online, at least the first time you run this.

In a terminal, navigate to the `fyw-scala` directory and type

> `sbt console`

From with the sbt console,

> `:load tools.sc` (type the initial colon!)

This loads the file `data/plutarch_women.cex` and creates:

1. `library` : an OHCO2 library of texts
1. `corpus` : the complete corpus

## Getting Out

- `:quit` exits Scala Console

