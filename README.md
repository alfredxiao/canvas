# coverlet

A Clojure leiningen plugin that reports on test coverage.

## Presentation
http://tiny.cc/coverlet

## Usage

### Latest Version
`[coverlet "0.1.8"]`

### Add a plugin reference in your project.clj:
```
:plugins [[coverlet "0.1.8"]]
```

### Run the plugin (in another project):
`lein coverlet`

### Sample output:

``` 

═════════ Coverlet Report ═════════
⛈ toy-robot.core      ½    
  + play              ✔   5
  + stdin->commands   ⬚    
  - go                ✔  30
  - valid?            ✔  24
  + file->commands    ⬚    
  - stdin->line-seq   ⬚    
❌ toy-robot.main          
  + -main             ⬚    
✅ toy-robot.parser        
  + line->command     ✔  11
  - is-digit?         ✔   2
  + lines->commands   ✔   1
════════════════════════════════════

```

## License

Copyright © 2019 Alfred Xiao

