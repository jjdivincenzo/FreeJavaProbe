package edu.regis.jprobe.ui.themes;

import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;


public class ThemeManager {
	
	private static DefaultMetalTheme themes[] = {
		new AquaMetalTheme(),
		new BigContrastMetalTheme(),
		new ContrastMetalTheme(),
		new DemoMetalTheme(),
		new GreenMetalTheme(),
		new KhakiMetalTheme(),
		new CharcoalMetalTheme()
		 };
	
	public ThemeManager() {
		
		for (int i = 0; i < themes.length; i++) {
			UIManager.LookAndFeelInfo lafinfo = 
				new UIManager.LookAndFeelInfo(themes[i].getName(),themes[i].getClass().getName());
			UIManager.installLookAndFeel(lafinfo);
		}
	}
	
	public String getThemeName(int idx) {
		
		if (idx < 0 || idx >= themes.length) {
			throw new ArrayIndexOutOfBoundsException("Theme Index Is Out Of Range"); 
		}
		
		return themes[idx].getName();
	}
	
	public MetalTheme getTheme(String name) {
		
		MetalTheme selectedTheme = null; 
		
		for (int i = 0; i < themes.length; i++ ) {
	    	if (themes[i].getName().equals(name)) {
	    		selectedTheme = themes[i];
	    	}
	    }
		
		return selectedTheme;
	}
}
