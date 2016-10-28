package twg2.parser.primitive.test;

import org.junit.Test;

import twg2.parser.primitive.ParseInt;
import twg2.parser.textParser.TextCharsParser;
import twg2.parser.textParser.TextIteratorParser;
import checks.CheckTask;

/**
 * @author TeamworkGuy2
 * @since 2015-6-21
 */
public class ParseIntTest {

	@Test
	public void parseIntTest() {
		String[] strs = { "		 1234", "12345", "+123456", "	-1234567", "" + Integer.MIN_VALUE, "" + Integer.MAX_VALUE, "+0", "-0", "\t6", " 08" };
		Integer[] expect = { 1234, 12345, +123456, -1234567, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0, 6, 8 };

		CheckTask.assertTests(strs, expect, (String s, Integer idx) -> {
			try {
				return ParseInt.readInt(TextCharsParser.of(s), true, 10);
			} catch(Exception e) {
				throw new RuntimeException((idx + 1) + ". " + s, e);
			}
		});

		CheckTask.assertTests(strs, expect, (String s, Integer idx) -> {
			try {
				return ParseInt.readInt(TextIteratorParser.of(s), true, 10);
			} catch(Exception e) {
				throw new RuntimeException((idx + 1) + ". " + s, e);
			}
		});
	}

}
