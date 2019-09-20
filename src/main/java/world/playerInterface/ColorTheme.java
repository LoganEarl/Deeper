package main.java.world.playerInterface;

import main.java.world.WorldModel;
import main.java.world.diplomacy.DiplomacyManager;
import main.java.world.diplomacy.DiplomaticRelation;
import main.java.world.diplomacy.Faction;
import main.java.world.entity.Entity;
import main.java.world.item.Item;

import java.awt.*;
import java.util.Locale;

public class ColorTheme {
    public static final Color OUTGOING_DAMAGE = new Color(16,119,192);
    public static final Color INCOMING_DAMAGE = new Color(255,11,5);

    public static final Color ITEM = new Color(91,52,0);

    public static final Color SUCCESS = new Color(0,208,14);
    public static final Color WARNING = new Color(255,144,0);
    public static final Color FAILURE = new Color(255,7,0);
    public static final Color INFORMATIVE = new Color(1,103,176);

    public static final Color ALLY = new Color(0,72,5);
    public static final Color FRIENDLY = new Color(0,34,59);
    public static final Color NEUTRAL = new Color(69,69,69);
    public static final Color UNFRIENDLY = new Color(91,52,0);
    public static final Color ENEMY = new Color(91,2,0);

    public static final Color HP_COLOR = new Color(210,5,0);
    public static final Color MP_COLOR = new Color(0,79,135);
    public static final Color STAMINA_COLOR = new Color(0,165,11);
    public static final Color BURNOUT_COLOR = new Color(210,118,0);

    public static String getMessageInColor(String message, DiplomaticRelation relation){
        return getMessageInColor(message, getColorOfRelation(relation));
    }

    public static Color getColorOfRelation(DiplomaticRelation relation){
        switch (relation){
            case allied:
                return ALLY;
            case friendly:
                return FRIENDLY;
            case neutral:
                return NEUTRAL;
            case unfriendly:
                return UNFRIENDLY;
            case enemies:
                return ENEMY;
        }
        return NEUTRAL;
    }

    public static String getItemColored(Item item){
        return getMessageInColor(item.getDisplayableName(),ITEM);
    }

    public static String getEntityColored(Entity displayedEntity, Entity povEntity, WorldModel worldModel){
        return getEntityColored(displayedEntity, povEntity,worldModel.getDiplomacyManager());
    }

    public static String getEntityColored(Entity displayedEntity, Entity povEntity, DiplomacyManager diplomacyManager){
        Faction viewedFaction = displayedEntity.getDiplomacy().getFaction();
        DiplomaticRelation relation = diplomacyManager.getRelation(viewedFaction,povEntity.getDiplomacy().getFaction());
        return getMessageInColor(displayedEntity.getDisplayName() + " the " + displayedEntity.getRace().getDisplayName(),relation);
    }

    public static String getMessageInColor(String message, Color selectColor){
        return String.format(Locale.US, "<font color=\"%s\">%s</font>", getHexValue(selectColor), message);
    }

    public static String getHexValue(Color color){
        return String.format("#%02x%02x%02x",color.getRed(),color.getGreen(), color.getBlue());
    }
}
