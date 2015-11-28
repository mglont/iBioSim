package biomodel.gui.sbol;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import main.Gui;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import sbol.util.SBOLUtility;
import sbol.util.SBOLUtility2;
import uk.ac.ncl.intbio.core.io.CoreIoException;
import biomodel.annotation.AnnotationUtility;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;

public class SBMLtoSBOL {
	BioModel bioModel;
	String path;
	SBOLDocument SBOLDOC;
	
	String VERSION = "";
	
	URI COLLECTION_ID ;
	URI LANGUAGE  = org.sbolstandard.core2.Model.SBML;
	URI FRAMEWORK = SystemsBiologyOntology.DISCRETE_FRAMEWORK;
	
	public SBMLtoSBOL(Gui gui,String path,BioModel bioModel) 
	{
		this.path = path;
		this.bioModel = bioModel;
		SBOLDOC = new SBOLDocument();
		HashSet<String> sbolFilePaths = gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION);
		loadSBOLFiles(sbolFilePaths);
	}
	
	private boolean loadSBOLFiles(HashSet<String> sbolFilePaths) 
	{
		for (String filePath : sbolFilePaths) 
		{
			SBOLDocument sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
			if (sbolDoc != null) 
			{
				for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
				{
					if(SBOLDOC.getComponentDefinition(c.getIdentity()) == null) 
					{
						SBOLDOC.createCopy(c);
					}
				}
				for(Sequence c : sbolDoc.getSequences())
				{
					if(SBOLDOC.getSequence(c.getIdentity()) == null) 
					{
						SBOLDOC.createCopy(c);
					}
				}
				
			} 
			else
				return false;
		}
		return true;
	}
	
	public void export(String exportFilePath) {
		// TODO read existing 1.1 document in the project to get sequences etc.
		SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
		SBOLDocument sbolDoc = new SBOLDocument();
		Preferences biosimrc = Preferences.userRoot();
		sbolDoc.setDefaultURIprefix(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE,""));
		sbolDoc.setComplete(true);
		sbolDoc.setTypesInURIs(true);
		
		String collection_id = "collection__" + bioModel.getSBMLDocument().getModel().getId();
		Collection collection = sbolDoc.createCollection(collection_id, VERSION);
		export_recurse("file:" + bioModel.getSBMLFile(),sbmlDoc,sbolDoc,collection); 
	    
		try 
	    {
			try 
			{
				SBOLWriter.write(sbolDoc, exportFilePath);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (CoreIoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		catch (FactoryConfigurationError e) { e.printStackTrace(); } 
	}
	
	public void export_recurse(String source,SBMLDocument sbmlDoc,SBOLDocument sbolDoc,Collection collection) 
	{
		Model model    = sbmlDoc.getModel();
		//String modelId = model.getId();
		URI sourceURI  = URI.create(source);
		
		org.sbolstandard.core2.Model sbolModel = sbolDoc.createModel(model.getId(), VERSION, sourceURI, LANGUAGE, FRAMEWORK);
		collection.addMember(sbolModel.getIdentity());	
		
		String identityStr  = model.getId();
		ModuleDefinition moduleDef = sbolDoc.createModuleDefinition(identityStr, VERSION);
		collection.addMember(moduleDef.getIdentity());

		for (int i = 0; i < model.getSpeciesCount(); i++) 
		{
			// convert species to a component definition
			Species species = model.getSpecies(i);
			ComponentDefinition compDef = setComponentDefinition(sbolDoc, model, species, collection);
			collection.addMember(compDef.getIdentity());
			
			setFunctionalComponent(sbmlDoc, moduleDef, compDef, species);
		}
		
		for (int i = 0; i < model.getReactionCount(); i++) 
		{
			Reaction reaction = model.getReaction(i);
			
			// if production reaction, then you want to examine the modifiers, and create interactions for 
			// each modifier that is a repressor from this species to the promoter
			if(BioModel.isProductionReaction(reaction))
			{
				extractProductionReaction(moduleDef, reaction);
			}
			// if complex reaction, then create on interaction
			else if(BioModel.isComplexReaction(reaction))
			{	
				extractComplexReaction(moduleDef, reaction);
			}
			// if degradation reaction, then create an interaction
			else if(BioModel.isDegradationReaction(reaction))
			{
				extractDegradationReaction(moduleDef, reaction);
			}
			else 
			{
				Set<URI> types = new HashSet<URI>();
				types.add(SystemsBiologyOntology.BIOCHEMICAL_REACTION);
				
				Interaction inter = moduleDef.createInteraction(reaction.getId(), types);
				
				for(SpeciesReference reactant : reaction.getListOfReactants())
				{
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(SystemsBiologyOntology.REACTANT);
					Participation p = inter.createParticipation(reactant.getSpecies(), reactant.getSpecies());
					p.setRoles(roles_r);
				}
				for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Set<URI> roles_r = new HashSet<URI>();
					roles_r.add(SystemsBiologyOntology.MODIFIER);
					Participation p = inter.createParticipation(modifier.getSpecies(), modifier.getSpecies());
					p.setRoles(roles_r);
				}
				for(SpeciesReference product : reaction.getListOfProducts())
				{
					// create participation from product.getSpecies() as type product
					Set<URI> roles_p = new HashSet<URI>();
					roles_p.add(SystemsBiologyOntology.PRODUCT);
					Participation p = inter.createParticipation(product.getSpecies(), product.getSpecies());
					p.setRoles(roles_p);
				}
			}
		}
		extractSubModels(sbmlDoc, sbolDoc, collection, moduleDef, model);
	}
	
	public void recurseComponentDefinition(SBOLDocument sbolDoc,ComponentDefinition cd,Collection collection) {
		for (org.sbolstandard.core2.Component comp : cd.getComponents()) {
			if (sbolDoc.getComponentDefinition(comp.getDefinitionURI())==null) {
				ComponentDefinition compDef = comp.getDefinition();
				sbolDoc.createCopy(compDef);
				collection.addMember(compDef.getIdentity());
				for (Sequence sequence : compDef.getSequences()) {
					if (sbolDoc.getSequence(sequence.getIdentity())==null) {
						sbolDoc.createCopy(sequence);
						collection.addMember(sequence.getIdentity());
					}
				}
				recurseComponentDefinition(sbolDoc,compDef,collection);
			}
		}
	}
	
	public ComponentDefinition setComponentDefinition(SBOLDocument sbolDoc, Model model, Species species, 
			Collection collection)
	{
		String compDef_identity =  model.getId() + "__" + species.getId();
		
		Set<URI> compDef_type = new HashSet<URI>();
		Set<URI> compDef_role = new HashSet<URI>();
		ComponentDefinition compDef = null;
		
		if (BioModel.isPromoterSpecies(species)) 
		{
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			if (sbolURIs.size()>0) {
				// TODO: need to figure out what to do when size is greater than 1
				compDef = SBOLDOC.getComponentDefinition(sbolURIs.get(0));
				if (compDef!=null) {
					if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
						sbolDoc.createCopy(compDef);
						collection.addMember(compDef.getIdentity());
						for (Sequence sequence : compDef.getSequences()) {
							if (sbolDoc.getSequence(sequence.getIdentity())==null) {
								sbolDoc.createCopy(sequence);
								collection.addMember(sequence.getIdentity());
							}
						}
						recurseComponentDefinition(sbolDoc,compDef,collection);
					}
					return compDef;
				}
			}
			Reaction production = BioModel.getProductionReaction(species.getId(),model);
			if (production!=null) {
				sbolURIs = new LinkedList<URI>();
				sbolStrand = AnnotationUtility.parseSBOLAnnotation(production, sbolURIs);
				if (sbolURIs.size()>0) {
					compDef = SBOLDOC.getComponentDefinition(sbolURIs.get(0));
					if (compDef!=null) {
						if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
							sbolDoc.createCopy(compDef);
							collection.addMember(compDef.getIdentity());
							for (Sequence sequence : compDef.getSequences()) {
								if (sbolDoc.getSequence(sequence.getIdentity())==null) {
									sbolDoc.createCopy(sequence);
									collection.addMember(sequence.getIdentity());
								}
							}
							recurseComponentDefinition(sbolDoc,compDef,collection);
						}
						return compDef;
					}
				}
			}
			compDef_type.add(ComponentDefinition.DNA);
			compDef_role.add(SequenceOntology.PROMOTER);
		} 
		// TODO: other cases for other SBO terms
		else 
		{
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(species, sbolURIs);
			if (sbolURIs.size()>0) {
				// TODO: what if more than 1
				compDef = SBOLDOC.getComponentDefinition(sbolURIs.get(0));
				if (compDef!=null) {
					if (sbolDoc.getComponentDefinition(compDef.getIdentity())==null) {
						sbolDoc.createCopy(compDef);
						collection.addMember(compDef.getIdentity());
						for (Sequence sequence : compDef.getSequences()) {
							if (sbolDoc.getSequence(sequence.getIdentity())==null) {
								sbolDoc.createCopy(sequence);
								collection.addMember(sequence.getIdentity());
							}
						}
						recurseComponentDefinition(sbolDoc,compDef,collection);
					}
					return compDef;
				}
			}
			compDef_type.add(ComponentDefinition.PROTEIN);
		}
		compDef = sbolDoc.createComponentDefinition(compDef_identity, VERSION, compDef_type);
		return compDef; 
	}
	
	public FunctionalComponent setFunctionalComponent(SBMLDocument sbmlDoc, ModuleDefinition moduleDef, ComponentDefinition compDef, Species species)
	{
		AccessType access; 
		DirectionType direction;
		// create FunctionalComponents for these within the module
		String funcComp_identity =  species.getId();
		
		if (SBMLutilities.isInput(sbmlDoc,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.IN;
		} 
		else if (SBMLutilities.isOutput(sbmlDoc,species.getId())) 
		{
			access    = AccessType.PUBLIC;
			direction = DirectionType.OUT;
		} 
		else 
		{
			access    = AccessType.PRIVATE; 
			direction = DirectionType.NONE;
		}
		
		return moduleDef.createFunctionalComponent(funcComp_identity, access, compDef.getIdentity(), direction);
	}
	
	public void extractProductionReaction(ModuleDefinition moduleDef, Reaction reaction)
	{
		List<ModifierSpeciesReference> repressors = new ArrayList<ModifierSpeciesReference>();
		List<ModifierSpeciesReference> activators = new ArrayList<ModifierSpeciesReference>(); 
		String promoterId = "";
		for(ModifierSpeciesReference modifier : reaction.getListOfModifiers())
		{

			if (BioModel.isPromoter(modifier)) 
			{
				promoterId = modifier.getSpecies(); 
			} 
			else if (BioModel.isRepressor(modifier)) 
			{
				repressors.add(modifier);
			} 
			else if (BioModel.isActivator(modifier)) 
			{
				activators.add(modifier);
			} 
			else if (BioModel.isRegulator(modifier)) 
			{
				repressors.add(modifier);
				activators.add(modifier);
			}
		}
		
		for(ModifierSpeciesReference r : repressors)
		{
			String inter_id = r.getSpecies() + "_rep_" + promoterId;
			
			Set<URI> types = new HashSet<URI>();
			types.add(SystemsBiologyOntology.GENETIC_SUPPRESSION);
			
			Interaction interaction = moduleDef.createInteraction(inter_id, types);
			
			Participation p1 = interaction.createParticipation(promoterId, promoterId);
			Participation p2 = interaction.createParticipation(r.getSpecies(), r.getSpecies());
			
			Set<URI> roles1 = new HashSet<URI>();
			roles1.add(SystemsBiologyOntology.PROMOTER);
			
			Set<URI> roles2 = new HashSet<URI>();
			roles2.add(SystemsBiologyOntology.INHIBITOR);
			
			p1.setRoles(roles1);
			p2.setRoles(roles2);
		}
		
		// Repeat same steps for the list of activators
		for(ModifierSpeciesReference a : activators)
		{
			String inter_id ="_act_" + a.getSpecies();
			
			Set<URI> types = new HashSet<URI>();
			types.add(SystemsBiologyOntology.GENETIC_ENHANCEMENT); 
			
			Interaction interaction = moduleDef.createInteraction(inter_id, types);
			
			Participation p1 = interaction.createParticipation(promoterId, promoterId);
			Participation p2 = interaction.createParticipation(a.getSpecies(), a.getSpecies());
			
			Set<URI> roles1 = new HashSet<URI>();
			roles1.add(SystemsBiologyOntology.PROMOTER);
			
			Set<URI> roles2 = new HashSet<URI>();
			roles2.add(SystemsBiologyOntology.STIMULATOR);
			
			p1.setRoles(roles1);
			p2.setRoles(roles2);
		}
		
		int prod_partPrefix = 1;
		for(SpeciesReference product : reaction.getListOfProducts())
		{
			String i_id = promoterId + "_prod_" + product.getSpecies();
			
			Set<URI> type = new HashSet<URI>();
			type.add(SystemsBiologyOntology.GENETIC_PRODUCTION);
			
			Interaction interaction = moduleDef.createInteraction(i_id, type);
			Participation p1 = interaction.createParticipation(promoterId, promoterId);
			Participation p2 = interaction.createParticipation(product.getSpecies(), product.getSpecies());
			
			Set<URI> roles1 = new HashSet<URI>();
			roles1.add(SystemsBiologyOntology.PROMOTER);
			
			Set<URI> roles2 = new HashSet<URI>();
			roles2.add(SystemsBiologyOntology.PRODUCT);
			
			p1.setRoles(roles1);
			p2.setRoles(roles2);
		}
	}
	
	public void extractComplexReaction(ModuleDefinition moduleDef, Reaction reaction)
	{
		Set<URI> type = new HashSet<URI>();
		type.add(SystemsBiologyOntology.NON_COVALENT_BINDING);
		
		Interaction inter = moduleDef.createInteraction(reaction.getId(), type);
		
		int reac_partPrefix = 1;
		int prod_partPrefix = 1;
		
		for(SpeciesReference reactant : reaction.getListOfReactants())
		{
			Participation p = inter.createParticipation(reactant.getSpecies(), reactant.getSpecies());
			
			Set<URI> roles_reac = new HashSet<URI>();
			roles_reac.add(SystemsBiologyOntology.REACTANT);
			
			p.setRoles(roles_reac);
		}
		for(SpeciesReference product : reaction.getListOfProducts())
		{
			Participation p = inter.createParticipation(product.getSpecies(), product.getSpecies());
			
			Set<URI> roles_prod = new HashSet<URI>();
			roles_prod.add(SystemsBiologyOntology.PRODUCT);
			
			p.setRoles(roles_prod); 
		}
	}
	
	public void extractDegradationReaction(ModuleDefinition moduleDef, Reaction reaction)
	{
		Set<URI> types = new HashSet<URI>();
		types.add(SystemsBiologyOntology.DEGRADATION);
		
		Interaction inter = moduleDef.createInteraction(reaction.getId(), types);
		
		for(SpeciesReference sp : reaction.getListOfReactants())
		{
			Set<URI> roles_sp = new HashSet<URI>();
			roles_sp.add(SystemsBiologyOntology.REACTANT);
			String p_id = sp.getSpecies();
			Participation p = inter.createParticipation(p_id, sp.getSpecies());
			p.setRoles(roles_sp);
		}
	}
	
	public void extractSubModels(SBMLDocument sbmlDoc, SBOLDocument sbolDoc, Collection collection, ModuleDefinition moduleDef, Model model)
	{
		ArrayList<String> comps = new ArrayList<String>();
		CompSBMLDocumentPlugin sbmlComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(sbmlDoc.getModel());
		if (sbmlCompModel.getListOfSubmodels().size()>0) 
		{
			CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(sbmlDoc);
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
				String subModelId = sbmlCompModel.getListOfSubmodels().get(i).getId();
				String extModel = sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
						.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
				SBMLDocument subDocument = SBMLutilities.readSBML(path + Gui.separator + extModel);
				if (!comps.contains(extModel)) 
				{
					comps.add(extModel);
					export_recurse(sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
							.getModelRef()).getSource(),subDocument,sbolDoc,collection);
				}
				Module m = moduleDef.createModule(subModelId, subDocument.getModel().getId(), VERSION);
				
				for (int j = 0; j < model.getSpeciesCount(); j++) 
				{
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(j));
					for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) 
					{
						ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
						if (replacement.getSubmodelRef().equals(subModelId)) 
						{
							if (replacement.isSetPortRef()) 
							{
								String mapId = model.getSpecies(j).getId();
					
								RefinementType refinement = RefinementType.USELOCAL;
								m.createMapsTo(mapId, refinement, model.getSpecies(j).getId(), 
										replacement.getPortRef().replace("output__","").replace("input__",""));
							}
						}
					}
					if (sbmlSBase.isSetReplacedBy()) 
					{
						ReplacedBy replacement = sbmlSBase.getReplacedBy();
						if (replacement.getSubmodelRef().equals(subModelId)) 
						{
							if (replacement.isSetPortRef()) 
							{
								String mapId = model.getSpecies(j).getId(); 

								RefinementType refinement = RefinementType.USEREMOTE;
								m.createMapsTo(mapId, refinement, model.getSpecies(j).getId(), 
										replacement.getPortRef().replace("output__","").replace("input__",""));
							} 
						}
					}
				}
			}
		}
	}
}
