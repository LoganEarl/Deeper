package world.playerInterface.commands;

import client.Client;
import world.WorldModel;
import world.entity.skill.Skill;
import world.entity.skill.SkillTable;
import static world.playerInterface.ColorTheme.*;

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
        if (skill.isVisibleToEntity(getSourceEntity()) || SkillTable.entityHasSkill(getSourceEntity(),skill)) {
            existingMessage += spacing;
            if(SkillTable.entityHasSkill(getSourceEntity(),skill))
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
