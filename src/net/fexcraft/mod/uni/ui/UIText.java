package net.fexcraft.mod.uni.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.RGB;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class UIText extends UIElement {

	public static Class<? extends UIText> IMPLEMENTATION;
	//
	public String initial_value;
	public String value;
	public boolean shadow;
	public boolean translate;
	public float scale;
	public RGB color = new RGB();
	public RGB hover = new RGB();

	public UIText(UserInterface ui, JsonMap map) throws Exception {
		super(ui, map);
		initial_value = value = map.getString("value", "");
		scale = map.getFloat("scale", 1);
		if(map.getBoolean("autoscale", false)) scale = -1;
		shadow = map.getBoolean("shadow", false);
		color.packed = map.getInteger("color", 0xf0f0f0);
		if(map.getBoolean("hoverable", true)){
			hover.packed = map.getInteger("hover", 0xf4d742);
		}
		translate = map.getBoolean("translate", false);
		if(translate) translate();
	}

	public void translate(){}

	public void translate(Object... objects){}

	public boolean onscroll(int am, int x, int y){
		return false;
	}

}
