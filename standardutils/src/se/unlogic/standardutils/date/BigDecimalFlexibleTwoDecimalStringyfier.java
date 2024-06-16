package se.unlogic.standardutils.date;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import se.unlogic.standardutils.string.Stringyfier;

public class BigDecimalFlexibleTwoDecimalStringyfier implements Stringyfier<BigDecimal> {

	@Override
	public String format(BigDecimal bean) {

		return new DecimalFormat("##.###").format(bean);
	}

}
