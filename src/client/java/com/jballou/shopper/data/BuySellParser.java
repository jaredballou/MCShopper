package com.jballou.shopper.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;

/**
 * Utility class for parsing the 3rd line of a shop sign.
 * Using this class helps to reduce garbage & allocations in
 * the scan iterator
 */
public final class BuySellParser
{
	private static final String REGEX_STR = "(B ([0-9.]+))?[ ]*(:)?[ ]*(([0-9.]+) S)?";
	private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX_STR);

	public int buyPrice = -1;
	public int sellPrice = -1;
	public boolean isFrontSide = false;

	public BuySellParser() {}

	public boolean parseSign(SignBlockEntity sign)
	{
		return parse(sign.getFrontText().getMessage(2, false), true) || parse(sign.getBackText().getMessage(2, false), false);
	}

	private boolean parse(Text text, boolean side)
	{
		Matcher matcher = REGEX_PATTERN.matcher(text.getString());

		if(!matcher.matches() || (matcher.group(2) == null && matcher.group(5) == null))
		{
			return false;
		}

		String buy = matcher.group(2);
		String sell = matcher.group(5);
		isFrontSide = side;

		buyPrice = buy != null ? Integer.parseInt(buy) : -1;
		sellPrice = sell != null ? Integer.parseInt(sell) : -1;

		return true;
	}
}
