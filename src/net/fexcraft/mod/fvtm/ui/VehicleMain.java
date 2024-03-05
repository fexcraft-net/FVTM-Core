package net.fexcraft.mod.fvtm.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.mod.fvtm.FvtmRegistry;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class VehicleMain extends UserInterface {

	public VehicleMain(JsonMap map, ContainerInterface con) throws Exception {
		super(map, con);
	}

	@Override
	public boolean onAction(UIButton button, String id, int l, int t, int x, int y, int mb){
		switch(id){
			case "status":{
				break;
			}
			case "fuel":{
				break;
			}
			case "attributes":{
				break;
			}
			case "inventories":{
				break;
			}
			case "containers":{
				break;
			}
			case "connectors":{
				break;
			}
		}
		return false;
	}

	@Override
	public boolean onScroll(UIButton button, String id, int gl, int gt, int mx, int my, int am){
		return false;
	}

	@Override
	public void predraw(float ticks, int mx, int my){

	}

	@Override
	public void postdraw(float ticks, int mx, int my){

	}

	@Override
	public void scrollwheel(int am, int mx, int my){

	}

}
