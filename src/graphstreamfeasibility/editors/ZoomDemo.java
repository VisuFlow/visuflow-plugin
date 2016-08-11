package graphstreamfeasibility.editors;

import org.graphstream.ui.view.View;

public class ZoomDemo implements Runnable{
	
	private View view;

	ZoomDemo(View view)
	{
		this.view = view;
	}
	
	protected void demonstrateZoom() {
		// TODO Auto-generated method stub
		double max = 5.0;
		double min = 0.0;
		double step = .20;
		long sleep = 100;
		
		for(double i=min;i<max;i+=step)
		{
			view.getCamera().setViewPercent(i);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for(double i=max;i>min;i-=step)
		{
			view.getCamera().setViewPercent(i);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		demonstrateZoom();
	}
}
