package com.example.eval;

import java.io.File;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class JavaCoverageEvaluator {

    public static double parseCoverageFromJacoco(String xmlPath) throws Exception {
        File file = new File(xmlPath);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(file));

        int total = Integer.parseInt(doc.getElementsByTagName("counter").item(0).getAttributes().getNamedItem("missed").getNodeValue()) +
                    Integer.parseInt(doc.getElementsByTagName("counter").item(0).getAttributes().getNamedItem("covered").getNodeValue());

        int covered = Integer.parseInt(doc.getElementsByTagName("counter").item(0).getAttributes().getNamedItem("covered").getNodeValue());

        return total == 0 ? 0 : (double) covered / total * 100;
    }
}