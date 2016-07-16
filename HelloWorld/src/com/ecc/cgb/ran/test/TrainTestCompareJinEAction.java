package com.ecc.cgb.ran.test;

import java.math.BigDecimal;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.liana.base.LianaAction;

public class TrainTestCompareJinEAction extends LianaAction{
	private double jine1;
	
	private double jine2;
	
	private String result;

	@Override
	public String execute(Context arg0) throws EMPException {
		
		BigDecimal bigDecimal1 = new BigDecimal(jine1);
		BigDecimal bigDecimal2 = new BigDecimal(jine2);
		if (bigDecimal1.compareTo(bigDecimal2)>0) {
			arg0.addDataField("result", "1");
		}else if (bigDecimal1.compareTo(bigDecimal2)==0) {
			arg0.addDataField("result", "0");
		}
		else if (bigDecimal1.compareTo(bigDecimal2)<0) {
			arg0.addDataField("result", "-1");
		}
		return "0";
	}


	public double getJine1() {
		return jine1;
	}


	public void setJine1(double jine1) {
		this.jine1 = jine1;
	}


	public double getJine2() {
		return jine2;
	}


	public void setJine2(double jine2) {
		this.jine2 = jine2;
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}
	
	
	
	
}
