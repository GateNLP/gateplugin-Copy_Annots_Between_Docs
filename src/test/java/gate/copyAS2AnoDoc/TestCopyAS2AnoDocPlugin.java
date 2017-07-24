/*
 *  TestCopyAS2AnoDocPlugin.java
 * 
 *  Yaoyong Li 08/10/2007
 *
 *  $Id: TestCopyAS2AnoDocPlugin.java, v 1.0 2009-05-10 11:44:16 +0000 yaoyong $
 */
package gate.copyAS2AnoDoc;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.GateConstants;
import gate.test.GATEPluginTestCase;
import gate.util.ExtensionFileFilter;
import gate.util.Files;

public class TestCopyAS2AnoDocPlugin extends GATEPluginTestCase {
  
  /** The test for AnnotationMerging. */
  public void testCopyAS2AnoDocPlugin() throws Exception {
    Boolean savedSpaceSetting = Gate.getUserConfig().getBoolean(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME);
    Gate.getUserConfig().put(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME, Boolean.FALSE);
    //Create a object for merging
    CopyAS2AnoDocMain copyAnnsOne = (CopyAS2AnoDocMain)Factory
      .createResource("gate.copyAS2AnoDoc.CopyAS2AnoDocMain");
    //A corpus
    
    File testData = Files.fileFromURL(this.getClass().getResource("/creole.properties")).getParentFile();    
    
    
    Corpus corpus = Factory.newCorpus("DataSet");
    ExtensionFileFilter fileFilter = new ExtensionFileFilter();
    fileFilter.addExtension("xml");
    
    File sourceDir = new File(testData, "source");
    File targetDir = new File(testData, "target");
    
    File[] xmlFiles = targetDir.listFiles(fileFilter);
    Arrays.sort(xmlFiles, new Comparator<File>() {
      public int compare(File a, File b) {
        return a.getName().compareTo(b.getName());
      }
    });
    for(File f : xmlFiles) {
      if(!f.isDirectory()) {
        Document doc = Factory.newDocument(f.toURI().toURL(), "UTF-8");
        doc.setName(f.getName());
        corpus.add(doc);
      }
    }

    gate.creole.SerialAnalyserController controller;
    controller = (gate.creole.SerialAnalyserController)Factory
      .createResource("gate.creole.SerialAnalyserController");
    controller.setCorpus(corpus);
    controller.add(copyAnnsOne);
    copyAnnsOne.setInputASName("ann1");
    copyAnnsOne.setOutputASName("ann5");
    copyAnnsOne.setSourceFilesURL(sourceDir.toURI().toURL());
    Vector<String>annTypes = new Vector<String>();
    annTypes.add("Os");
    annTypes.add("sent");
    copyAnnsOne.setAnnotationTypes(annTypes);
    
    controller.execute();
    
    Document doc = corpus.get(1);
    AnnotationSet anns = doc.getAnnotations("ann5").get("Os");
    int num;
    num = anns.size();
    assertEquals(num, 3);
    anns = doc.getAnnotations("ann5").get("sent");
    num = anns.size();
   
    assertEquals(num, 18);
    doc.removeAnnotationSet("ann5");
    
    doc = corpus.get(0);
    doc.removeAnnotationSet("ann5");
    
    System.out.println("completed");
    corpus.clear();
    Factory.deleteResource(corpus);
    controller.cleanup();
    Factory.deleteResource(controller);

    // finally {
    Gate.getUserConfig().put(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
      savedSpaceSetting);
    // }
  }
}
