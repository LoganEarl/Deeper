package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.skill.SkillTable;
import main.java.world.trait.Trait;

import java.util.Locale;

public class StatsCommand extends EntityCommand {
    private boolean complete = false;

    public StatsCommand(Client sourceClient, WorldModel model) {
        super(sourceClient, model);
    }

    @Override
    protected boolean requiresBalance() {
        return false;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        String message = String.format(Locale.US,
                "Name: %s\n" +
                        "Account:%s\n" +
                        "Race:%s\n" +
                        "Stats:%s\n" +
                        "Unspent IP:%d\n" +
                        "Total IP:%d\n" +
                        "Skills:\n%s" +
                        "Traits:\n%s" +
                        "Active Effects:\n%s",
                getSourceEntity().getDisplayName(),
                getSourceClient().getUserName(),
                getSourceEntity().getRace().getDisplayName(),
                getSourceEntity().getStats().toString(),
                getSourceEntity().getProgression().getIP(),
                getSourceEntity().getProgression().getTotalIP(),
                getSkillView(),
                getTraitView(),
                getEffectView()
        );
        getSourceClient().sendMessage(message);

        complete = true;
    }

    private String getSkillView() {
        StringBuilder skills = new StringBuilder();
        for(Skill s: SkillTable.getEntitySkills(getSourceEntity()))
            skills.append("\t").append(s.getDisplayName()).append("\n");

        return skills.toString();
    }

    private String getTraitView() {
        StringBuilder traits = new StringBuilder();
        for(Trait t: getSourceEntity().getTransitiveTraits())
            traits.append("\t").append(t.getDisplayableName()).append("\n");
        return traits.toString();
    }

    private String getEffectView() {
        return "\tNot Implemented";
    }
}
