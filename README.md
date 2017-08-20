JParsePrimitive
==============
version: 0.2.1

* twg2.parser.primitive:
  * NumericParser - for parsing Java Language Specification (JLS) numeric literals (binary, octal, decimal, hex, float, and hex-float) from text input streams
  * ParseNumeric - for parsing simple +/-###... integer values from a TextParser

--------
#### NumericParser
```Java
NumericParser np = new NumericParser(true/*parse -/+ sign*/);
TextParser buf = TextCharsParser.of("-6.022137e+23f"); // TextParser and TextCharsParser from jtext-parser library

while(buf.hasNext()) {
	char ch = buf.nextChar();
	if(!np.acceptNext(ch, buf)) {
		break;
	}
}

System.out.println(NumericType.DECIMAL_FLOAT == np.getNumericType()); // true
System.out.println(np.getParserDestination().toString()); // -6.022137e+23f
```

--------
#### ParseInt
```Java
int i = ParseInt.readInt(TextCharsParser.of("  +1256"), true, 10); // TextCharsParser from jtext-parser library
System.out.println(i); // 1256
```
