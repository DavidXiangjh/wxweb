package com.ecc.cgb.ran.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.liana.base.LianaAction;

public class TrainTestDateAction extends LianaAction{
	private int dateLength;
	
	private String dateStr;
	
	private String datePattern;

	@Override
	public String execute(Context arg0) throws EMPException {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		arg0.setDataValue(dateStr, sdf.format(new Date()));
		return "0";
	}

	public int getDateLength() {
		return dateLength;
	}

	public void setDateLength(int dateLength) {
		this.dateLength = dateLength;
	}

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
	
}
