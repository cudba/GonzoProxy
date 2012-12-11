package ch.compass.gonzoproxy;

import ch.compass.gonzoproxy.controller.RelayController;
import ch.compass.gonzoproxy.view.GonzoProxyFrame;

public class GonzoProxy {

	public static void main(String[] args) {

		RelayController controller = new RelayController();
		GonzoProxyFrame frame = new GonzoProxyFrame(controller);
		frame.setVisible(true);

	}

}
