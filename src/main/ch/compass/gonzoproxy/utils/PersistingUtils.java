package ch.compass.gonzoproxy.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistingUtils {

	public static Object loadFile(File file) throws IOException,
			ClassNotFoundException {
		FileInputStream fin = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fin);
		Object loadedObject = ois.readObject();
		ois.close();
		return loadedObject;
	}

	public static void saveFile(File file, Object persistingObject)
			throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(persistingObject);
		oos.close();
	}
}
