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

====================================
toy-robot.core
  + play             T 5
  + stdin->commands    0
  - go               T 30
  - valid?           T 24
  + file->commands     0
  - stdin->line-seq    0
toy-robot.main
  + -main              0
toy-robot.parser
  + line->command    T 9
  - is-digit?        T 2
  + lines->commands    0
====================================

```

## License

Copyright © 2019 Alfred Xiao

