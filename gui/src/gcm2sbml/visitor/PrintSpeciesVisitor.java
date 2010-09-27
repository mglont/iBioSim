package gcm2sbml.visitor;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import biomodelsim.BioSim;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

public class PrintSpeciesVisitor extends AbstractPrintVisitor {

	public PrintSpeciesVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species, String compartment) {
		super(document);
		this.species = species;
		this.compartment = compartment;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {		
		for (SpeciesInterface specie : species) {
			specie.accept(this);
		}
	}

	public void visitDimer(DimerSpecies specie) {
		loadValues(specie);
		if (!dimerizationAbstraction) {
			Species s = Utility.makeSpecies(specie.getId(), compartment, 0);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
		}

	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie);
		if (!biochemicalAbstraction) {
			Species s = Utility.makeSpecies(specie.getId(), compartment, 0);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
		}

	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie);
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		s.setBoundaryCondition(true);
		//s.setConstant(true);
		Utility.addSpecies(document, s);
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		Species s = Utility.makeSpecies(specie.getId(), compartment, init);
		s.setName(specie.getName());
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		
		org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		r.setId("Spastic_production_" + s.getName());
		
		r.addProduct(Utility.SpeciesReference(s.getName(), Double.parseDouble(parameters.getParameter(GlobalConstants.STOICHIOMETRY_STRING))));
		
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter("kp", Double.parseDouble(parameters
					.getParameter((GlobalConstants.OCR_STRING)))));	
		kl.setFormula("kp");
		Utility.addReaction(document, r);		
	}
	
	private void loadValues(SpeciesInterface specie) {
		init = specie.getInit();
	}

	
	private double init;
	private Collection<SpeciesInterface> species;
	private String compartment;

}
