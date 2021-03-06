/*******************************************************************************
 *
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;

/**
 * Base class for panels in iBioSim.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public abstract class PanelObservable extends JPanel implements BioObservable {

	private static final long serialVersionUID = -5440615594726032780L;
	protected BioObservable parent;
	private final List<BioObserver> listOfObservers;

	/**
	 * Constructs a new instance.
	 */
	public PanelObservable () {
		listOfObservers = new ArrayList<BioObserver>();
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param layout
	 *          - the layout of the panel.
	 */
	public PanelObservable (BorderLayout layout) {
		super(layout);
		listOfObservers = new ArrayList<BioObserver>();
	}

	@Override
	public void addObservable(BioObservable bioObservable) {
		parent = bioObservable;
	}

	@Override
	public void addObserver(BioObserver bioObserver) {
		this.listOfObservers.add(bioObserver);
	}

	@Override
	public void notifyObservers(Message message) {
		if (parent != null) {
			parent.notifyObservers(message);
		}
		for (BioObserver bioObserver : listOfObservers) {
			bioObserver.update(message);
		}
	}

	@Override
	public boolean request(RequestType type, Message message) {
		return false;
	}

	@Override
	public boolean send(RequestType type, Message message) {
		return false;
	}

}
