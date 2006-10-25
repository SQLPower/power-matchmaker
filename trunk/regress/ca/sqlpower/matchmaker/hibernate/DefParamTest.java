package ca.sqlpower.matchmaker.hibernate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diasparsoftware.util.junit.ValueObjectEqualsTest;

/**
 * Test DefParam, which is all about many fields
 */
public class DefParamTest extends ValueObjectEqualsTest {

	static Map<String, Class> map  = new HashMap<String, Class>();

	static {
		map.put("companyName", String.class);
		map.put("commitFreq", Integer.class);
		map.put("processAddInd", boolean.class);
		map.put("processUpdInd", boolean.class);
		map.put("processDelInd", boolean.class);
		map.put("writeDbErrorsInd", boolean.class);
		map.put("showProgressFreq", Integer.class);
		map.put("fiscalYearEndDate", Date.class);
		map.put("defaultLogFilePath", String.class);
		map.put("defaultErrFilePath", String.class);
		map.put("defaultBadFilePath", String.class);
		map.put("defaultUnixLogFilePath", String.class);
		map.put("defaultUnixErrFilePath", String.class);
		map.put("defaultUnixBadFilePath", String.class);
		map.put("appendToLogInd", boolean.class);
		map.put("appendToErrInd", boolean.class);
		map.put("appendToBadInd", boolean.class);
		map.put("rollbackSegmentName", String.class);
		map.put("lastUpdateDate", Date.class);
		map.put("lastUpdateUser", String.class);
		map.put("defaultDataFilePath", String.class);
		map.put("defaultUnixDataFilePath", String.class);
		map.put("trialInd", boolean.class);
		map.put("trialLength", BigDecimal.class);
		map.put("emailNotificationReturnAdrs", String.class);
		map.put("mailServerName", String.class);
		map.put("engineExePath", String.class);
		map.put("tableTablespaceName", String.class);
		map.put("tableInitialExtent", BigDecimal.class);
		map.put("tableNextExtent", BigDecimal.class);
		map.put("tablePctIncrease", BigDecimal.class);
		map.put("indexTablespaceName", String.class);
		map.put("indexInitialExtent", BigDecimal.class);
		map.put("indexNextExtent", BigDecimal.class);
		map.put("indexPctIncrease", BigDecimal.class);
		map.put("mmDefaultLogFilePath", String.class);
		map.put("mmAppendToLogInd", boolean.class);
		map.put("summDefaultLogFilePath", String.class);
		map.put("summAppendToLogInd", boolean.class);
		map.put("lastUpdateOsUser", String.class);
		map.put("webReportsUrl", String.class);
		map.put("scrcrdDefaultLogFilePath", String.class);
		map.put("scrcrdAppendToLogInd", boolean.class);
		map.put("dbHost", String.class);
		map.put("portNo", BigDecimal.class);
		map.put("dbName", String.class);
		map.put("dashboardUrl", String.class);
		map.put("schemaVersion", String.class);
		map.put("defaultScriptFilePath", String.class);
		map.put("licensedCdnAdrsCorrInd", boolean.class);
		map.put("licensedUsAdrsCorrInd", boolean.class);
		map.put("mmDefaultScriptFilePath", String.class);
		map.put("summDefaultScriptFilePath", String.class);
		map.put("dashDefaultScriptFilePath", String.class);
		map.put("versionControlInd", boolean.class);
		map.put("dashScrcrdBaseRank", BigDecimal.class);
		map.put("compileInd", boolean.class);
		map.put("audDefaultLogFilePath", String.class);
		map.put("audDefaultScriptFilePath", String.class);
		map.put("dashDefaultTargetGrowthPct", BigDecimal.class);
	}

	@Override
	protected Object createControlInstance() throws Exception {
		return new DefParam();
	}

	@Override
	protected Object createInstanceDiffersIn(String fieldName) throws Exception {
		DefParam dp = new DefParam();
		Class c = map.get(fieldName);
		Field f = getField(fieldName);
		f.setAccessible(true);  // bye-bye "private"
		if (c.equals(String.class)) {
			f.set(dp, "testme");
		} else if (c.equals(BigDecimal.class)) {
			f.set(dp, new BigDecimal(123));
		} else if (c.equals(boolean.class)) {
			f.set(dp, Boolean.TRUE);
		} else if (c.equals(java.util.Date.class)) {
			f.set(dp, new Date());
		} else if (c.equals(Integer.class)) {
			f.set(dp, new Integer(111));
		} else throw new IllegalArgumentException(
			"Unhandled type " + c);
		return dp;
	}

	Field[] fields = DefParam.class.getDeclaredFields();

	private Field getField(String name) {
		for (Field f : fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		throw new IllegalArgumentException(name + " is not a field");
	}

	@Override
	protected List<String> keyPropertyNames() {
		List<String> n = new ArrayList<String>();
		for (String s : map.keySet())
			n.add(s);
		return n;
	}


}
