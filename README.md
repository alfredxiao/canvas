# canvas

A Clojure leiningen plugin that reports on test coverage.

## Usage

### Build the plugin:
`lein install`

### Add a plugin reference in your project.clj:
```
:local-repo "/Users/alfredxiao/.m2/repository"
:plugins [[canvas "0.1.0"]]
```

### Run the plugin (in another project):
`lein canvas`

### Sample output:
```
Testing toy-robot.core-test

Testing toy-robot.parser-test

Ran 8 tests containing 14 assertions.
0 failures, 0 errors.
{toy-robot.core
 {play {:scope :public, :line 65, :tested? true, :hit 5},
  stdin->commands {:scope :public, :line 87, :tested? false, :hit 0},
  go {:scope :private, :line 55, :tested? true, :hit 30},
  valid? {:scope :private, :line 48, :tested? true, :hit 24},
  file->commands {:scope :public, :line 76, :tested? false, :hit 0},
  stdin->line-seq {:scope :private, :line 82, :tested? false, :hit 0}},
 toy-robot.main
 {-main {:scope :public, :line 6, :tested? false, :hit 0}},
 toy-robot.parser
 {line->command {:scope :public, :line 8, :tested? true, :hit 9},
  is-digit? {:scope :private, :line 4, :tested? true, :hit 2},
  lines->commands {:scope :public, :line 30, :tested? false, :hit 0}}}
```

## License

Copyright © 2019 Alfred Xiao

