package ch.compass.gonzoproxy.relay.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import org.yaml.snakeyaml.Yaml;

import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.template.PacketTemplate;
import ch.compass.gonzoproxy.model.template.TemplateSettings;

public class ParsingHandler {

	private static final String TEMPLATE_ROOT_FOLDER = "templates/";

	private ArrayList<File> templateFiles = new ArrayList<File>();
	private ArrayList<PacketTemplate> templates = new ArrayList<PacketTemplate>();

	private ParsingUnit parsingUnit = new ParsingUnit();
	private TemplateValidator templateValidator = new TemplateValidator();

	public ParsingHandler() {
		loadTemplates();
	}

	public void tryParse(Packet processingPacket) {
		if (!parseByTemplate(processingPacket))
			parseByDefault(processingPacket);
	}

	public void loadTemplates() {
		findTemplates(TEMPLATE_ROOT_FOLDER);

		for (File templateFile : templateFiles) {
			InputStream fileInput = null;
			try {
				fileInput = new FileInputStream(templateFile);

				Yaml beanLoader = new Yaml();
				PacketTemplate template = beanLoader.loadAs(fileInput,
						PacketTemplate.class);
				templates.add(template);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fileInput != null)
						fileInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private void findTemplates(String directory) {
		File folder = new File(directory);

		File[] files = folder.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				findTemplates(file.getPath());
			} else {
				if (isTemplateFile(file))
					templateFiles.add(file);
			}
		}

	}

	private boolean isTemplateFile(File file) {
		return file.getName().endsWith(TemplateSettings.TEMPLATE_FILE_ENDING);
	}

	private void parseByDefault(Packet processingPacket) {
		parsingUnit.parseByDefault(processingPacket);
	}

	private boolean parseByTemplate(Packet processingPacket) {
		TreeMap<Integer, PacketTemplate> matchingTemplates = new TreeMap<Integer, PacketTemplate>();

		for (PacketTemplate template : templates) {
			if (templateValidator.accept(template, processingPacket)) {
				matchingTemplates.put(template.getFields().size(), template);
			}
		}

		if (matchingTemplates.size() > 0) {
			PacketTemplate bestMatchingTemplate = matchingTemplates.lastEntry()
					.getValue();
			return parsingUnit.parseBy(bestMatchingTemplate, processingPacket);
		}
		return false;
	}
}
