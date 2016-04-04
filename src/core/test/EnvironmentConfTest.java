package core.test;

import core.EnvironmentConf;
import junit.framework.TestCase;

public class EnvironmentConfTest extends TestCase {
	protected EnvironmentConf env;
	
	protected void setUp() {
		env = new EnvironmentConf(){
			@Override
			public String getControllerHostname() {
				return "PlaceHolder";
			}
			
		};
	}
	
	public void testMyHostname() {
		String myHostname = env.getMyHostName();
		assertTrue(myHostname != null);
		assertTrue(!myHostname.isEmpty());
	}
}
