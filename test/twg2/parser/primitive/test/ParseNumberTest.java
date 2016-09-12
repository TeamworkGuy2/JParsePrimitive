package twg2.parser.primitive.test;

import org.junit.Test;

import twg2.parser.primitive.ParseInt;
import twg2.parser.textParser.TextIteratorParser;
import checks.CheckTask;

/**
 * @author TeamworkGuy2
 * @since 2015-6-21
 */
public class ParseNumberTest {

	@Test
	public void parseIntTest() {
		String[] strs = new String[] { "		 1234", "12345", "+123456", "	-1234567", "" + Integer.MIN_VALUE, "" + Integer.MAX_VALUE, "+0", "-0" };
		Integer[] expect = { 1234, 12345, +123456, -1234567, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0 };

		CheckTask.assertTests(strs, expect, (s, n) -> {
			try {
				return ParseInt.readInt(TextIteratorParser.of(s), true, 10);
			} catch(Exception e) {
				throw new RuntimeException((n + 1) + ". " + s, e);
			}
		});
	}

}
