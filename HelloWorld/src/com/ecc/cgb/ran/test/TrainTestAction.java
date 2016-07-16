package com.ecc.cgb.ran.test;

import com.ecc.emp.core.Context;
import com.ecc.emp.core.EMPException;
import com.ecc.liana.base.LianaAction;

public class TrainTestAction extends LianaAction{
	private String trainCode;
	
	private String trainName;
	
	private String channelType;

	@Override
	public String execute(Context arg0) throws EMPException {
		// TODO Auto-generated method stub
		return "0";
	}

	public String getTrainCode() {
		return trainCode;
	}

	public void setTrainCode(String trainCode) {
		this.trainCode = trainCode;
	}

	public String getTrainName() {
		return trainName;
	}

	public void setTrainName(String trainName) {
		this.trainName = trainName;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	
	
}
