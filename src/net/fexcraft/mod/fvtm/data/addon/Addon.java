package net.fexcraft.mod.fvtm.data.addon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputFilter.Config;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonObject;
import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.utils.ZipUtil;
import net.fexcraft.lib.mc.registry.FCLRegistry.AutoRegisterer;
import net.fexcraft.mod.fvtm.data.DecorationData;
import net.fexcraft.mod.fvtm.data.DirectPipe;
import net.fexcraft.mod.fvtm.data.TextureSupply;
import net.fexcraft.mod.fvtm.data.block.Block;
import net.fexcraft.mod.fvtm.data.block.CraftBlockScript;
import net.fexcraft.mod.fvtm.data.container.Container;
import net.fexcraft.mod.fvtm.data.container.ContainerType;
import net.fexcraft.mod.fvtm.data.part.Part;
import net.fexcraft.mod.fvtm.data.root.DataType;
import net.fexcraft.mod.fvtm.data.root.Registrable;
import net.fexcraft.mod.fvtm.data.vehicle.Vehicle;
import net.fexcraft.mod.fvtm.data.vehicle.VehicleData;
import net.fexcraft.mod.fvtm.sys.condition.Condition;
import net.fexcraft.mod.fvtm.sys.condition.ConditionRegistry;
import net.fexcraft.mod.fvtm.util.DataUtil;
import net.fexcraft.mod.fvtm.util.Resources;
import net.fexcraft.mod.uni.client.uCreativeTab;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Addon extends Registrable<Addon> {
	
	protected ArrayList<String> authors = new ArrayList<>();
	protected String version, url, license, update_id;
	protected boolean enabled = true, generatelang, generatejson, generateicon;
	protected File file, lang;
	//protected HashMap<String, ArmorMaterial> armats = new HashMap<>();
	protected LinkedHashMap<String, TextureSupply> supp_tex = new LinkedHashMap<>();
	protected PackContainerType contype;
	protected Location loc;
	//
	protected HashMap<String, uCreativeTab> creativetabs;
	
	public Addon(PackContainerType type, Location loc, File file){
		this.contype = type;
		this.file = file;
		this.loc = loc;
	}

	@Override
	public Addon parse(JsonMap map){
		id = DataUtil.getID(map);
		if(id == null) return null;
		pack = this;
		name = map.getString("Name", "Unnamed Addon");
		version = map.getString("Version", "0.o");
		if(map.has("Authors") && map.get("Authors").isArray()){
			map.get("Authors").asArray().elements().forEach(elm -> { this.authors.add(elm.string_value()); });
		}
		if(map.has("Author") && map.get("Author").isObject()){
			this.authors.add(map.get("Author").string_value());
		}
		url = map.getString("URL", "http://fexcraft.net/not_found");
		license = map.getString("License", "http://fexcraft.net/not_found");
		update_id = map.getString("UpdateID", "null");
		generatelang = map.getBoolean("GenerateLang", false);
		generatejson = map.getBoolean("GenerateItemJson", false);
		generateicon = map.getBoolean("GenerateItemIcon", false);
		//
		if(Static.isClient()){
			creativetabs = new HashMap<>();
			if(!map.has("CreativeTabs")){
				this.creativetabs.put(AddonTab.DEFAULT, new AddonTab(this, AddonTab.DEFAULT));
			}
			else{
				map.get("CreativeTabs").asArray().elements().forEach(elm -> {
					this.creativetabs.put(elm.string_value(), new AddonTab(this, elm.string_value()));
				});
			}
		}
		if(map.has("ClothMaterials")){
			map.get("ClothMaterials").asMap().entries().forEach(entry -> {
				JsonMap data = entry.getValue().asMap();
				int durr = data.getInteger("durability", 1);
				int[] ams = new int[4];
				if(data.has("damage_reduction")){
					JsonArray arr = data.getArray("damage_reduction");
					for(int i = 0; i < 4; i++) ams[i] = arr.get(i).integer_value();
				}
				float tgh = data.getFloat("toughness", 0);
				//TODO cloth materials | armats.put(entry.getKey(), EnumHelper.addArmorMaterial(entry.getKey(), Resources.NULL_TEXTURE.toString(), durr, ams, 0, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, tgh));
			});
		}
		if(map.has("SupplyTextures")){
			map.getMap("SupplyTextures").entries().forEach(entry -> {
				supp_tex.put(entry.getKey(), new TextureSupply(entry.getKey(), entry.getValue().asMap()));
			});
		}
		if(map.has("WireDecos")){
			Resources.WIRE_DECO_CACHE.put(id.id(), map.get("WireDecos").asMap());
		}
		if(map.has("Particles") && Static.isClient()){
			JsonMap par = map.get("Particles").asMap();
			for(Entry<String, JsonObject<?>> entry : par.entries()){
				//TODO particle system | new net.fexcraft.mod.fvtm.sys.particle.Particle(registryname.getPath() + ":" + entry.getKey(), JsonHandler.parse(entry.getValue().toString(), true).asMap());
			}
		}
		if(map.has("Conditions")){
			JsonMap conds = map.getMap("Conditions");
			for(Entry<String, JsonObject<?>> entry : conds.entries()){
				Condition cond = null;
				if(entry.getValue().isArray()){
					cond = new Condition(id.id() + ":" + entry.getKey(), entry.getValue().asArray());
				}
				else{
					cond = new Condition(id.id() + ":" + entry.getKey(), entry.getValue().asMap());
				}
				ConditionRegistry.register(cond);
			}
		}
		if(map.has("TrafficSigns")){
			/**JsonObject tsn = map.get("TrafficSigns").getAsJsonObject();
			TrafficSignLibrary.AddonLib lib = new TrafficSignLibrary.AddonLib(registryname.getPath());
			if(tsn.has("backgrounds")){
				for(Entry<String, JsonElement> elm : tsn.get("backgrounds").getAsJsonObject().entrySet()){
					lib.backgrounds.put(elm.getKey(), elm.getValue().getAsString());
				}
			}
			if(tsn.has("components")){
				for(Entry<String, JsonElement> elm : tsn.get("components").getAsJsonObject().entrySet()){
					lib.components.put(elm.getKey(), elm.getValue().getAsString());
				}
			}
			if(tsn.has("fonts")){
				for(Entry<String, JsonElement> elm : tsn.get("fonts").getAsJsonObject().entrySet()){
					lib.fonts.put(elm.getKey(), elm.getValue().getAsString());
				}
			}
			if(tsn.has("presets")){
				for(Entry<String, JsonElement> elm : tsn.get("presets").getAsJsonObject().entrySet()){
					lib.presets.put(elm.getKey(), elm.getValue().getAsJsonObject());
				}
			}
			TrafficSignLibrary.LIBRARIES.put(lib.id, lib);
			lib.load();*/
			//TODO traffic signs
		}
		if(map.has("Decorations")){
			JsonMap cats = map.get("Decorations").asMap();
			for(Entry<String, JsonObject<?>> entry : cats.entries()){
				String category = entry.getKey();
				JsonMap decos = entry.getValue().asMap();
				for(Entry<String, JsonObject<?>> entr : decos.entries()){
					String key = id.id() + ":" + entr.getKey();
					Resources.DECORATIONS.put(key, new DecorationData(key, category, entr.getValue()));
				}
				if(decos.size() > 0 && !Resources.DECORATION_CATEGORIES.contains(category)){
					Resources.DECORATION_CATEGORIES.add(category);
				}
			}
		}
		if(map.has("DirectPipes")){
			JsonMap pipes = map.getMap("DirectPipes");
			for(Entry<String, JsonObject<?>> entry : pipes.entries()){
				try{
					String pid = id.id() + ":" +  entry.getKey();
					Resources.DIRPIPES.put(pid, new DirectPipe(pid, entry.getValue()));
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return this;
	}

	@Override
	public DataType getDataType(){
		return DataType.ADDON;
	}

	@Override
	public Class<?> getDataClass(){
		return null;
	}
	
	public List<String> getAuthors(){
		return authors;
	}
	
	public File getFile(){
		return file;
	}
	
	public PackContainerType getPackConType(){
		return contype;
	}
	
	public String getVersion(){
		return version;
	}
	
	public String getURL(){
		return url;
	}
	
	public String getLicense(){
		return license;
	}
	
	public String getUpdateId(){
		return update_id;
	}
	
	public Addon setEnabled(boolean bool){
		enabled = bool;
		return this;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	/** For sending over network. */
	public JsonMap toJson(){
		JsonMap map = new JsonMap();
		map.add("ID", id.toString());
		map.add("Name", name);
		map.add("Version", version);
		map.add("URL", url);
		map.add("License", license);
		map.add("UpdateID", update_id);
		JsonArray array = new JsonArray();
		authors.forEach(elm -> array.add(elm));
		map.add("Authors", array);
		return map;
	}

	public void searchFor(DataType data) throws InstantiationException, IllegalAccessException {
		if(data == DataType.ADDON) return;
		if(!this.isEnabled()){
			Print.log("Skipping " + data.name() + " search for Addon '" + registryname.toString() + "' since it's marked as not enabled!");
			return;
		}
		if(contype == ContainerType.DIR){
			if(!file.isDirectory()) return;
			//
			File folder = new File(file, "assets/" + registryname.getPath() + "/config/" + data.cfg_folder + "/");
			if(!folder.exists()){ folder.mkdirs(); }
			ArrayList<File> candidates = findFiles(folder, data.suffix);
			for(File file : candidates){
				try{
					JsonObject obj = JsonUtil.get(file);
					TypeCore<?> core = (TypeCore<?>)data.core.newInstance().parse(obj);
					if(core == null){
						if(obj.has("RegistryName")) Print.log("Skipping " + data.name() + " '" + obj.get("RegistryName").getAsString() + "' due to errors.");
						continue;
					}
					data.register(core); //Print.log("Registered "+ data.name() +  " with ID '" + core.getRegistryName() + "' into FVTM.");
					if(Static.side().isClient()){
						checkIfHasCustomModel(data, core);
					}
					if(Static.dev()){
						if(generatelang) checkLangFile(core);
						if(generatejson) checkItemJson(core, data);
						if(generateicon) checkItemIcon(core, data);
					}
				}
				catch(Throwable t){
					t.printStackTrace();
					Print.log("Failed to load config from file '" + file + "'!"); Static.stop();
				}
			}
		}
		else{ //assume it's a jar.
			String lastentryname = null;
			try{
				String path = "assets/" + registryname.getPath() + "/config/" + data.cfg_folder + "/";
				ZipFile zip = new ZipFile(file);
				ZipInputStream stream = new ZipInputStream(new FileInputStream(file));
				while(true){
					ZipEntry entry = stream.getNextEntry();
					if(entry == null){
						break;
					}
					lastentryname = entry.getName();
					if(entry.getName().startsWith(path) && entry.getName().endsWith(data.suffix)){
						JsonObject obj = JsonUtil.getObjectFromInputStream(zip.getInputStream(entry));
						TypeCore<?> core = (TypeCore<?>)data.core.newInstance().parse(obj);
						if(core == null){
							if(obj.has("RegistryName")) Print.log("Skipping " + data.name() + " '" + obj.get("RegistryName").getAsString() + "' due to errors.");
							continue;
						}
						data.register(core); //Print.log("Registered " + data.name() + " with ID '" + core.getRegistryName() + "' into FVTM.");
						if(Static.side().isClient()){
							checkIfHasCustomModel(data, core);
						}
					}
				}
				zip.close();
				stream.close();
			}
			catch(Throwable e){
				e.printStackTrace();
				if(lastentryname != null) Print.log("Failed to load config from zip entry '" + lastentryname + "'!"); Static.stop();
			}
		}
	}

	private void checkIfHasCustomModel(DataType data, TypeCore<?> core){
		switch(data){
			case BLOCK:{
				Block block = (Block)core;
				if(!block.hasPlainModel() && Config.RENDER_BLOCK_MODELS_AS_ITEMS && !block.no3DItemModel()){
					net.fexcraft.lib.mc.render.FCLItemModelLoader.addItemModel(core.getRegistryName(), BlockModel.EMPTY);
					return;
				}
				break;
			}
			case CONTAINER:{
				Container con = (Container)core;
				if(!con.no3DItemModel()){
					net.fexcraft.lib.mc.render.FCLItemModelLoader.addItemModel(core.getRegistryName(), ContainerModel.EMPTY);
					return;
				}
				break;
			}
			case PART:{
				Part part = (Part)core;
				if(!part.no3DItemModel() && part.getDefaultFunctions().stream().filter(pre -> pre.getId().equals("fvtm:wheel")).count() > 0){
					net.fexcraft.lib.mc.render.FCLItemModelLoader.addItemModel(core.getRegistryName(), PartModel.EMPTY);
					return;
				}
				break;
			}
			case VEHICLE:{
				Vehicle veh = (Vehicle)core;
				if(Config.RENDER_VEHILE_MODELS_AS_ITEMS && !veh.no3DItemModel()){
					net.fexcraft.lib.mc.render.FCLItemModelLoader.addItemModel(core.getRegistryName(), VehicleModel.EMPTY);
					return;
				}
				break;
			}
			default: break;
		}
		if(loc.isFullLite() || isItemModelMissing(core)){
			net.fexcraft.lib.mc.render.FCLItemModelLoader.addItemModel(core.getRegistryName(), ItemPlaceholderModel.INSTANCE);
		}
	}

	private boolean isItemModelMissing(TypeCore<?> type){
		try{
			net.minecraft.client.resources.IResource res = net.minecraft.client.Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(type.getRegistryName().getNamespace(), "textures/items/" + type.getRegistryName().getPath() + ".png"));
			return res == null;
		}
		catch(IOException e){
			//e.printStackTrace();
			return true;
		}
	}

	private void checkLangFile(TypeCore<?> core){
		if(lang == null) lang = new File((loc.isLitePack() ? file : file.getParentFile()), (loc.isLitePack() ? "" : "/src/main/resources") + "/assets/" + registryname.getPath() + "/lang/en_us.lang");
		String regname = (core instanceof Block ? "tile." : "item.") + core.getRegistryName().toString() + ".name=";
		if(!containsLangEntry(regname)){
			try{
				Files.write(lang.toPath(), ("\n" + regname).getBytes(), StandardOpenOption.APPEND);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			Print.log("Added lang entry '" + regname.replace("=", "") + "'!");
		}
	}

	private void checkItemJson(TypeCore<?> core, DataType data){
		File json = new File((loc.isLitePack() ? file : file.getParentFile()), (loc.isLitePack() ? "" : "/src/main/resources") + "/assets/" + core.getRegistryName().getNamespace() + "/models/item/" + core.getRegistryName().getPath() + ".json");
		if(!json.exists()){
			if(!json.getParentFile().exists()) json.getParentFile().mkdirs();
			JsonObject obj = new JsonObject();
			obj.addProperty("parent", "item/generated");
			JsonObject textures = new JsonObject();
			textures.addProperty("layer0", core.getRegistryName().getNamespace() + ":items/" + core.getRegistryName().getPath());
			obj.add("textures", textures);
			obj.addProperty("__comment", "Autogenerated Item JSON via FVTM.");
			JsonUtil.write(json, obj);
			Print.log("Generated item json for '" + core.getRegistryName().toString() + "'!");
		}
		//TODO eventually an alternative model for blocks?
	}
	
	private static final String gitph = "https://raw.githubusercontent.com/Fexcraft/FVTM/1.12.2/placeholders/ph_%s.png";
	private static BufferedImage img, img_veh, img_part;

	private void checkItemIcon(TypeCore<?> core, DataType data){
		File icon = new File((loc.isLitePack() ? file : file.getParentFile()), (loc.isLitePack() ? "" : "/src/main/resources") + "/assets/" + core.getRegistryName().getNamespace() + "/textures/items/" + core.getRegistryName().getPath() + ".png");;
		if(!icon.exists()){
			if(!icon.getParentFile().exists()) icon.getParentFile().mkdirs();
			BufferedImage image = null;
			if(data == DataType.VEHICLE){
				if(img_veh == null){
					img_veh = DataUtil.tryDownload(String.format(gitph, "vehicle"));
				}
				image = img_veh;
			}
			else if(data == DataType.PART){
				if(img_part == null){
					img_part = DataUtil.tryDownload(String.format(gitph, "part"));
				}
				image = img_part;
			}
			else{
				if(img == null){
					img = DataUtil.tryDownload(String.format(gitph, "general"));
				}
				image = img;
			}
			try{
				ImageIO.write(image, "png", icon);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			Print.log("Generated item icon for '" + core.getRegistryName().toString() + "'!");
		}
	}

	private boolean containsLangEntry(String regname){
		try{
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(lang);
			while(scanner.hasNext()){
				if(scanner.nextLine().startsWith(regname)) return true;
			} scanner.close();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		return false;
	}

	public static ArrayList<File> findFiles(File file, String suffix){
		ArrayList<File> result = new ArrayList<>();
		if(file.isDirectory()){
			for(File sub : file.listFiles()){
				ArrayList<File> search = findFiles(sub, suffix);
				if(!search.isEmpty()) result.addAll(search);
			}
		}
		else if(file.getName().endsWith(suffix)) result.add(file);
		return result;
	}

	public AutoRegisterer getFCLRegisterer(){
		return registerer;
	}

	@SideOnly(Side.CLIENT)
	public CreativeTabs getDefaultCreativeTab(){
		if(creativetabs.size() == 0) return null;
		if(creativetabs.containsKey("default"))
			return creativetabs.get("default");
		else return creativetabs.values().toArray(new CreativeTabs[0])[0];
	}

	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTab(String id){
		if(creativetabs.containsKey(id))
			return creativetabs.get(id);
		else return getDefaultCreativeTab();
	}

	public void loadPresets(){
		if(!this.isEnabled()){
			Print.log("Skipping PRESET search for Addon '" + registryname.toString() + "' since it's marked as not enabled!");
			return;
		}
		if(contype == ContainerType.DIR){
			if(!file.isDirectory()) return;
			//
			File folder = new File(file, "assets/" + registryname.getPath() + "/config/presets/");
			if(!folder.exists()){
				folder.mkdirs();
			}
			ArrayList<File> candidates = findFiles(folder, ".json");
			for(File file : candidates){
				try{
					JsonObject obj = JsonUtil.get(file);
					if(obj.entrySet().isEmpty()) continue;
					Vehicle vehicle = Resources.VEHICLES.get(new ResourceLocation(obj.get("Vehicle").getAsString()));
					VehicleData data = (VehicleData)vehicle.getDataClass().getConstructor(Vehicle.class).newInstance(vehicle);
					data.read(JsonToNBT.getTagFromJson(obj.toString()));
					data.setPreset(JsonUtil.getIfExists(obj, "Preset", "Nameless"));
					PresetTab.INSTANCE.add(data.newItemStack());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		else{ // assume it's a jar.
			JsonArray array = ZipUtil.getJsonObjectsAt(file, "assets/" + registryname.getPath() + "/config/presets/", ".json");
			for(JsonElement elm : array){
				try{
					JsonObject obj = elm.getAsJsonObject();
					if(obj.entrySet().isEmpty()) continue;
					Vehicle vehicle = Resources.VEHICLES.get(obj.get("Vehicle").getAsString());
					VehicleData data = (VehicleData)vehicle.getDataClass().getConstructor(Vehicle.class).newInstance(vehicle);
					data.read(JsonToNBT.getTagFromJson(obj.toString()));
					data.setPreset(JsonUtil.getIfExists(obj, "Preset", "Nameless"));
					PresetTab.INSTANCE.add(data.newItemStack());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public void loadRecipes(){
		if(!this.isEnabled()){
			Print.log("Skipping RECIPE search for Addon '" + registryname.toString() + "' since it's marked as not enabled!");
			return;
		}
		if(contype == ContainerType.DIR){
			if(!file.isDirectory()) return;
			//
			File folder = new File(file, "assets/" + registryname.getPath() + "/config/recipes/");
			if(!folder.exists()){
				folder.mkdirs();
			}
			ArrayList<File> candidates = findFiles(folder, ".recipe");
			for(File file : candidates){
				try{
					CraftBlockScript.parseRecipes(this, file.getName(), new FileInputStream(file));
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		else{ // assume it's a jar.
			try{
				String path = "assets/" + registryname.getPath() + "/config/presets/", ext = ".recipe";
				ZipFile zip = new ZipFile(file);
				ZipInputStream stream = new ZipInputStream(new FileInputStream(file));
				while(true){
					ZipEntry entry = stream.getNextEntry();
					if(entry == null){
						break;
					}
					if(entry.getName().startsWith(path) && entry.getName().endsWith(ext)){
						CraftBlockScript.parseRecipes(this, "ZIPENTRY", zip.getInputStream(entry));
					}
				}
				zip.close();
				stream.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public HashMap<String, ArmorMaterial> getClothMaterials(){
		return armats;
	}
	
	public Location getLocation(){
		return loc;
	}

	public LinkedHashMap<String, TextureSupply> getTextureSuppliers(){
		return supp_tex;
	}
	
	public static enum Location {
		
		RESOURCEPACK, MODS, CONFIG, CODE;
		
	}
	
	public static enum PackContainerType {
		
		JAR, FOLDER, INTERNAL;
		
	}

}