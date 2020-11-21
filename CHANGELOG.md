# Change Log
All notable changes to this project will be documented in this file.
This project does its best to adhere to [Semantic Versioning](http://semver.org/).


--------
### [0.3.0](N/A) - 2020-11-21
#### Changed
* Added `NumericParser.getFirstChars()` to match jtext-parser version 0.17.0
* Improve unit tests

#### Removed
* `NumericParser.getParserDestination()` and `setParserDestination()`


--------
### [0.2.1](https://github.com/TeamworkGuy2/JParsePrimitive/commit/443428c474a71501f85e87de9eb5eb4650e4f0e5) - 2017-08-20
#### Changed
* Updated dependency jfunc@0.3.0 (`Predicates.Char` -> `CharPredicate`)


--------
### [0.2.0](https://github.com/TeamworkGuy2/JParsePrimitive/commit/0c3101d2823a6cfc8b18fe2567a9fe20805a635a) - 2016-12-03
#### Changed
* Updated to latest jtext-parser version 0.11.0 (`NumericParser.getCompleteMatchedTextCoords()` -> `getMatchedTextCoords()`)


--------
### [0.1.5](https://github.com/TeamworkGuy2/JParsePrimitive/commit/55b8ae6e92de7c1748d686cc70481ff49ac96094) - 2016-10-30
#### Changed
* Updated to latest jtext-parser version 0.10.0 (`TextFragmentRef.ImplMut` -> `TextFragmentRefImplMut`)
* Added some example code to README


--------
### [0.1.4](https://github.com/TeamworkGuy2/JParsePrimitive/commit/d8137c07484afb4bc63b7713ba18a8c4334a7360) - 2016-10-27
#### Changed
* Switched/updated jarray-util -> jarrays dependency
* Updated unit tests


--------
### [0.1.3](https://github.com/TeamworkGuy2/JParsePrimitive/commit/2b72e70ba474b922d6812f373eada5afa5b1a6ae) - 2016-09-11
#### Changed
* Updated jtext-parser dependency to latest 0.9.0 version (TextParserImpl -> TextIteratorParser)


--------
### [0.1.2](https://github.com/TeamworkGuy2/JParsePrimitive/commit/f4fafc577b7c9f3d117bb50ae1ea90f06d55c543) - 2016-09-10
#### Changed
* NumericParser performance adjustments to see if we see downstream improvements in jparser-code


--------
### [0.1.1](https://github.com/TeamworkGuy2/JParsePrimitive/commit/4efa8373c385dc9eee922f1bf3a0007e3caeb146) - 2016-09-02
#### Changed
* Renamed project from JParserPrimitive -> JParsePrimitive


--------
### [0.1.0](https://github.com/TeamworkGuy2/JParsePrimitive/commit/c551ac5c5ecef328f228ac945b53b831fd55b47f) - 2016-09-01
#### Added
Includes primitive (boolean, int, float) parsing code from [JParserDataTypeLike](https://github.com/TeamworkGuy2/JParserDataTypeLike) which came from the JLikelyParser library:
* `twg2.parser.primitive.NumericParser` - for parsing Java numeric literals (binary, octal, decimal, hex, float, and hex-float) from text input streams
