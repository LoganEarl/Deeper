package world.diplomacy;

public class DiplomacyManager {
    /**
     * gets the sourceFaction's opinion of the targetFaction
     * @param sourceFaction the faction doing the viewing
     * @param targetFaction the faction being viewed
     * @return a diplomatic relationship
     */
    public DiplomaticRelation getRelation(Faction sourceFaction, Faction targetFaction){
        if(sourceFaction == null || targetFaction == null)
            return DiplomaticRelation.neutral;

        //TODO finish this
        return DiplomaticRelation.neutral;
    }
}
