package twg2.parser.primitive;

import twg2.arrays.ArrayUtil;
import twg2.functions.BiPredicates;
import twg2.functions.Predicates;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;

/**
 * @author TeamworkGuy2
 * @since 2014-8-30
 */
public class NumericParser implements CharParserMatchable {

	/** 
	 * @author TeamworkGuy2
	 * @since 2015-12-12
	 */
	public static enum Stage {
		INIT,
		INIT_AFTER_SIGN,
		HEX_OCT_BIN_OR_INT_FLOAT,
		HEX_DIGIT,
		HEX_DIGIT_OR_,
		HEX_FLOAT_DIGIT,
		HEX_FLOAT_DIGIT_OPTIONAL,
		HEX_FLOAT_DIGIT_OR_,
		DECIMAL_DIGIT,
		DECIMAL_DIGIT_OR_,
		DECIMAL_FLOAT_DIGIT,
		DECIMAL_FLOAT_DIGIT_OPTIONAL,
		DECIMAL_FLOAT_DIGIT_OR_,
		OCTAL_DIGIT,
		OCTAL_DIGIT_OR_,
		BINARY_DIGIT,
		BINARY_DIGIT_OR_,
		EXPONENT_INT_INIT,
		EXPONENT_INT_DIGIT,
		EXPONENT_INT_DIGIT_OR_,
		COMPLETE,
		FAILED;
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2014-8-24
	 */
	public static enum NumericType {
		HEX_INT,
		HEX_FLOAT,
		DECIMAL_INT,
		DECIMAL_FLOAT,
		OCTAL_INT,
		BINARY_INT;
	}




	public static final char[] floatSuffix = { 'f', 'F', 'd', 'D' };
	public static final char[] intSuffix = { 'l', 'L' };
	public static final char[] allSuffix = ArrayUtil.concat(floatSuffix, intSuffix);

	protected static final char[] floatExponentIndicator = { 'e', 'E' };
	protected static final char[] binaryExponentIndicator = { 'p', 'P' };

	protected static final char allowedChar = '_';
	protected static final char allowedHexChar = '_';
	protected static final char allowedDigitChar = '_' ;
	protected static final char allowedOctalChar = '_';
	protected static final char allowedBinaryChar = '_';


	public static final char floatPointChar = '.';
	public static final char prefix1Of2 = '0';
	public static final Predicates.Char isPrefix2Of2 = (c) -> (c == 'X' || c == 'x' || c == 'C' || c == 'c' || c == 'B' || c == 'b');
	public static final Predicates.Char isSign = (c) -> (c == '-' || c == '+');
	public static final Predicates.Char isFloatExponent = (c) -> (c == 'E' || c == 'e');
	public static final Predicates.Char isFloatBinaryExponent = (c) -> (c == 'P' || c == 'p');
	public static final Predicates.Char isFloatSuffix = (c) -> (c == 'F' || c == 'f' || c == 'D' || c == 'd');
	public static final Predicates.Char isIntSuffix = (c) -> (c == 'L' || c == 'l');

