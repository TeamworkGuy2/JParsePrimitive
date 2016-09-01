package twg2.parser.primitive.test;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import twg2.arrays.ArrayUtil;
import twg2.parser.primitive.NumericParser;
import twg2.parser.primitive.NumericParser.NumericType;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.text.stringUtils.StringTrim;

/**
 * @author TeamworkGuy2
 * @since 2016-2-10
 */
public class NumericParserTest {
	// TODO note: 'F' suffixes are not allowed to simplify the parser checkers job

	@Test
	public void parseFloats() {
		String[] inputs1 = { "1e1f", "2.f", ".3f", "0f", "3.14f", "6.022137e+23f" };
		Float[] expects1 = {  1e1f,   2.f,   .3f,   0f,   3.14f,   6.022137e+23f };

		testParse(inputs1, expects1, Float::parseFloat, 10, NumericType.DECIMAL_FLOAT, false);

		String[] inputs2 = { "-1e1f", "-2.f", "-.3f", "-0f", "-3.14f", "-6.022137e+23f" };
		Float[] expects2 = {  -1e1f,   -2.f,   -.3f,   -0f,   -3.14f,   -6.022137e+23f };

		testParse(inputs2, expects2, Float::parseFloat, 10, NumericType.DECIMAL_FLOAT, true);
	}


	@Test
	public void parseDoubles() {
		String[] inputs = { "1e1", "2.", ".3", "0.0", "3.14", "1e-9d", "1e137" };
		Double[] expects = { 1e1,   2.,   .3,   0.0,   3.14,   1e-9d,   1e137 };

		testParse(inputs, expects, Double::parseDouble, 10, NumericType.DECIMAL_FLOAT, false);

		String[] inputs2 = { "-1e1", "-2.", "-.3", "-0.0", "-3.14", "-1e-9d", "-1e137" };
		Double[] expects2 = { -1e1,   -2.,   -.3,   -0.0,   -3.14,   -1e-9d,   -1e137 };

		testParse(inputs2, expects2, Double::parseDouble, 10, NumericType.DECIMAL_FLOAT, true);
	}


	@Test
	public void parseInts() {
		String[] inputs1 = {  "0", "2", "0372", "1996", "1234567" };
		Integer[] expects1 = { 0,   2,    372,   1996,   1234567 };

		testParse(inputs1, expects1, Integer::parseInt, 10, NumericType.DECIMAL_INT, false);

		String[] inputs2 = {  "-0", "-2", "-0372", "-1996", "-1234567" };
		Integer[] expects2 = { -0,   -2,    -372,   -1996,   -1234567 };

		testParse(inputs2, expects2, Integer::parseInt, 10, NumericType.DECIMAL_INT, true);

		String[] inputs3 = {  "0xDada_Cafe", "0x00_FF__00_FF" };
		Integer[] expects3 = { 0xDada_Cafe,   0x00_FF__00_FF };

		testParse(inputs3, expects3, (s) -> (int)Long.parseLong(s, 16), 16, NumericType.HEX_INT, true);
	}


	@Test
	public void parseLongs() {
		String[] inputs1 = { "0l", "0777L", "2_147_483_648L" };
		Long[] expects1 = {   0l,    777L,   2_147_483_648L };

		testParse(inputs1, expects1, Long::parseLong, 10, NumericType.DECIMAL_INT, false);

		String[] inputs2 = { "-0l", "-0777L", "-2_147_483_648L" };
		Long[] expects2 = {   -0l,    -777L,   -2_147_483_648L };

		testParse(inputs2, expects2, Long::parseLong, 10, NumericType.DECIMAL_INT, true);

		String[] inputs3 = { "0x100000000L", "0xC0B0L" };
		Long[] expects3 = {   0x100000000L,   0xC0B0L };

		testParse(inputs3, expects3, (s) -> Long.parseLong(s, 16), 16, NumericType.HEX_INT, false);
	}


	private static <T extends Number> void testParse(String[] srcs, T[] expects, Function<String, T> parse, int radix, NumericType type, boolean parseSign) {
		Assert.assertEquals(srcs.length, expects.length);

		BiFunction<String, String, T> parseWrapper = (in, inSrc) -> {
			try {
				return parse.apply(in);
			} catch(Exception e) {
				throw new RuntimeException("source: " + inSrc, e);
			}
		};

		NumericParser n = new NumericParser(parseSign);
		for(int i = 0, size = srcs.length; i < size; i++) {
			TextParser buf = TextParserImpl.of(srcs[i]);

			while(buf.hasNext()) {
				char ch = buf.nextChar();
				if(!n.acceptNext(ch, buf)) {
					break;
				}
			}

			Assert.assertFalse("numeric parse failed: " + srcs[i], n.isFailed());
			Assert.assertEquals("source: " + srcs[i], type, n.getNumericType());
			String expectStr = trimSuffix(n.getParserDestination().toString(), radix);
			Assert.assertEquals("source: " + srcs[i] + " (expect string: " + expectStr + ")", expects[i], parseWrapper.apply(expectStr, (i + 1) + ". " + srcs[i]));
			n = n.recycle();
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
