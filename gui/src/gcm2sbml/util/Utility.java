package gcm2sbml.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.*;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Species;

import sun.awt.im.InputMethodManager;

/**
 * This is a utility class. The constructor is private so that only one instance
 * of the class exists at any time.
 * 
 * @author Nam
 * 
 */
public class Utility {
	private Utility() {
	};

	public static final Utility getInstance() {
		if (instance == null) {
			instance = new Utility();
		}
		return instance;
	}

	/**
	 * Creates a copy of a double array
	 * 
	 * @param toCopy
	 *            the array to copy
	 * @return a copy of a double array
	 */
	public static double[] createCopy(double[] toCopy) {
		double[] copy = new double[toCopy.length];
		System.arraycopy(toCopy, 0, copy, 0, toCopy.length);
		return copy;
	}

	public static void print(boolean debug, String message) {
		if (debug) {
			System.out.println(message);
		}
	}

	public static String makeBindingReaction(String name,
			ArrayList<String> reactants, ArrayList<String> products) {
		return "";
	}

	public static Compartment makeCompartment(String id) {
		Compartment c = new Compartment("default");
		c.setConstant(true);
		c.setSpatialDimensions(3);
		return c;
	}

	public static void createErrorMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	public static Species makeSpecies(String id, String compartment,
			double amount) {
		Species specie = new Species(id, id);
		specie.setCompartment(compartment);
		specie.setInitialAmount(amount);
		return specie;
	}

	public static boolean isValid(String toValidate, String repExp) {
		Pattern pattern = Pattern.compile(repExp);
		Matcher matcher = pattern.matcher(toValidate);
		boolean state = matcher.find();
		if (state) {
			state = matcher.group().equals(toValidate);
		}
		return state;
	}

	public static/* Create add/remove/edit panel */
	JPanel createPanel(ActionListener listener, String panelName,
			JList panelJList, JButton addButton, JButton removeButton,
			JButton editButton) {
		JPanel Panel = new JPanel(new BorderLayout());
		JPanel addRem = new JPanel();
		if (addButton != null) {
			addButton.addActionListener(listener);
			addRem.add(addButton);
		}
		if (removeButton != null) {
			removeButton.addActionListener(listener);
			addRem.add(removeButton);
		}
		if (editButton != null) {
			addRem.add(editButton);
			editButton.addActionListener(listener);
		}

		JLabel panelLabel = new JLabel("List of " + panelName + ":");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(panelJList);

		if (listener instanceof MouseListener) {
			panelJList.addMouseListener((MouseListener) listener);
		}
		Panel.add(panelLabel, "North");
		Panel.add(scroll, "Center");
		Panel.add(addRem, "South");
		return Panel;
	}

	public static HashMap<String, double[]> calculateAverage(String folder) {
		final class TSDFilter implements java.io.FilenameFilter {
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.contains("tsd") && name.contains("run");
			}
		}
		HashMap<String, double[]> result = new HashMap<String, double[]>();
		HashMap<Integer, String> resultMap = new HashMap<Integer, String>();
		String allValues = "\\((.*)\\)";
		String oneValue = "\\(([^\\)\\(]+)\\)";
		Pattern onePattern = Pattern.compile(oneValue);
		String headerValue = "\"([^,\"]+)\"";
		Pattern speciesPattern = Pattern.compile(headerValue);
		String doubleValue = "[\\(,]*([\\d]+)[,\\)]";
		Pattern doublePattern = Pattern.compile(doubleValue);
		ArrayList<String> header = new ArrayList<String>();
		ArrayList<ArrayList<Double>> average = new ArrayList<ArrayList<Double>>();
		File allFiles = new File(folder);
		String[] files = allFiles.list(new TSDFilter());
		try {
			boolean headerFilled = false;
			for (int i = 0; i < files.length; i++) {
				String fileName = folder + File.separator + files[i];
				BufferedReader in = new BufferedReader(new FileReader(fileName));
				StringBuffer data = new StringBuffer();
				String str;
				while ((str = in.readLine()) != null) {
					data.append(str + "\n");
				}
				in.close();

				Pattern pattern = Pattern.compile(allValues);
				Matcher matcher = pattern.matcher(data.toString());
				matcher.find();
				String dataPoints = matcher.group(1);
				pattern = Pattern.compile(oneValue);
				matcher = pattern.matcher(dataPoints);
				// Setup and take care of headers
				int j = 0;
				while (matcher.find()) {
					Matcher speciesMatcher = speciesPattern.matcher(matcher
							.group(1));
					if (!headerFilled) {
						while (speciesMatcher.find()) {
							String t = speciesMatcher.group();
							result.put(t.replace("\"", ""), null);
							resultMap.put(Integer.valueOf(j), t.replace("\"", ""));
							j++;
						}
						int index = 0;
						int numGroups = 0;
						while (index != -1) {
							index = dataPoints.indexOf("(", index+1);
							numGroups++;
						}
						for (String s : result.keySet()) {							
							result.put(s, new double[numGroups-1]);
						}
					}
					headerFilled = true;
					break;
				}
				int k = 0;
				while (matcher.find()) {
					Matcher valueMatcher = doublePattern.matcher(matcher.group());
					// Now start reading in values
					j = 0;
					while (valueMatcher.find()) {
						String t = valueMatcher.group(1);
						result.get(resultMap.get(Integer.valueOf(j)))[k] = result
								.get(resultMap.get(Integer.valueOf(j)))[k]
								+ Double.parseDouble(t)/((double)files.length);
						j++;
					}
					k++;
				}
			}
//			for (String s : )
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static final Pattern IDpat = Pattern
			.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");
	public static final Pattern NUMpat = Pattern
			.compile("([\\d]*[\\.\\d]?\\d+)");
	private static Utility instance = null;
	public static final String NUMstring = "([\\d]*[\\.\\d]?\\d+)";
	public static final String PROPstring = "([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*";
	public static final String IDstring = "([a-zA-Z])([a-zA-Z]|[0-9]|_)*";

	public static final String DECAY = ".0075";
	public static final String KDIMER = ".5";
	public static final String DIMER = "1";
}
