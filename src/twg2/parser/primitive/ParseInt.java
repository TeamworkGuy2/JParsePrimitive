package twg2.parser.primitive;

import twg2.parser.textParser.TextParser;
import twg2.parser.textParserUtils.ReadIsMatching;
import twg2.parser.textParserUtils.ReadWhitespace;
import twg2.text.stringUtils.StringCheck;

public class ParseInt {


	/** Read an integer from a {@link TextParser}.  Differs from {@link Integer#parseInt(String, int)} by
	 * allowing whitespace before the integer and any text after the integer.
	 * @param in the {@code LineBuffer} to read text from
	 * @param readWhitespaceBefore true to read any whitespace at the beginning of the string
	 * @param radix the radix of the integer to parse
	 * @return the parsed integer
	 */
	public static final int readInt(TextParser in, boolean readWhitespaceBefore, int radix) {
		if(radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix + " must be greater than or equal to " + Character.MIN_RADIX);
		}
		if(radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix + " must be less than or equal to " + Character.MAX_RADIX);
		}

		if(readWhitespaceBefore) {
			ReadWhitespace.readWhitespaceCustom(in, StringCheck.SIMPLE_WHITESPACE_NOT_NEWLINE);
		}

		// TODO throw exceptions with string portion that failed to parse
		int res = 0;
		boolean neg = false;
		int limit = -Integer.MAX_VALUE;
		if(in.hasNext()) {
			char firstChar = in.nextChar();
			// parse sign ('-' or '+')
			if(firstChar < '0') {
				if(firstChar == '-') {
					neg = true;
					limit = Integer.MIN_VALUE;
				}
				else if(firstChar != '+') {
					throw new NumberFormatException("invalid number starting with '" +
							firstChar + "' at " + in.getColumnNumber());
				}
				if(!ReadIsMatching.isNextBetween(in, '0', '9', 1)) {
					throw new NumberFormatException("cannot parse integer, no digits found");
				}
			}
			// else, first digit of integer (could be a decimal point '.' for decimals)
			else if(Character.digit(firstChar, radix) > -1) {
				in.unread(1);
			}

			int multmin = limit / radix;
			// for each additional digit, multiply the result's magnitude by the radix and add the next digit to the partial result
			// NOTE: result number is always parsed into a negative result number until the end when the sign is applied
			while(in.hasNext()) {
				int digit = Character.digit(in.nextChar(), radix);
				if(digit < 0) {
					break;
				}
				if(res < multmin) {
					throw new NumberFormatException();
				}
				res *= radix;
				if(res < limit + digit) {
					throw new NumberFormatException();
				}
				res -= digit;
			}
		}
		else {
			throw new NumberFormatException("cannot parse integer, no digits found");
		}
		in.unread(1);
		return neg ? res : -res;
	}

}
