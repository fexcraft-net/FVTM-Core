package net.fexcraft.mod.uni.ui;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public abstract class UserInterface {

	public static Class<UserInterface> IMPLEMENTATION;
	//
	public InterfaceContainer container;
	public LinkedHashMap<String, UIText> texts = new LinkedHashMap<>();
	public LinkedHashMap<String, UIButton> buttons = new LinkedHashMap<>();
	public LinkedHashMap<String, UIField> fields = new LinkedHashMap<>();
	public LinkedHashMap<String, UITab> tabs = new LinkedHashMap<>();

	public UserInterface(JsonMap map, InterfaceContainer container) throws Exception {
		this.container = container;
		if(map.has("texts")){
			for(Entry<String, JsonValue<?>> entry : map.getMap("texts").entries()){
				texts.put(entry.getKey(), UIText.IMPLEMENTATION.getConstructor(UserInterface.class, JsonMap.class).newInstance(entry.getValue()));
			}
		}
		if(map.has("buttons")){
			for(Entry<String, JsonValue<?>> entry : map.getMap("buttons").entries()){
				buttons.put(entry.getKey(), UIButton.IMPLEMENTATION.getConstructor(UserInterface.class, JsonMap.class).newInstance(entry.getValue()));
			}
		}
		if(map.has("fields")){
			for(Entry<String, JsonValue<?>> entry : map.getMap("fields").entries()){
				fields.put(entry.getKey(), UIField.IMPLEMENTATION.getConstructor(UserInterface.class, JsonMap.class).newInstance(entry.getValue()));
			}
		}
		if(map.has("tabs")){
			for(Entry<String, JsonValue<?>> entry : map.getMap("tabs").entries()){
				tabs.put(entry.getKey(), UITab.IMPLEMENTATION.getConstructor(UserInterface.class, JsonMap.class).newInstance(entry.getValue()));
			}
		}
		else{
			UITab main = UITab.IMPLEMENTATION.getConstructor(UserInterface.class, JsonMap.class).newInstance(map);
			main.texts.addAll(texts.values());
			main.buttons.addAll(buttons.values());
			main.fields.addAll(fields.values());
			tabs.put("main", main);
		}
	}

	public boolean onClick(int mx, int my, int mb){
		UIButton button = null;
		for(Entry<String, UIButton> entry : buttons.entrySet()){
			button = entry.getValue();
			if(!button.visible || !button.enabled) continue;
			if(button.hovered(mx, my)){
				return processAction(button, entry.getKey(), mx, my, mb);
			}
		}
		return false;
	}

	public abstract boolean processAction(UIButton button, String id, int x, int y, int b);

	public abstract void bindTexture(String texture);

}