package world.diplomacy;

public class DiplomacyManager {
    public DiplomaticRelation getRelation(Faction sourceFaction, Faction targetFaction){
        if(sourceFaction == null || targetFaction == null)
            return DiplomaticRelation.neutral;

        //TODO finish this
        return DiplomaticRelation.neutral;
    }
}
