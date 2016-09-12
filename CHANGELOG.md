# Change Log
All notable changes to this project will be documented in this file.
This project does its best to adhere to [Semantic Versioning](http://semver.org/).


--------
###[0.1.3](N/A) - 2016-09-11
#### Changed
* Updated jtext-parser dependency to latest 0.9.0 version (TextParserImpl -> TextIteratorParser)


--------
###[0.1.2](https://github.com/TeamworkGuy2/JParsePrimitive/commit/f4fafc577b7c9f3d117bb50ae1ea90f06d55c543) - 2016-09-10
#### Changed
* NumericParser performance adjustments to see if we see downstream improvements in jparser-code


--------
###[0.1.1](https://github.com/TeamworkGuy2/JParsePrimitive/commit/4efa8373c385dc9eee922f1bf3a0007e3caeb146) - 2016-09-02
#### Changed
* Renamed project from JParserPrimitive -> JParsePrimitive


--------
###[0.1.0](https://github.com/TeamworkGuy2/JParsePrimitive/commit/c551ac5c5ecef328f228ac945b53b831fd55b47f) - 2016-09-01
#### Added
Includes primitive (boolean, int, float) parsing code from [JParserDataTypeLike](https://github.com/TeamworkGuy2/JParserDataTypeLike) which came from [JLikelyParser] (https://github.com/TeamworkGuy2/JLikelyParser) library:
* twg2.parser.primitive.NumericParser - for parsing Java numeric literals (binary, octal, decimal, hex, float, and hex-float) from text input streams
