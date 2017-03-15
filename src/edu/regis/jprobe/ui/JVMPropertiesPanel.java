///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2007  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////
package edu.regis.jprobe.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import edu.regis.jprobe.jni.OSLibInfo;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivince
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class JVMPropertiesPanel extends PerformancePanel implements IPerformancePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea jvmOptions;
	private JTextArea classPath;
	private JTextArea javaProps;
	private JTextArea nativeLibs;
	private JTextArea envProps;
	private JTabbedPane tabPane;
	private JProbeClientFrame ui;
	private JPanel libsPanel = null;
	private DefaultTableModel libsModel;

	private static final String[] LIBS_COL_NAMES = { "Name", "Path", "Size", "File Date/Time", "Load Address" };

	public JVMPropertiesPanel(JProbeClientFrame u) {

		this.ui = u;
		Dimension hugeTextSize = new Dimension(250, 80);
		setLayout(new GridBagLayout());
		tabPane = new JTabbedPane(JTabbedPane.TOP);
		JPanel cp = new JPanel();
		cp.setLayout(new GridBagLayout());
		cp.setBorder(new EtchedBorder());
		GridBagConstraints cc = new GridBagConstraints();
		cc.insets = new Insets(1, 1, 1, 1);
		cc.fill = GridBagConstraints.BOTH;

		classPath = new JTextArea();
		classPath.setEditable(false);
		classPath.setCaretPosition(0);
		JScrollPane resultPane = new JScrollPane(classPath, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane.setPreferredSize(hugeTextSize);

		cc.gridx = 0;
		cc.gridy = 0;
		cc.gridwidth = 1;
		cc.gridheight = 1;
		cc.weightx = 1;
		cc.weighty = 1;

		cp.add(resultPane, cc);
		classPath.setText("Testing...");

		JPanel jp = new JPanel();
		jp.setLayout(new GridBagLayout());
		jp.setBorder(new EtchedBorder());
		GridBagConstraints cc2 = new GridBagConstraints();
		cc2.insets = new Insets(1, 1, 1, 1);
		cc2.fill = GridBagConstraints.BOTH;
		cc2.gridx = 0;
		cc2.gridy = 0;
		cc2.gridwidth = 1;
		cc2.gridheight = 1;
		cc2.weightx = 1;
		cc2.weighty = 1;

		javaProps = new JTextArea();
		javaProps.setEditable(false);
		javaProps.setCaretPosition(0);

		javaProps.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 3) {
					if (!ui.isPlaybackMode()) {
						promptProperty();
					}
				}

			}
		});
		JScrollPane resultPane2 = new JScrollPane(javaProps, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane2.setPreferredSize(hugeTextSize);
		jp.add(resultPane2, cc2);

		JPanel jo = new JPanel();
		jo.setLayout(new GridBagLayout());
		jo.setBorder(new EtchedBorder());
		GridBagConstraints ccjo = new GridBagConstraints();
		ccjo.insets = new Insets(1, 1, 1, 1);
		ccjo.fill = GridBagConstraints.BOTH;
		ccjo.gridx = 0;
		ccjo.gridy = 0;
		ccjo.gridwidth = 1;
		ccjo.gridheight = 1;
		ccjo.weightx = 1;
		ccjo.weighty = 1;
		jvmOptions = new JTextArea();
		jvmOptions.setEditable(false);
		jvmOptions.setCaretPosition(0);
		JScrollPane resultPane3 = new JScrollPane(jvmOptions, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane3.setPreferredSize(hugeTextSize);
		jo.add(resultPane3, ccjo);

		JPanel ep = new JPanel();
		ep.setLayout(new GridBagLayout());
		ep.setBorder(new EtchedBorder());
		GridBagConstraints ec = new GridBagConstraints();
		ec.insets = new Insets(1, 1, 1, 1);
		ec.fill = GridBagConstraints.BOTH;

		envProps = new JTextArea();
		envProps.setEditable(false);
		envProps.setCaretPosition(0);
		JScrollPane envPane = new JScrollPane(envProps, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		envPane.setPreferredSize(hugeTextSize);

		ec.gridx = 0;
		ec.gridy = 0;
		ec.gridwidth = 1;
		ec.gridheight = 1;
		ec.weightx = 1;
		ec.weighty = 1;

		ep.add(envPane, ec);

		GridBagConstraints cc3 = new GridBagConstraints();
		cc3.insets = new Insets(1, 1, 1, 1);
		cc3.fill = GridBagConstraints.BOTH;
		cc3.gridx = 0;
		cc3.gridy = 0;
		cc3.gridwidth = 1;
		cc3.gridheight = 1;
		cc3.weightx = 1;
		cc3.weighty = 1;

		tabPane.addTab("Java Properties", jp);
		tabPane.addTab("Environment", envPane);
		tabPane.addTab("Classpath", cp);
		tabPane.addTab("Java Startup Options", jo);

		add(tabPane, cc3);

	}

	public void update(ProbeResponse res) {

		if (res.isJvmOpts_updates()) {
			jvmOptions.setText(res.getJvmOpts());
			jvmOptions.setCaretPosition(0);
		}

		if (res.isClasspath_updated()) {
			classPath.setText(res.getClasspath());
			classPath.setCaretPosition(0);
		}

		if (res.isEnvProperties_updated()) {
			envProps.setText(res.getEnvProperties());
			envProps.setCaretPosition(0);
		}

		if (res.isJavaProperties_updated()) {
			javaProps.setText(res.getJavaProperties());
			javaProps.setCaretPosition(0);

		}

		if (res.isIoCountersAvailable()) {
			OSLibInfo libs[] = res.getNativeLibs();

			if (libs != null && libs.length > 0) {

				if (libsPanel == null) {
					libsPanel = createLibsPanel(libs);
					tabPane.addTab("Native Libs", libsPanel);
				} else {
					updateLibsPanel(libs);
				}
			}

		}
	}

	public void resetPanel() {
		jvmOptions.setText("");
		classPath.setText("");
		javaProps.setText("");
		nativeLibs.setText("");
		envProps.setText("");
	}

	protected void promptProperty() {

		final JPopupMenu queryPopup = new JPopupMenu("Popup Memu");

		JMenuItem popupSelect = new JMenuItem("Add or Modify a Property");
		popupSelect.setMnemonic('A');
		popupSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		popupSelect.setEnabled(true);
		queryPopup.add(popupSelect);

		popupSelect.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				alterProperty(0);
			}

		});

		JMenuItem popupInsert = new JMenuItem("Remove a Property");
		popupInsert.setMnemonic('R');
		popupInsert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		popupInsert.setEnabled(true);
		queryPopup.add(popupInsert);

		popupInsert.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				alterProperty(1);

			}

		});

		queryPopup.add(new JPopupMenu.Separator());

		JMenuItem popupCancel = new JMenuItem("Cancel");
		popupCancel.setMnemonic('C');
		popupCancel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		popupCancel.setEnabled(true);
		queryPopup.add(popupCancel);

		popupCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				queryPopup.setVisible(false);
			}

		});
		Point p = this.getMousePosition();
		queryPopup.setBorderPainted(true);
		queryPopup.show(this, p.x, p.y);

	}

	protected void alterProperty(int type) {
		String title = "Enter Property to ";
		String prompt = "";
		switch (type) {
		case 0:
			title += "Add or Modify";
			prompt = "Enter a Property in the form <property>=<value>";
			break;

		case 1:
			title += "Remove";
			prompt = "Enter a Property Key";
			break;
		}
		String opt = JOptionPane.showInputDialog(this, prompt, title, JOptionPane.OK_CANCEL_OPTION);
		String key = "";
		String value = "";
		if (opt != null) {
			if (type == 0) {
				StringTokenizer st = new StringTokenizer(opt, "=");

				int tokens = st.countTokens();
				String option[] = new String[tokens];
				int optIdx = 0;

				if (tokens != 2) {
					JOptionPane.showMessageDialog(this,
							"Invalid Format, must be in the format of " + "<property>=<value>", "Format",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				while (st.hasMoreTokens()) {
					option[optIdx++] = st.nextToken();
				}
				key = option[0];
				value = option[1];
				ui.setProperty(key, value);
			} else {
				key = opt;
				if (opt.trim().equals("")) {
					JOptionPane.showMessageDialog(this, "Property Key Cannot Be Blank ", "Format Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				ui.clearProperty(key);
			}

		} else {
			return;
		}

	}

	private JPanel createLibsPanel(OSLibInfo[] libs) {

		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(1, 1, 1, 1));
		p3.setBorder(new TitledBorder(new EtchedBorder(), "Modules"));

		String[][] data = loadLibData(libs);
		libsModel = new DefaultTableModel(data, LIBS_COL_NAMES);

		JTable tab = new JTable(libsModel);
		tab.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane sp = new JScrollPane(tab, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		p3.add(sp);
		return p3;
	}

	private void updateLibsPanel(OSLibInfo[] libs) {

		String[][] data = loadLibData(libs);

		libsModel.setDataVector(data, LIBS_COL_NAMES);
		libsModel.fireTableDataChanged();
	}

	private String[][] loadLibData(OSLibInfo[] libs) {

		String[][] data = new String[libs.length][LIBS_COL_NAMES.length];

		for (int i = 0; i < libs.length; i++) {
			OSLibInfo li = libs[i];
			data[i][0] = li.getName();
			data[i][1] = li.getPath();
			data[i][2] = Utilities.format(li.getSize());
			data[i][3] = li.getFileDate();
			data[i][4] = li.getLoadAddress();

		}
		return data;
	}
}
