package sbol;

import java.awt.*;

import javax.swing.*;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import sbol.SBOLUtility;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class SBOLAssociationPanel extends JPanel {

	private HashSet<String> sbolFilePaths;
//	private HashMap<String, SBOLDocument> sbolFileDocMap = new HashMap<String, SBOLDocument>();
	private List<URI> compURIs;
	private List<URI> defaultCompURIs;
	private Set<String> soTypes;
	private String modelID;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
	private JList compList = new JList();
	private String[] options;
	private boolean iBioSimURIPresent;
	private URI deletionURI;
	
	public SBOLAssociationPanel(HashSet<String> sbolFilePaths, List<URI> defaultCompURIs, Set<String> soTypes, String modelID) {
		super(new BorderLayout());
		
		this.sbolFilePaths = sbolFilePaths;
		
		if (defaultCompURIs.size() == 0) {
			compURIs = new LinkedList<URI>();
			insertPlaceHolder();
		} else 
			compURIs = new LinkedList<URI>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
		this.soTypes = soTypes;
		this.modelID = modelID;
		
		
		options = new String[]{"Add/Move Composite", "Add", "Remove", "Ok", "Cancel"};
		constructPanel();
	}
	
	public SBOLAssociationPanel(HashSet<String> sbolFilePaths, List<URI> defaultCompURIs, Set<String> soTypes) {
		super(new BorderLayout());
		
		this.sbolFilePaths = sbolFilePaths;
			
		compURIs = new LinkedList<URI>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
		this.soTypes = soTypes;
		
		options = new String[]{"Add", "Remove", "Ok", "Cancel"};
		constructPanel();
	}
	
	public void constructPanel() {
		JLabel associationLabel = new JLabel("Associated DNA Components:");
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		this.add(associationLabel, "North");
		this.add(componentScroll, "Center");
		
		if (loadSBOLFiles(sbolFilePaths)) {
			boolean display = setComponentIDList();
			while (display)
				display = panelOpen();
		}
	}
	
	private boolean loadSBOLFiles(HashSet<String> sbolFilePaths) {
		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		for (String filePath : sbolFilePaths) {
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
//			sbolFileDocMap.put(filePath, sbolDoc);
			if (sbolDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
				compResolvers.add(flattenedDoc.getComponentUriResolver());
			} else
				return false;
		}
		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
		aggregateCompResolver.setResolvers(compResolvers);
		if (sbolFilePaths.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean setComponentIDList() {
		LinkedList<String> compIdNames = new LinkedList<String>();
		LinkedList<Integer> dissociationIndices = new LinkedList<Integer>();
		iBioSimURIPresent = false;
		for (int i = compURIs.size() - 1; i >= 0; i--) {
			URI uri = compURIs.get(i);
			String compIdName = "";
			if (uri.toString().endsWith("iBioSimPlaceHolder")) {
				iBioSimURIPresent = true;
				compIdName = modelID + " (Placeholder for iBioSim Composite DNA Component)";
			} else {
				DnaComponent resolvedComp = aggregateCompResolver.resolve(uri);
				if (resolvedComp != null) {
					compIdName = resolvedComp.getDisplayId();
					if (resolvedComp.getName() != null && !resolvedComp.getName().equals(""))
						compIdName = compIdName + " : " + resolvedComp.getName();
					if (uri.toString().endsWith("iBioSim")) {
						iBioSimURIPresent = true;
						compIdName = compIdName + " (iBioSim Composite DNA Component)";
					}
				} else {
					Object[] options = { "OK", "Cancel" };
					int choice = JOptionPane.showOptionDialog(Gui.frame, 
							"Currently associated DNA component with URI " + uri.toString() +
							" is not found in project SBOL files and could not be loaded. " +
							"Would like to dissociate this component?", 
							"DNA Component Not Found", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
					if (choice == 0) 
						dissociationIndices.add(i);
					else
						return false;
				}
			}
			if (compIdName.length() > 0)
				compIdNames.addFirst(compIdName);
		}
		Object[] idObjects = compIdNames.toArray();
		compList.setListData(idObjects);
		for (int i : dissociationIndices) 
			if (compURIs.remove(i).toString().endsWith("iBioSim"))
				insertPlaceHolder();
		return true;
	}
	
	private boolean panelOpen() {
		int mode = 5 - options.length;
		int choice = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Association", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (choice >= 0)
			choice += mode;
		if (choice == 0) {
			if (iBioSimURIPresent) {
				moveIBioSimURI();
			} else 
				insertPlaceHolder();
			return true;
		} else if (choice == 1) {
			SBOLBrowser browser = new SBOLBrowser(sbolFilePaths, soTypes, getSelectedURIs());
			insertComponents(browser.getSelection());
			return true;
		} else if (choice == 2) {
			removeSelectedURIs();
			return true;
		} else if (choice == 3) 
			return false;
		else {
			compURIs = defaultCompURIs;
			return false;
		}
	}
	
	private void moveIBioSimURI() {
		int[] selectedIndices = compList.getSelectedIndices();
		int destinationIndex;
		if (selectedIndices.length == 0)
			destinationIndex = compURIs.size();
		else
			destinationIndex = selectedIndices[selectedIndices.length - 1];
		int originIndex = -1;
		do {
			originIndex++;
		} while (!compURIs.get(originIndex).toString().endsWith("iBioSim") 
				&& !compURIs.get(originIndex).toString().endsWith("iBioSimPlaceHolder"));
		URI compURI = compURIs.remove(originIndex);
		if (destinationIndex > originIndex)
			destinationIndex--;
		compURIs.add(destinationIndex, compURI);
		setComponentIDList();
	}
	
	private LinkedList<URI> getSelectedURIs() {
		LinkedList<URI> selectedURIs = new LinkedList<URI>();
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = 0; i < selectedIndices.length; i++)
			selectedURIs.add(compURIs.get(selectedIndices[i]));
		return selectedURIs;
	}
	
	private void insertComponents(LinkedList<URI> insertionURIs) {
		int[] selectedIndices = compList.getSelectedIndices();
		int insertionIndex;
		if (selectedIndices.length == 0)
			insertionIndex = compURIs.size();
		else
			insertionIndex = selectedIndices[selectedIndices.length - 1];
		compURIs.addAll(insertionIndex, insertionURIs);
		setComponentIDList();
	}
	
	private void insertPlaceHolder() {
		LinkedList<URI> tempList = new LinkedList<URI>();
		try {
			tempList.add(new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		insertComponents(tempList);
	}
	
	private void removeSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		if (selectedIndices.length == 0) {
			if (compURIs.size() > 0) {
				URI compURI = compURIs.remove(compURIs.size() - 1);
				if (compURI.toString().endsWith("iBioSim"))
					deletionURI = compURI;
			}
		} else 
			for (int i = selectedIndices.length; i > 0; i--) {
				URI compURI = compURIs.remove(selectedIndices[i - 1]);
				if (compURI.toString().endsWith("iBioSim"))	
					deletionURI = compURI;
			}
		setComponentIDList();
	}
	
	public List<URI> getCompURIs() {
		return compURIs;
	}
	
	public URI getDeletionURI() {
		return deletionURI;
	}
}
