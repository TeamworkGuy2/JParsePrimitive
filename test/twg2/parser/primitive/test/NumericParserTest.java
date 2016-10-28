package twg2.parser.primitive.test;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import twg2.arrays.ArrayUtil;
import twg2.parser.primitive.NumericParser;
import twg2.parser.primitive.NumericParser.NumericType;
import twg2.parser.textParser.TextCharsParser;
import twg2.parser.textParser.TextIteratorParser;
import twg2.parser.textParser.TextParser;
import twg2.text.stringUtils.StringTrim;

/**
 * @author TeamworkGuy2
 * @since 2016-2-10
 */
public class NumericParserTest {
	// TODO note: 'F' suffixes are not allowed to simplify the parser checkers job

	@Test
	public void parseFloats() {
		String[] inputs1 = { "1e1f", "2.f", ".3f", "0f", "3.14f", "33.3333F", "55.01", "6.022137e+23f" };
		Float[] expects1 = {  1e1f,   2.f,   .3f,   0f,   3.14f,   33.3333f,   55.01f,  6.022137e+23f };

		testParse(inputs1, expects1, (s, i) -> Float.parseFloat(s), 10, NumericType.DECIMAL_FLOAT, false);

		String[] inputs2 = { "-1e1f", "-2.f", "-.3f", "-0f", "22.22F", "-3.14f", "-6.022137e+23f" };
		Float[] expects2 = {  -1e1f,   -2.f,   -.3f,   -0f,   22.22f,   -3.14f,   -6.022137e+23f };

		testParse(inputs2, expects2, (s, i) -> Float.parseFloat(s), 10, NumericType.DECIMAL_FLOAT, true);
	}


	@Test
	public void parseDoubles() {
		String[] inputs = { "1e1", "2.", ".3", "0.0", "3.14", "1e-9d", "1e137" };
		Double[] expects = { 1e1,   2.,   .3,   0.0,   3.14,   1e-9d,   1e137 };

		testParse(inputs, expects, (s, i) -> Double.parseDouble(s), 10, NumericType.DECIMAL_FLOAT, false);

		String[] inputs2 = { "-1e1", "-2.", "-.3", "-0.0", "-3.14", "-1e-9d", "-1e137" };
		Double[] expects2 = { -1e1,   -2.,   -.3,   -0.0,   -3.14,   -1e-9d,   -1e137 };

		testParse(inputs2, expects2, (s, i) -> Double.parseDouble(s), 10, NumericType.DECIMAL_FLOAT, true);
	}


	@Test
	public void parseInts() {
		String[] inputs1 = {  "0", "2", "0372", "1996", "1234567" };
		Integer[] expects1 = { 0,   2,    372,   1996,   1234567 };

		testParse(inputs1, expects1, (s, i) -> Integer.parseInt(s), 10, NumericType.DECIMAL_INT, false);

		String[] inputs2 = {  "-0", "-2", "-0372", "-1996", "-1234567", "123" };
		Integer[] expects2 = { -0,   -2,    -372,   -1996,   -1234567,   123 };

		testParse(inputs2, expects2, (s, i) -> Integer.parseInt(s), 10, NumericType.DECIMAL_INT, true);

		String[] inputs3 = {  "0xDada_Cafe", "0x00_FF__00_FF" };
		Integer[] expects3 = { 0xDada_Cafe,   0x00_FF__00_FF };

		testParse(inputs3, expects3, (s, i) -> (int)Long.parseLong(s, 16), 16, NumericType.HEX_INT, true);
	}


	@Test
	public void parseLongs() {
		String[] inputs1 = { "0l", "0777L", "3L", "829315l", "5_347_483_648L" };
		Long[] expects1 = {   0l,    777L,   3L,   829315L,   5_347_483_648L };

		testParse(inputs1, expects1, (s, i) -> Long.parseLong(s), 10, NumericType.DECIMAL_INT, false);

		String[] inputs2 = { "-0l", "-0777L", "+43l", "88L", "-2_147_483_648L" };
		Long[] expects2 = {   -0l,    -777L,   43L,    88L,   -2_147_483_648L };

		testParse(inputs2, expects2, (s, i) -> Long.parseLong(s), 10, NumericType.DECIMAL_INT, true);

		String[] inputs3 = { "0x100000000L", "0X0B30", "0xC0B0L" };
		Long[] expects3 = {   0x100000000L,   0x0B30L,  0xC0B0L };

		testParse(inputs3, expects3, (s, i) -> Long.parseLong(s, 16), 16, NumericType.HEX_INT, false);
	}


