package main.java.world.playerInterface;

import main.java.world.WorldModel;
import main.java.world.diplomacy.DiplomaticRelation;
import main.java.world.diplomacy.Faction;
import main.java.world.entity.Entity;

import java.awt.*;

public class MessageSubstitutor {
    public static final String NAME = "NAME";
    public static final String PRONOUN = "PRONOUN";
    public static final String REFLEXIVE = "REFLEXIVE";
    public static final String POSSESSIVE = "POSSESSIVE";

    public static String insertEntities(Entity viewer, String message, WorldModel model, Entity... substitutions) {
        for (int i = 0; i < substitutions.length; i++) {
            message = message.replaceAll(getEntityTag(i, NAME), getTransformedForm(viewer, substitutions[i], NAME, model));
            message = message.replaceAll(getEntityTag(i, PRONOUN), getTransformedForm(viewer, substitutions[i], PRONOUN, model));
            message = message.replaceAll(getEntityTag(i, REFLEXIVE), getTransformedForm(viewer, substitutions[i], REFLEXIVE, model));
            message = message.replaceAll(getEntityTag(i, POSSESSIVE), getTransformedForm(viewer, substitutions[i], POSSESSIVE, model));
        }
        return message;
    }

    private static String getTransformedForm(Entity viewer, Entity toView, String form, WorldModel model) {
        String result;
        if (viewer.equals(toView)) {
            if (form.equals(REFLEXIVE)) result = "yourself";
            else if (form.equals(POSSESSIVE)) result = "your";
            else result = "you";
        } else {
            Faction viewedFaction = toView.getDiplomacy().getFaction();
            DiplomaticRelation relation = model.getDiplomacyManager().getRelation(viewedFaction,viewer.getDiplomacy().getFaction());
            Color messageColor = ColorTheme.getColorOfRelation(relation);

            if (form.equals(REFLEXIVE)) result = ColorTheme.getMessageInColor(toView.getReflexivePronoun(),messageColor);
            else if (form.equals(POSSESSIVE)) result = ColorTheme.getMessageInColor(toView.getPossessivePronoun(),messageColor);
            else if(form.equals(PRONOUN)) result = ColorTheme.getMessageInColor(toView.getPronoun(),messageColor);
            else result = ColorTheme.getEntityColored(toView, viewer,model);
        }
        return result;
    }

    private static String getEntityTag(int entityNumber, String tag) {
        return "ENTITY" + entityNumber + tag;
    }
}
