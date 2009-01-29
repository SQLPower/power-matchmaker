/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.security.MessageDigest;

import sun.misc.BASE64Encoder;

/**
 * This class will create a new munge step to the MatchMaker in order to calculate the
 * MD5 Check Sum of a string. This is usually used for password encryption.
 */
public class MD5CheckSumMungeStep extends AbstractMungeStep {
	
	
	public MD5CheckSumMungeStep(){
		super("MD5 CheckSum", false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("md5CheckSumOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("md5CheckSum", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("MD5 CheckSum munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
		throw new UnsupportedOperationException("MD5 CheckSum substitution munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"MD5 CheckSum munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean doCall() throws Exception {		
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		if (data != null) {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data.getBytes("UTF-8"));
			out.setData((new BASE64Encoder()).encode(digest.digest()));
		} else {
			out.setData(null);
		}
		return true;
	}

}
