package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;

public class PrintDecaySpeciesVisitor extends AbstractPrintVisitor {

	public PrintDecaySpeciesVisitor(SBMLDocument document,
			Collection<SpeciesInterface> species) {
		super(document);
		this.species = species;
		addDecayUnit();
	}
	
	private void addDecayUnit() {
		decayUnitString = GeneticNetwork.getMoleTimeParameter(1);
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

	@Override
	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitDimer(DimerSpecies specie) {
		//do nothing, dimer species decay much more slowly than their
		//constituent species
	}

	@Override
	public void visitBiochemical(BiochemicalSpecies specie) {
		//do nothing, biochemical species decay much more slowly than their
		// constituent species
	}

	@Override
	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie);
		Reaction r = Utility.Reaction("Degradation_"+specie.getId());
		r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(decayString, decay, decayUnitString));
		kl.setFormula(decayString + "*" + specie.getId());
		Utility.addReaction(document, r);
	}

	@Override
	public void visitConstantSpecies(ConstantSpecies specie) {
		//do nothing, constant species can't decay
	}

	@Override
	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie);
		Reaction r = Utility.Reaction("Degradation_"+specie.getId());
		r.addReactant(Utility.SpeciesReference(specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = r.createKineticLaw();
		kl.addParameter(Utility.Parameter(decayString, decay, decayUnitString));
		kl.setFormula(decayString + "*" + specie.getId());
		Utility.addReaction(document, r);
	}
	
	private void loadValues(SpeciesInterface specie) {
		decay = specie.getDecay();
	}
	
	private double decay;
	private String decayUnitString;
	private String decayString = CompatibilityFixer.getSBMLName(GlobalConstants.KDECAY_STRING);
	
	private Collection<SpeciesInterface> species;
	

}