	@Test
	public void parseHexFloats() {
		String[] inputs1 = { "0x03B1p1F", "0x8p113f", "0x123Ap10F" };
		Float[] expects1 = {  0x03B1p1f,   0x8p113f,   0x123Ap10f };

		testParse(inputs1, expects1, (s, i) -> expects1[i], 16, NumericType.HEX_FLOAT, true); // compare expected number to itself, still checks to make sure NumericParser successfully parses the string

		String[] inputs2 = { "-0X1p1F", "-0x77p7f", "-0x2_147p83f" };
		Float[] expects2 = {  -0x1p1f,   -0x77p7f,   -0x2_147p83f };

		testParse(inputs2, expects2, (s, i) -> expects2[i], 16, NumericType.HEX_FLOAT, true); // compare expected number to itself, still checks to make sure NumericParser successfully parses the string
	}


	private static <T extends Number> void testParse(String[] srcs, T[] expects, BiFunction<String, Integer, T> parse, int radix, NumericType type, boolean parseSign) {
		testParse(srcs, expects, parse, radix, type, parseSign, TextIteratorParser::of);
		testParse(srcs, expects, parse, radix, type, parseSign, TextCharsParser::of);
	}


	private static <T extends Number> void testParse(String[] srcs, T[] expects, BiFunction<String, Integer, T> parse, int radix, NumericType type, boolean parseSign, Function<String, TextParser> createParser) {
		Assert.assertEquals(srcs.length, expects.length);

		NumericParser n = new NumericParser(parseSign);
		for(int i = 0, size = srcs.length; i < size; i++) {
			String src = srcs[i];
			TextParser buf = createParser.apply(src);

			while(buf.hasNext()) {
				char ch = buf.nextChar();
				if(!n.acceptNext(ch, buf)) {
					break;
				}
			}

			Assert.assertFalse("numeric parse failed: " + src, n.isFailed());
			Assert.assertEquals("source: " + src, type, n.getNumericType());
			String parsedStr = n.getParserDestination().toString();
			String expectStr = trimSuffix(parsedStr, radix);
			Assert.assertEquals(parsedStr, src);

			try {
				T parsedRes = parse.apply(expectStr, i);
				Assert.assertEquals("source: " + src + " (expect string: " + expectStr + ")", expects[i], parsedRes);
				n = n.recycle();
			} catch(Exception e) {
				throw new RuntimeException("source: " + (i + 1) + ". " + src, e);
			}
		}
	}


	private static String trimSuffix(String str, int radix) {
		int[] allowedSuffixes = new String(NumericParser.allSuffix).chars().filter((a) -> a != 'F').toArray();
		str = str.trim().replace("_", "").toUpperCase();

		String sign = "";
		if(str.length() > 0 && NumericParser.isSign.test(str.charAt(0))) {
			sign = str.charAt(0) == '-' ? "-" : "";
			str = str.substring(1);
		}

		switch(radix) {
		case 16:
			str = StringTrim.trimLeading(str, "0X");
			break;
		case 8:
			str = StringTrim.trimLeading(str, "0C");
			break;
		case 2:
			str = StringTrim.trimLeading(str, "0B");
			break;
		case 10:
			// no decimal prefix
			break;
		default:
			throw new IllegalArgumentException("unknown numeric radix " + radix);
		}

		StringBuilder sb = new StringBuilder(sign + str);
		// trim suffixes other than 'F'
		while(sb.length() > 0 && ArrayUtil.indexOf(allowedSuffixes, sb.charAt(sb.length() - 1)) > -1) {
			sb.setLength(sb.length() - 1);
		}
		while(sb.length() > 1 && sb.charAt(0) == '0' && Character.isDigit(sb.charAt(1))) {
			sb.delete(0, 1);
		}
		return sb.toString();
	}

}
