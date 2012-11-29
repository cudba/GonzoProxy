package ch.compass.gonzoproxy.relay.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import ch.compass.gonzoproxy.mvc.model.Field;


public class YamlTemplatesTest extends TestCase {
	@
	Test
	public void testLoadAtsTemplate() throws FileNotFoundException {
		InputStream input = new FileInputStream("templates/ats.apdu");
			Yaml beanLoader = new Yaml();
			PacketTemplate parsed = beanLoader.loadAs(input, PacketTemplate.class);
			
			assertNotNull(parsed);
			
			int expectedAtsFields = 9;
			ArrayList<Field> fields = parsed.getFields();
			assertEquals(expectedAtsFields, fields.size());
	}
	

}
