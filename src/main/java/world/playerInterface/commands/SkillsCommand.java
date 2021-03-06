package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.world.WorldModel;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.skill.SkillContainer;
import static main.java.world.playerInterface.ColorTheme.*;

public class SkillsCommand extends EntityCommand {
    private boolean complete = false;

    public SkillsCommand(Client sourceClient, WorldModel model) {
        super(sourceClient, model);
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void executeEntityCommand() {
        String skillMessage = "Skills\n";
        for (Skill skill : Skill.values()) {
            if (skill.getRequiredSkills().length == 0)
                skillMessage = displaySkill(skill, 1, skillMessage) + "\n";
        }

        getSourceClient().sendMessage(skillMessage);

        complete = true;
    }

    private String displaySkill(Skill skill, int indentNum, String existingMessage) {
        StringBuilder spacing = new StringBuilder();
        for (int i = 0; i < indentNum; i++)
            spacing.append("  ");
        if (skill.isVisibleToEntity(getSourceEntity()) || getSourceEntity().getSkills().getLearnLevel(skill) != SkillContainer.UNLEARNED) {
            existingMessage += spacing;
            if(getSourceEntity().getSkills().getLearnLevel(skill) >= skill.getElevationLevel())
                existingMessage += getMessageInColor(skill.getDisplayName(),SUCCESS);
            else if(skill.isLearnableByEntity(getSourceEntity()))
                existingMessage += getMessageInColor(skill.getDisplayName(),INFORMATIVE);
            else
                existingMessage += getMessageInColor(skill.getDisplayName(),FAILURE);
        } else {
            if(skill.isLearnableByEntity(getSourceEntity()))
                existingMessage += spacing + getMessageInColor("Unknown",INFORMATIVE);
            else
                existingMessage += spacing + getMessageInColor("Unknown", FAILURE);
        }
        for (Skill s : Skill.getSkillsThatRequire(skill))
            existingMessage = displaySkill(s, indentNum + 1, existingMessage + "\n");

        return existingMessage;
    }

    @Override
    protected int getRequiredStamina() {
        return 0;
    }

    @Override
    protected int getStaminaUsed() {
        return 0;
    }

    @Override
    protected boolean canDoWhenDying() {
        return true;
    }

    @Override
    protected boolean requiresBalance() {
        return false;
    }

}
