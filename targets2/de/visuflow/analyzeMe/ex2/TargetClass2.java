package de.visuflow.analyzeMe.ex2;

public class TargetClass2 {
	
	private String whatsoever;
	
	private void leak(String data) {
		System.out.println("Leak: " + data);
	}
	public void sourceToSink() {
		String x = getSecret();
		String y = x;
		leak(y);
		int z =0;
		if(z==0)
		{
			System.out.println("In Loop");
		}
		else
		{
			System.out.println("Outside loop");
		}
	}
	
	
	private String getSecret() {
		return "top secret";
	}
	

}
