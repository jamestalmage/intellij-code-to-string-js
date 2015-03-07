# intellij-code-to-string-js
Intellij Plugin that will convert highlighted javascript code to a string.

Check out [this video](http://youtu.be/9NqtoREdAnI) demonstrating it's use.

Basically, it provides a really fast way for you to highlight javascript code and turn in to a javascript string.

```javascript
var input = [
  'var c, d;',
  '// @ngProvide',
  'var a = "a", b = "b";'
].join('\n');
```      
This is targeted at all you parser/transpiler/transform developers out there as a thank you for all your hard work.
Special thanks to @benjamn of [recast](https://github.com/benjamn/ast-types) for his work on that library.

It handles indentations in an intelligent way.
Watch the video closely - notice how the strings are indented to match the current code block,
while the code within the strings still maintains your "pretty printed" indentations.

Just grab the `.jar` file from the repo and `install from disk` in the plugin settings. Works in webstorm.

There are two tagged versions of this plugin, one that wraps your code in single quotes, and one that wraps it in double quotes.
(sorry, making it a configurable option would be quite a lot of work - if you decide to implement such a feature yourself, 
I ask that you contribute it back via a pull request).

