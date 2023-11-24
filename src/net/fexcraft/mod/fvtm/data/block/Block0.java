package net.fexcraft.mod.fvtm.data.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fvtm.data.Content;
import net.fexcraft.mod.fvtm.data.ContentType;
import net.fexcraft.mod.fvtm.data.root.*;
import net.fexcraft.mod.fvtm.data.root.Colorable.ColorHolder;
import net.fexcraft.mod.fvtm.data.root.Soundable.SoundHolder;
import net.fexcraft.mod.fvtm.data.root.Textureable.TextureHolder;
import net.fexcraft.mod.fvtm.model.ModelData;
import net.fexcraft.mod.fvtm.util.ContentConfigUtil;
import net.fexcraft.mod.fvtm.util.Resources;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Block0 extends Content<Block0> implements TextureHolder, ColorHolder, SoundHolder, WithItem, ItemTextureable {

    protected List<IDL> textures;
    protected ArrayList<BlockFunction> functions = new ArrayList<>();
    protected Map<String, RGB> channels = new LinkedHashMap<>();
    protected Map<String, Sound> sounds = new LinkedHashMap<>();
    protected Map<String, AABB[]> aabbs = new LinkedHashMap<>();
    protected ModelData modeldata;
    protected BlockType blocktype;
    protected Object relaydata;
    protected boolean plain_model;
    protected boolean no3ditem;
    protected boolean weblike;
    protected boolean fullblock;
    protected boolean fullcube;
    protected boolean opaque;
    protected boolean cutout;
    protected boolean translucent;
    protected boolean invisible;
    protected boolean hideitem;
    protected boolean randomrot;
    protected boolean ladder;
    protected boolean tickable;
    protected boolean hastile;
    protected String modelid;
    protected String ctab;
    protected String oredict;
    protected String material;
    protected String mapcolor;
    protected String harverest_class;
    protected float hardness;
    protected float lightlevel;
    protected float resistance;
    protected float lightopacity;
    protected float damage;
    protected int harverest_level;
    protected int maxstacksize;
    protected int burntime;
    protected IDL itemtexloc;
    protected Object block;

    @Override
    public Block0 parse(JsonMap map){
        if((pack = ContentConfigUtil.getAddon(map)) == null) return null;
        if((id = ContentConfigUtil.getID(pack, map)) == null) return null;
        //
        name = map.getString("Name", "Unnamed Block");
        description = ContentConfigUtil.getStringList(map, "Description");
        textures = ContentConfigUtil.getTextures(map);
        if(map.has("ColorChannels")){
            for(Map.Entry<String, JsonValue<?>> entry : map.get("ColorChannels").asMap().entries()){
                channels.put(entry.getKey(), new RGB(entry.getValue().string_value()));
            }
        }
        if(channels.isEmpty()){
            channels.put("primary", RGB.WHITE.copy());
            channels.put("secondary", RGB.WHITE.copy());
        }
        maxstacksize = map.getInteger("MaxItemStackSize", 64);
        if(maxstacksize < 1) maxstacksize = 1;
        if(maxstacksize > 64) maxstacksize = 64;
        burntime = map.getInteger("ItemBurnTime", 0);
        oredict = map.getString("OreDictionary", null);
        modelid = map.getString("Model", null);
        if(modelid == null || modelid.equals("null") || modelid.startsWith("baked|")) plain_model = true;
        if(EnvInfo.CLIENT){
            modeldata = ContentConfigUtil.getModelData(map, "ModelData", new ModelData());
        }
        if(map.has("AABBs")){
            map.getMap("AABBs").entries().forEach(entry -> {
                JsonArray value = entry.getValue().asArray();
                if(value.get(0).isArray()){
                    ArrayList<AABB> list = new ArrayList<>();
                    for(JsonValue<?> elm : value.value){
                        JsonArray arr = elm.asArray();
                        list.add(new AABB(arr.get(0).float_value(), arr.get(1).float_value(), arr.get(2).float_value(),
                            arr.get(3).float_value(), arr.get(4).float_value(), arr.get(5).float_value()));
                    }
                    aabbs.put(entry.getKey(), list.toArray(new AABB[0]));
                }
                else{
                    JsonArray array = entry.getValue().asArray();
                    ArrayList<AABB> list = new ArrayList<>();
                    if(entry.getKey().startsWith("collision") && array.get(0).string_value().equals("null")){
                        aabbs.put(entry.getKey(), AABB.NULL);
                    }
                    else{
                        aabbs.put(entry.getKey(), new AABB[]{
                            new AABB(array.get(0).float_value(), array.get(1).float_value(), array.get(2).float_value(),
                                array.get(3).float_value(), array.get(4).float_value(), array.get(5).float_value())
                        });
                    }
                }
            });
        }
        if(map.has("Sounds")){
            for(Map.Entry<String, JsonValue<?>> entry : map.getMap("Sounds").entries()){
                if(entry.getValue().isMap()){
                    JsonMap val = entry.getValue().asMap();
                    sounds.put(entry.getKey(), new Sound(IDLManager.getIDLCached(val.getString("sound", "minecraft:block.lever.click")), val.getFloat("volume", 1f), val.getFloat("pitch", 1f)));
                }
                else{
                    sounds.put(entry.getKey(), new Sound(IDLManager.getIDLCached(entry.getValue().string_value()), 1f, 1f));
                }
            }
        }
        blocktype = BlockType.valueOf(map.getString("BlockType", "GENERIC_SIMPLE"));
        material = map.getString("Material", "ROCK");
        mapcolor = map.getString("MapColor", "STONE");
        hardness = map.getFloat("Hardness", 1f);
        lightlevel = map.getFloat("LightLevel", 0f);
        resistance = map.getFloat("Resistance", 0f);
        lightopacity = map.getFloat("LightOpacity", 0f);
        if(map.has("HarverestTool")){
            JsonArray array = map.getArray("HarverestTool");
            harverest_class = array.get(0).string_value();
            harverest_level = array.get(1).integer_value();
        }
        damage = map.getFloat("CollisionDamage", 0);
        weblike = map.getBoolean("WebLike", false);
        fullblock = map.getBoolean("FullBlock", true);
        fullcube = map.getBoolean("FullCube", true);
        opaque = map.getBoolean("Opaque", false);
        cutout = map.getBoolean("RenderCutout", false);
        translucent = map.getBoolean("RenderTranslucent", false);
        invisible = map.getBoolean("Invisible", false);
        hideitem = map.getBoolean("HideItem", false);
        randomrot = map.getBoolean("RandomRotation", false);
        ladder = map.getBoolean("Ladder", false);
        tickable = map.getBoolean("Tickable", false);
        hastile = map.getBoolean("MultiSubBlock", false);
        hastile = map.getBoolean("HasBlockEntity", hastile);
        if(map.has("Function")){
            parseFunction(map.get("Function"));
        }
        else if(map.has("Functions")){
            map.getArray("Functions").value.forEach(elm -> {
                parseFunction(elm);
            });
        }
        //wirerelays
        //
        ctab = map.getString("CreativeTab", "default");
        itemtexloc = ContentConfigUtil.getItemTexture(id, getContentType(), map);
        no3ditem = map.getBoolean("Disable3DItemModel", false);
        //
        try{
            block = BlockType.BLOCK_IMPL.get(blocktype, hastile || relaydata != null, plain_model).getConstructor(Block0.class).newInstance(this);
        }
        catch(Throwable e){
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public ContentType getContentType(){
        return ContentType.BLOCK;
    }

    @Override
    public Class<?> getDataClass(){
        return BlockData0.class;
    }

    @Override
    public List<IDL> getDefaultTextures(){
        return textures;
    }

    @Override
    public Map<String, Sound> getSounds(){
        return sounds;
    }

    @Override
    public String getItemContainer(){
        return null;
    }

    @Override
    public String getCreativeTab(){
        return ctab;
    }

    @Override
    public IDL getItemTexture(){
        return itemtexloc;
    }

    @Override
    public boolean noCustomItemModel(){
        return no3ditem;
    }

    @Override
    public RGB getDefaultColorChannel(String channel){
        return channels.get(channel);
    }

    @Override
    public Map<String, RGB> getDefaultColorChannels(){
        return channels;
    }

    private void parseFunction(JsonValue elm) {
        try {
            if(!elm.isMap()){
                functions.add(Resources.getBlockFunction(elm.string_value()).newInstance().parse(null));
            }
            else{
                JsonMap obj = elm.asMap();
                functions.add(Resources.getBlockFunction(obj.get("type").string_value()).newInstance().parse(obj));
            }
        }
        catch (Exception e){
            Print.log("Failed to load BlockFunction for '" + id.colon() + "' with JSON: " + elm);
            e.printStackTrace();
            Static.stop();
        }
    }

}
