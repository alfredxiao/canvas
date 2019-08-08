# coverlet

A Clojure leiningen plugin that reports on test coverage.

## Usage

### Latest Version
`[coverlet "0.1.2"]`

### Add a plugin reference in your project.clj:
```
:plugins [[coverlet "0.1.2"]]
```

### Run the plugin (in another project):
`lein coverlet`

### Sample output:

``` 

== Coverlet Test Coverage Report ==
toy-robot.core
  + play             T  5
  + stdin->commands  /  /
  - go               T 30
  - valid?           T 24
  + file->commands   /  /
  - stdin->line-seq  /  /
toy-robot.main
  + -main            /  /
toy-robot.parser
  + line->command    T  9
  - is-digit?        T  2
  + lines->commands  /  /
====================================

```

## License

Copyright © 2019 Alfred Xiao

