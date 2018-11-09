package bwfdm.sara.transfer;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MetadataFormatter {
	private static final DocumentBuilderFactory DBF = DocumentBuilderFactory
			.newInstance();
	private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String DC = "http://purl.org/dc/elements/1.1/";

	private final Document doc;
	private final Element desc;
	
	public MetadataFormatter()  {
		try {
			doc = DBF.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("cannot create DocumentBuilder", e);
		}
		final Element root = doc.createElementNS(RDF, "RDF");
		root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:dc",
				DC);
		desc = doc.createElementNS(RDF, "Description");
		root.appendChild(desc);
		doc.appendChild(root);
	}

	public void addDC(final String name, final String value) {
		if (value == null || value.isEmpty())
			return;
		final Element elem = doc.createElementNS(DC, "dc:" + name);
		elem.setTextContent(value);
		desc.appendChild(elem);
	}

	public String getSerializedXML() {
		final OutputFormat format = new OutputFormat();
		format.setLineWidth(0);
		format.setIndent(2);
		format.setEncoding("UTF-8");
		final StringWriter buffer = new StringWriter();
		final XMLSerializer ser = new XMLSerializer(buffer, format);
		ser.setNamespaces(true);
		try {
			ser.serialize(doc);
		} catch (IOException e) {
			throw new RuntimeException(
					"StringWriter is throwing IOExceptions?!", e);
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws Exception {
		MetadataFormatter test = new MetadataFormatter();
		test.addDC("creator", "JB Johnson");
		test.addDC("date", "2018-07-13T14:17:17+00:00");
		test.addDC("title", "The Collected Works of W. Shakespeare");
		test.addDC("description",
				"Blah blah important writer "
						+ "blah blah all works in one place "
						+ "blah blah blah phantastic book.");
		test.addDC("identifier", "3t7n90387nt92d7t");
		test.addDC("publisher", "SARA service");
		test.addDC("rights", "MIT");
		test.addDC("type", "Software");
		System.out.println(test.getSerializedXML());
	}
}