	public static final Predicates.Char isHex = (c) -> (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
	public static final Predicates.Char isHexOrOptionalChar = (c) -> (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') || c == allowedHexChar;
	public static final Predicates.Char isHexPrefix = (c) -> (c == 'X' || c == 'x');

	public static final Predicates.Char isDigit = (c) -> (c >= '0' && c <= '9');
	public static final Predicates.Char isDigitOrOptionalChar = (c) -> (c >= '0' && c <= '9') || c == allowedDigitChar;

	public static final Predicates.Char isOctal = ((c) -> (c >= '0' && c <= '7'));
	public static final Predicates.Char isOctalOrOptionalChar = ((c) -> (c >= '0' && c <= '7') || c == allowedOctalChar);
	public static final Predicates.Char isOctalPrefix = (c) -> (c == 'C' || c == 'c');

	public static final Predicates.Char isBinary = (c) -> (c == '0' || c == '1');
	public static final Predicates.Char isBinaryOrOptionalChar = (c) -> (c == '0' || c == '1') || c == allowedBinaryChar;
	public static final Predicates.Char isBinaryPrefix = (c) -> (c == 'B' || c == 'b');

	protected static final char[] suffixes = { 'X', 'x', 'C', 'c', 'B', 'b' };
	protected static final char firstDigit = '0';
	protected static final char lastDigit = '9';
	protected static final char firstHexUp = 'A';
	protected static final char lastHexUp = 'F';
	protected static final char firstHexLo = 'a';
	protected static final char lastHexLo = 'f';
	protected static final char firstOctal = '0';
	protected static final char lastOctal = '7';
	protected static final char firstBinary = '0';
	protected static final char lastBinary = '1';

	protected static final char firstValidChar = '0';
	protected static final char lastValidChar = '9';

	protected boolean parseSign;
	/** optimistic flag indicating whether enough characters have been parsed to form a valid numeric value */
	protected boolean isComplete = false;
	/** count accepted characters */
	protected int matchCount = 0;
	protected char lastChar = 0;
	protected boolean lastRes = false;
	protected boolean lastLastRes = false;
	protected boolean noRemainingInput = false;
	/** the current parsing stage (this advances as {@link #readNumeral} is called with additional characters) */
	protected Stage stage = Stage.INIT;
	/** The possible type of numeric value being parsed, given the input parsed since the last time stage was {@link Stage.INIT} */
	protected NumericType type;

	char sign;
	StringBuilder dstBuf = new StringBuilder();
	TextFragmentRef.ImplMut coords = new TextFragmentRef.ImplMut();
	BiPredicates.CharObject<TextParser> firstCharMatcher = (char ch, TextParser buf) -> {
		return ch >= firstValidChar && ch <= lastValidChar;
	};
	String name;


	public NumericParser() {
		this("numeric literal parser");
	}


	public NumericParser(boolean parseSign) {
		this("numeric literal parser", parseSign);
	}


	public NumericParser(String name) {
		this(name, false);
	}


	public NumericParser(String name, boolean parseSign) {
		this.name = name;
		this.parseSign = parseSign;
	}


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(char ch, TextParser buf) {
		// look ahead to determine exactly when the number parser ends
		if(matchCount > 0 && !buf.hasNext()) {
			noRemainingInput = true;
			return lastRes;
		}
		if(matchCount == 0) {
			boolean res = this.readNumeral(ch, buf);
			lastLastRes = lastRes;
			lastRes = res;
			if(res) {
				matchCount++;
				lastChar = ch;
				dstBuf.append(ch);
				coords.setStart(buf);
				coords.setEnd(buf);
			}
			else {
				return false;
			}

			if(!buf.hasNext()) {
				noRemainingInput = true;
				return res;
			}
		}
		char nextCh = buf.nextChar();
		boolean res = this.readNumeral(nextCh, buf);
		lastLastRes = lastRes;
		lastRes = res;

		if(res) {
			matchCount++;
			lastChar = nextCh;
			dstBuf.append(nextCh);
			coords.setEnd(buf);
		}
		buf.unread(1);
		return res || isComplete();
	}


	@Override
	public boolean isComplete() {
		return lastLastRes && (!lastRes || (noRemainingInput && isComplete));
	}


	@Override
	public boolean isFailed() {
		return !isComplete;
	}


	@Override
	public BiPredicates.CharObject<TextParser> getFirstCharMatcher() {
		return firstCharMatcher;
	}


	public NumericType getNumericType() {
		return this.type;
	}


	@Override
	public TextFragmentRef getCompleteMatchedTextCoords() {
		return coords;
	}


	@Override
	public StringBuilder getParserDestination() {
		return dstBuf;
	}


	@Override
	public void setParserDestination(StringBuilder parserDestination) {
		this.dstBuf = parserDestination;
	}


	@Override
	public NumericParser copy() {
		NumericParser copy = new NumericParser(name);
		return copy;
	}


	@Override
	public boolean canRecycle() {
		return true;
	}


	@Override
	public NumericParser recycle() {
		this.reset();
		return this;
	}


	// package-private
	void reset() {
		isComplete = false;
		matchCount = 0;
		lastChar = 0;
		sign = 0;
		dstBuf.setLength(0);
		coords = new TextFragmentRef.ImplMut();
		stage = Stage.INIT;
		type = null;
		lastRes = false;
		lastLastRes = false;
		noRemainingInput = false;
	}


	/* intNumeral    : digits underscoresOrDigits digits
	 * hexNumeral    : hexDigits underscoresOrHexDigits hexDigits
	 * signedInteger : [+-] digits underscoresOrDigits digits
	 * int decimal   : intNumeral [intSuffix]
	 * int hex       : 0[Xx] hexNumeral [intSuffix]
	 * int octal     : 0[Cc] octalDigits underscoresOrOctalDigits octalDigits [intSuffix]
	 * int binary    : 0[Bb] binaryDigits underscoresOrBinaryDigits binaryDigits [intSuffix]
	 * float decimal : intNumeral [. [intNumeral]] [floatExponentId signedInteger] [floatSuffix]
	 *               : [intNumeral] . intNumeral [floatExponentId signedInteger] [floatSuffix]
	 * float hex     : 0[Xx] [hexNumeral] [. [hexNumeral]] floatBinaryExponentId signedInteger [floatSuffix]
	 */

	/* decNumeral        : DECIMAL_DIGIT DECIMAL_DIGIT_OR_ **DECIMAL_DIGIT
	 * decFloatNumeral   : DECIMAL_FLOAT_DIGIT DECIMAL_FLOAT_DIGIT_OR_ **DECIMAL_FLOAT_DIGIT
	 * decFloatOptNumeral: DECIMAL_FLOAT_DIGIT_OPTIONAL DECIMAL_FLOAT_DIGIT_OR_ **DECIMAL_FLOAT_DIGIT
	 * hexNumeral        : HEX_DIGIT HEX_DIGIT_OR_ **HEX_DIGIT
	 * hexFloatOptNumeral: HEX_FLOAT_DIGIT_OPTIONAL HEX_FLOAT_DIGIT_OR_ **HEX_FLOAT_DIGIT
	 * signedInteger     : [+-] decNumeral
	 * 
	 * int decimal   : DECIMAL_DIGIT DECIMAL_DIGIT_OR_ **DECIMAL_DIGIT [intSuffix]
	 * int hex       : 0[Xx] hexNumeral [intSuffix]
	 * int octal     : 0[Cc] OCTAL_DIGIT OCTAL_DIGIT_OR_ **OCTAL_DIGIT [intSuffix]
	 * int binary    : 0[Bb] BINARY_DIGIT BINARY_DIGIT_OR_ **BINARY_DIGIT [intSuffix]
	 * float decimal : decNumeral [. [decFloatOptNumeral]] [floatExponentId EXPONENT_FLOAT] [floatSuffix]
	 *               : [decNumeral] . decFloatNumeral [floatExponentId EXPONENT_FLOAT] [floatSuffix]
	 * float hex     : 0[Xx] [hexNumeral] [. [hexFloatOptNumeral]] floatBinaryExponentId EXPONENT_FLOAT [floatSuffix]
	 */
	public boolean readNumeral(char ch, TextParser buf) {
		boolean res = false;
		switch(this.stage) {
		case INIT:
			if(this.parseSign && isSign.test(ch)) {
				this.sign = ch;
				this.stage = Stage.INIT_AFTER_SIGN;
				this.isComplete = false;
				return true;
			}
			//$FALL-THROUGH$
		case INIT_AFTER_SIGN:
			if(ch == '.') {
				this.stage = Stage.DECIMAL_FLOAT_DIGIT;
				this.type = NumericType.DECIMAL_FLOAT;
				this.isComplete = false;
				return true;
			}
			else if(isDigit.test(ch)) {
				this.stage = ch == '0' ? Stage.HEX_OCT_BIN_OR_INT_FLOAT : Stage.DECIMAL_DIGIT_OR_;
				this.type = NumericType.DECIMAL_INT;
				this.isComplete = true;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case DECIMAL_DIGIT:
			res = isDigit.test(ch);
			this.stage = res ? Stage.DECIMAL_DIGIT_OR_ : Stage.FAILED;
			this.isComplete = res;
			return res;

		case HEX_OCT_BIN_OR_INT_FLOAT:
			//$FALL-THROUGH$
		case DECIMAL_DIGIT_OR_:

			if(this.stage == Stage.HEX_OCT_BIN_OR_INT_FLOAT) {
				this.isComplete = false;
				if(matchCount > 0 && lastChar == '0') {
					if(isHexPrefix.test(ch)) {
						this.stage = Stage.HEX_DIGIT;
						this.type = NumericType.HEX_INT;
						return true;
					}
					else if(isOctalPrefix.test(ch)) {
						this.stage = Stage.OCTAL_DIGIT;
						this.type = NumericType.OCTAL_INT;
						return true;
					}
					else if(isBinaryPrefix.test(ch)) {
						this.stage = Stage.BINARY_DIGIT;
						this.type = NumericType.BINARY_INT;
						return true;
					}
					else {
						// TODO here we can handle octal literals (starting with 0)
						// we break from Java syntax and literals that start with 0 are still decimal
					}
				}
				else if(isDigit.test(ch)) {
					this.stage = this.matchCount == 0 ? Stage.DECIMAL_DIGIT : Stage.DECIMAL_DIGIT_OR_;
					this.type = NumericType.DECIMAL_INT;
					this.isComplete = true;
					return true;
				}
			}

			// DECIMAL_DIGIT_OR_
			if(ch == '.') {
				this.stage = Stage.DECIMAL_FLOAT_DIGIT_OPTIONAL;
				this.type = NumericType.DECIMAL_FLOAT;
				this.isComplete = true;
				return true;
			}
			else if(isDigitOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = isDigit.test(ch);
				return true;
			}
			else if(isIntSuffix.test(ch)) {
				this.stage = isDigit.test(lastChar) ? Stage.COMPLETE : Stage.FAILED;
				this.type = NumericType.DECIMAL_INT;
				this.isComplete = true;
				return true;
			}
			else if(isFloatSuffix.test(ch)) {
				this.stage = isDigit.test(lastChar) ? Stage.COMPLETE : Stage.FAILED;
				this.type = NumericType.DECIMAL_FLOAT;
				this.isComplete = true;
				return true;
			}
			else if(isFloatExponent.test(ch)) {
				this.stage = isDigit.test(lastChar) ? Stage.EXPONENT_INT_INIT : Stage.FAILED;
				this.type = NumericType.DECIMAL_FLOAT;
				this.isComplete = false;
				return true;
			}
			this.isComplete = isDigit.test(lastChar);
			this.stage = this.isComplete ? Stage.COMPLETE : Stage.FAILED;
			return false;

		case DECIMAL_FLOAT_DIGIT:
			res = isDigit.test(ch);
			this.stage = res ? Stage.DECIMAL_FLOAT_DIGIT_OR_ : Stage.FAILED;
			this.isComplete = true;
			return res;

		case DECIMAL_FLOAT_DIGIT_OPTIONAL:
			if(isDigit.test(ch)) {
				this.stage = Stage.DECIMAL_FLOAT_DIGIT_OR_;
				this.isComplete = true;
				return true;
			}
			else if(isFloatSuffix.test(ch)) {
				this.stage = Stage.COMPLETE;
				this.isComplete = true;
				return true;
			}
			else if(isFloatExponent.test(ch)) {
				this.stage = Stage.EXPONENT_INT_INIT;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.COMPLETE;
			this.isComplete = isDigit.test(lastChar);
			return false;

		case DECIMAL_FLOAT_DIGIT_OR_:
			if(isDigitOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = true;
				return true;
			}
			else if(isFloatSuffix.test(ch)) {
				this.stage = Stage.COMPLETE;
				this.isComplete = true;
				return true;
			}
			else if(isFloatExponent.test(ch)) {
				this.stage = Stage.EXPONENT_INT_INIT;
				this.type = NumericType.DECIMAL_FLOAT;
				this.isComplete = false;
				return true;
			}
			res = isDigit.test(lastChar);
			this.stage = res ? Stage.COMPLETE : Stage.FAILED;
			this.isComplete = res;
			return false;

		case HEX_DIGIT:
			if(ch == '.') {
				this.stage = Stage.HEX_FLOAT_DIGIT_OPTIONAL;
				this.type = NumericType.HEX_FLOAT;
				this.isComplete = false;
				return true;
			}
			else if(isHex.test(ch)) {
				this.stage = Stage.HEX_DIGIT_OR_;
				this.isComplete = true;
				return true;
			}
			else if(isFloatBinaryExponent.test(ch)) {
				this.stage = Stage.EXPONENT_INT_INIT;
				this.type = NumericType.HEX_FLOAT;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.FAILED;
			return false;

		case HEX_DIGIT_OR_:
			if(ch == '.') {
				this.stage = Stage.HEX_FLOAT_DIGIT_OPTIONAL;
				this.type = NumericType.HEX_FLOAT;
				this.isComplete = false;
				return true;
			}
			else if(isHexOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = isHex.test(ch);
				return true;
			}
			else if(isIntSuffix.test(ch)) {
				res = isHex.test(lastChar);
				this.stage = res ? Stage.COMPLETE : Stage.FAILED;
				this.isComplete = res;
				return true;
			}
			else if(isFloatBinaryExponent.test(ch)) {
				this.stage = Stage.EXPONENT_INT_INIT;
				this.type = NumericType.HEX_FLOAT;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.COMPLETE;
			this.isComplete = true;
			return false;

		case HEX_FLOAT_DIGIT:
			if(isHexOrOptionalChar.test(ch)) {
				this.stage = Stage.HEX_FLOAT_DIGIT_OR_;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case HEX_FLOAT_DIGIT_OPTIONAL:
			if(isHexOrOptionalChar.test(ch)) {
				this.stage = Stage.HEX_FLOAT_DIGIT_OR_;
				this.isComplete = false;
				return true;
			}
			else if(isFloatBinaryExponent.test(ch)) {
				this.stage = Stage.EXPONENT_INT_INIT;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.FAILED; // hex float requires binary exponent
			this.isComplete = false;
			return false;

		case HEX_FLOAT_DIGIT_OR_:
			if(isHexOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = false;
				return true;
			}
			else if(isFloatBinaryExponent.test(ch)) {
				this.stage = Stage.EXPONENT_INT_INIT;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case OCTAL_DIGIT:
			if(isOctal.test(ch)) {
				this.stage = Stage.OCTAL_DIGIT_OR_;
				this.isComplete = true;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case OCTAL_DIGIT_OR_:
			if(isOctalOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = isOctal.test(lastChar);
				return true;
			}
			else if(isIntSuffix.test(ch)) {
				res = isOctal.test(lastChar);
				this.stage = res ? Stage.COMPLETE : Stage.FAILED;
				this.isComplete = res;
				return true;
			}
			res = isOctal.test(lastChar);
			this.stage = res ? Stage.COMPLETE : Stage.FAILED;
			this.isComplete = res;
			return false;

		case BINARY_DIGIT:
			if(isBinary.test(ch)) {
				this.stage = Stage.BINARY_DIGIT_OR_;
				this.isComplete = true;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case BINARY_DIGIT_OR_:
			if(isBinaryOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = isBinary.test(lastChar);
				return true;
			}
			else if(isIntSuffix.test(ch)) {
				res = isBinary.test(lastChar);
				this.stage = res ? Stage.COMPLETE : Stage.FAILED;
				this.isComplete = res;
				return true;
			}
			res = isBinary.test(lastChar);
			this.stage = res ? Stage.COMPLETE : Stage.FAILED;
			this.isComplete = res;
			return false;

		case EXPONENT_INT_INIT:
			if(isDigit.test(ch)) {
				this.stage = Stage.EXPONENT_INT_DIGIT_OR_;
				this.isComplete = true;
				return true;
			}
			else if(isSign.test(ch)) {
				this.stage = Stage.EXPONENT_INT_DIGIT;
				this.isComplete = false;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case EXPONENT_INT_DIGIT:
			if(isDigit.test(ch)) {
				this.stage = Stage.EXPONENT_INT_DIGIT_OR_;
				this.isComplete = true;
				return true;
			}
			this.stage = Stage.FAILED;
			this.isComplete = false;
			return false;

		case EXPONENT_INT_DIGIT_OR_:
			if(isDigitOrOptionalChar.test(ch)) {
				// stage stays the same
				this.isComplete = isDigit.test(ch);
				return true;
			}
			else if(isFloatSuffix.test(ch)) {
				this.stage = Stage.COMPLETE;
				this.isComplete = isDigit.test(lastChar);
				return true;
			}
			res = isDigit.test(lastChar);
			this.stage = res ? Stage.COMPLETE : Stage.FAILED;
			this.isComplete = res;
			return false;

		case COMPLETE:
			this.isComplete = false;
			return false;

		case FAILED:
			this.isComplete = false;
			return false;

		default:
			throw new IllegalArgumentException("unknown enum value '" + this.stage + "' of type " + Stage.class);
		}
	}


	/*
	public static NumericType readNumeral_Old(TextParser in, boolean readSign, boolean originalText, char[] otherAllowedChars,
			boolean readSuffix, char[] floatSuffix, char[] intSuffix, boolean readIdentifier, Appendable dst) {

		try {
			if(readSign) {
				in.nextIf(isSign, 1, dst);
			}

			// int hex, int octal, int binary, float hex
			if(in.nextIf(prefix1Of2, 1, dst) > 0) {

				// int hex remaining: [Xx] hexNumeral [intSuffix]
				// float hex remaining: [Xx] [hexNumeral] [. [hexNumeral]] floatBinaryExponentId signedInteger [floatSuffix]
				if(in.nextIf(isHexPrefix2Of2, 1, dst) > 0) {

					if(ReadIsMatching.isNext(in, isHex, 1)) {
						readHex(in, false, false, false, dst);
					}

					// if float point, float hex remaining: [. [hexNumeral]] floatBinaryExponentId signedInteger [floatSuffix]
					boolean isFloatPoint = ReadIsMatching.isNext(in, floatPointChar, 1);

					boolean isHexFloatExponent = ReadIsMatching.isNext(in, isFloatBinaryExponent, 1);
					if(isFloatPoint || isHexFloatExponent) {
						if(isFloatPoint) {
							dst.append(floatPointChar);
							in.nextChar();
							if(ReadIsMatching.isNext(in, isHex, 1)) {
								readHex(in, false, false, false, dst);
							}
						}
						if(in.nextIf(isFloatBinaryExponent, 1, dst) > 0) {
							int exponentDigitCount = readInt(in, true, false, dst);
							if(exponentDigitCount < 1) {
								throw new IllegalStateException("hexadecimal floating point exponent must have atleast one digit");
							}
						}
						in.nextIf(isFloatSuffix, 1, dst);
						return NumericType.FLOAT_HEX;
					}
					// else int hex remaining: [intSuffix]
					else {
						in.nextIf(isIntSuffix, 1, dst);
						return NumericType.INT_HEX;
					}
				}

				// int octal remaining: [Cc] octalDigits underscoresOrOctalDigits octalDigits [intSuffix]
				else if(in.nextIf(isOctalPrefix2Of2, 1, dst) == 1) {
					readOctal(in, false, false, true, dst);
					return NumericType.INT_OCTAL;
				}

				// int binary remaining: [Bb] binaryDigits underscoresOrBinaryDigits binaryDigits [intSuffix]
				else if(in.nextIf(isBinaryPrefix2Of2, 1, dst) == 1) {
					readBinary(in, false, false, true, dst);
					return NumericType.INT_BINARY;
				}
				else {
					in.unread(1);
				}
			}

			// int decimal remaining: intNumeral [intSuffix]
			// float decimal remaining: intNumeral [. [intNumeral]] [floatExponentId signedInteger] [floatSuffix]
			if(ReadIsMatching.isNextBetween(in, firstDigit, lastDigit, 1)) {
				readInt(in, false, true, dst);

				// float decimal remaining: . [intNumeral] [floatExponentId signedInteger] [floatSuffix]
				if(ReadIsMatching.isNext(in, floatPointChar) || ReadIsMatching.isNext(in, isFloatExponent, 1) || ReadIsMatching.isNext(in, isFloatSuffix, 1)) {
					if(in.nextIf(isFloatSuffix, 1, dst) > 0) {
						return NumericType.FLOAT_DECIMAL;
					}

					in.nextIf(floatPointChar, 1, dst);

					if(ReadIsMatching.isNextBetween(in, firstDigit, lastDigit, 1)) {
						readInt(in, false, false, dst);
					}
					int count = in.nextIf(isFloatExponent, 1, dst);
					if(count > 0) {
						int exponentDigitCount = readInt(in, true, false, dst);
						if(exponentDigitCount < 1) {
							throw new IllegalStateException("floating point exponent must have atleast one digit");
						}
					}
					in.nextIf(isFloatSuffix, 1, dst);
					return NumericType.FLOAT_DECIMAL;
				}
				else {
					in.nextIf(isIntSuffix, 1, dst);
					return NumericType.INT_DECIMAL;
				}
			}

			// float decimal remaining: . [intNumeral] [floatExponentId signedInteger] [floatSuffix]
			if(in.nextIf(floatPointChar, 1, dst) == 1) {
				// float decimal remaining: [intNumeral] [floatExponentId signedInteger] [floatSuffix]
				if(ReadIsMatching.isNextBetween(in, firstDigit, lastDigit, 1)) {
					readInt(in, false, false, dst);
				}

				// float decimal remaining: [floatExponentId signedInteger] [floatSuffix]
				int count = in.nextIf(isFloatExponent, 1, dst);
				if(count > 0) {
					readInt(in, true, false, dst);
				}

				in.nextIf(isFloatSuffix, 1, dst);

				return NumericType.FLOAT_DECIMAL;
			}
		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
		throw new IllegalStateException("could not identify type of numeric value starting with '" + in.nextChar() + "'");
	}


	private static int readInt_Old(TextParser in, boolean readSign, boolean readSuffix, Appendable dst) {
		int count = 0;
		if(readSign) {
			count += in.nextIf(isSign, 1, dst);
		}
		int digitCount = in.nextBetween(firstDigit, lastDigit, 1, dst);
		count += digitCount;
		if(digitCount == 0) {
			throw new IllegalStateException("first decimal character must be a digit, found '" + ReadPeek.peekNext(in) + "' instead");
		}
		digitCount = in.nextIf(isDigitOrOptionalChar, 0, dst);
		count += digitCount;
		char ch = 0;
		if(digitCount > 0 && !isDigit.test((ch = ReadPeek.peekNext(in)))) {
			throw new IllegalStateException("first decimal character must be an digit, found '" + ch + "' instead");
		}
		if(readSuffix) {
			count += in.nextIf(isIntSuffix, 1, dst);
		}
		return count;
	}


	private static int readHex_Old(TextParser in, boolean readSign, boolean readPrefix, boolean readSuffix, Appendable dst) {
		int pos = in.getPosition();

		if(readSign) {
			in.nextIf(isSign, 1, dst);
		}
		if(readPrefix && in.nextIf(prefix1Of2, 1, dst) == 1) {
			boolean foundPrefix2 = in.nextIf(isHexPrefix2Of2, 1, dst) == 1;
			if(foundPrefix2) {
				in.skip(1);
			}
		}

		int digitCount = in.nextIf(isHex, 1, dst);

		if(digitCount == 0) {
			throw new IllegalStateException("first hexadecimal numeric character must be a hexadecimal digit, found '" + ReadPeek.peekNext(in) + "' instead");
		}

		digitCount = in.nextIf(isHexOrOptionalChar, 0, dst);

		char ch = 0;
		if(digitCount > 0 && !isHex.test((ch = ReadPeek.peekNext(in)))) {
			throw new IllegalStateException("first hexadecimal numberic character must be a hexadecimal digit, found '" + ch + "' instead");
		}
		if(readSuffix) {
			in.nextIf(isIntSuffix, 1, dst);
		}

		return in.getPosition() - pos;
	}


	private static int readOctal_Old(TextParser in, boolean readSign, boolean readPrefix, boolean readSuffix, Appendable dst) {
		int count = 0;
		if(readSign) {
			count += in.nextIf(isSign, 1, dst);
		}
		if(readPrefix && ReadIsMatching.isNext(in, prefix1Of2)) {
			in.nextChar();
			if(ReadIsMatching.isNext(in, isOctalPrefix2Of2, 1)) {
				try {
					dst.append(prefix1Of2);
					dst.append(in.nextChar());
				} catch(IOException ioe) {
					throw new UncheckedIOException(ioe);
				}
				in.nextChar();
				count += 2;
			}
			else {
				in.skip(1);
			}
		}
		int digitCount = in.nextIf(isOctal, 1, dst);
		count += digitCount;
		if(digitCount == 0) {
			throw new IllegalStateException("first octal numeric character must be an octal digit, found '" + ReadPeek.peekNext(in) + "' instead");
		}
		digitCount = in.nextIf(isOctalOrOptionalChar, 0, dst);
		count += digitCount;
		char ch = 0;
		if(digitCount > 0 && !isOctal.test((ch = ReadPeek.peekNext(in)))) {
			throw new IllegalStateException("first octal numberic character must be an octal digit, found '" + ch + "' instead");
		}
		if(readSuffix) {
			count += in.nextIf(isIntSuffix, 1, dst);
		}
		return count;
	}


	private static int readBinary_Old(TextParser in, boolean readSign, boolean readPrefix, boolean readSuffix, Appendable dst) {
		int count = 0;
		if(readSign) {
			count += in.nextIf(isSign, 1, dst);
		}
		if(readPrefix && ReadIsMatching.isNext(in, prefix1Of2)) {
			in.nextChar();
			if(ReadIsMatching.isNext(in, isBinaryPrefix2Of2, 1)) {
				try {
					dst.append(prefix1Of2);
					dst.append(in.nextChar());
				} catch(IOException ioe) {
					throw new UncheckedIOException(ioe);
				}
				in.nextChar();
				count += 2;
			}
			else {
				in.skip(1);
			}
		}
		int digitCount = in.nextIf(isBinary, 1, dst);
		count += digitCount;
		if(digitCount == 0) {
			throw new IllegalStateException("first binary numeric character must be an binary digit, found '" + ReadPeek.peekNext(in) + "' instead");
		}
		digitCount = in.nextIf(isBinaryOrOptionalChar, 0, dst);
		count += digitCount;
		char ch = 0;
		if(digitCount > 0 && !isBinary.test((ch = ReadPeek.peekNext(in)))) {
			throw new IllegalStateException("first binary numberic character must be a binary digit, found '" + ch + "' instead");
		}
		if(readSuffix) {
			count += in.nextIf(isIntSuffix, 1, dst);
		}
		return count;
	}
	*/

}
