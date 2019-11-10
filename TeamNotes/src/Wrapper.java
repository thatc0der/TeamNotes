import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import bsh.ConsoleInterface;

public class Wrapper implements ConsoleInterface {

	volatile String text = "init val";
	
			
			
	final static ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	final PrintStream out = make();
	
	private static PrintStream make () {
		try {
			return new PrintStream(baos, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	
	
	
	@Override
	public void error(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PrintStream getErr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reader getIn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrintStream getOut() {
		return out;
	}

	@Override
	public void print(Object arg0) {
		getOut().print(arg0);
	}

	@Override
	public void println(Object arg0) {
		getOut().println(arg0);

	}

}
