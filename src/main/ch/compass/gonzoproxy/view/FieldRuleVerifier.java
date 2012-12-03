package ch.compass.gonzoproxy.view;

import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

class FieldRuleVerifier extends InputVerifier implements CaretListener {
	
	public boolean verify(JComponent input) {
		JTextField tf = (JTextField) input;
		if(!tf.getText().matches("[a-f0-9][a-f0-9](\\s[a-f0-9][a-f0-9])*")){
			tf.setBackground(new Color(255, 99, 71));
			return false;
		}

		tf.setBackground(new Color(255, 255, 255));
		return true;
	}

	public void caretUpdate(CaretEvent input) {
		if (((JTextField) input.getSource()).hasFocus()) {
			verify((JComponent) input.getSource());
		}
	}
}
