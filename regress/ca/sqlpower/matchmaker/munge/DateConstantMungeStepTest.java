/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class DateConstantMungeStepTest extends MatchMakerTestCase<DateConstantMungeStep> {

	private static final Logger logger = Logger.getLogger(DateConstantMungeStep.class);

    DateConstantMungeStep step;

	public DateConstantMungeStepTest(String name) {
		super(name);
	}
	
    @Override
    protected void setUp() throws Exception {
        step = new DateConstantMungeStep();
        super.setUp();
        MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addTransformationMungeStep(step);
    }
    
    private void setFormat(int x){
    	step.setDateFormat(DateConstantMungeStep.FORMAT.get(x));
    }
    
    private void setRetNull(boolean b) {
    	step.setReturnNull(b);
    }
    
    private void setUseCurrent(boolean b) {
    	step.setUseCurrentTime(b);
    }
    
    private Date runWith(Date d) throws Exception {
    	step.setValueAsDate(d);
    	step.open(logger);
    	step.call();
    	Date out = (Date) step.getOut().getData();
    	step.mungeRollback();
    	step.mungeClose();
    	return out;
    }
    
    public void test20011002() throws Exception{
    	Calendar cal = Calendar.getInstance();
    	cal.set(2001, 10, 02, 0, 0, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date d = cal.getTime();
    	assertEquals(d, runWith(d));
    }
    
    public void testRetNull() throws Exception{
    	Calendar cal = Calendar.getInstance();
    	cal.set(2001, 10, 02, 0, 0, 0);
    	Date d = cal.getTime();
    	setRetNull(true);
    	assertNull(runWith(d));
    }
    
    public void testRetCurent() throws Exception {
    	setRetNull(false);
    	setUseCurrent(true);
    	Calendar cal = Calendar.getInstance();
    	cal.set(2007, 10, 22, 0, 0, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date d = cal.getTime();
    	assertTrue(d.before(runWith(d)));
    }
    
    public void test20011002DateAndTime() throws Exception{
    	setFormat(0);
    	Calendar cal = Calendar.getInstance();
    	cal.set(2001, 10, 02, 0, 0, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date d = cal.getTime();
    	assert(runWith(d) instanceof Date);
    }
    
    public void test20011002Date() throws Exception{
    	setFormat(1);
    	Calendar cal = Calendar.getInstance();
    	cal.set(2001, 10, 02, 0, 0, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date d = cal.getTime();
    	assert(runWith(d) instanceof java.sql.Date);
    }
    
    public void test20011002Time() throws Exception{
    	setFormat(1);
    	Calendar cal = Calendar.getInstance();
    	cal.set(2001, 10, 02, 0, 0, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date d = cal.getTime();
    	assert(runWith(d) instanceof java.sql.Time);
    }

	@Override
	protected DateConstantMungeStep getTarget() {
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
    
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// Already in AbstractMungeStep
	}
}
