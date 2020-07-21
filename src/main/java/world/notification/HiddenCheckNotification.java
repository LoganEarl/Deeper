package main.java.world.notification;

import main.java.client.ClientRegistry;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.stat.StatValueContainer;

import java.util.Locale;

import static main.java.world.playerInterface.ColorTheme.SUCCESS;
import static main.java.world.playerInterface.ColorTheme.getMessageInColor;


@SuppressWarnings("unused")
public abstract class HiddenCheckNotification extends ConcreteNotification {
    private int viewerDifficultyBonus;
    private Skill viewerSkillToRoll;
    private int sourceDifficultyBonus;
    private Skill sourceSkillToRoll;
    private Entity source;
    private boolean isOpposed;

    public HiddenCheckNotification(Skill skillToRoll, int difficultyBonus, ClientRegistry registry) {
        super(registry);
        this.viewerSkillToRoll = skillToRoll;
        this.viewerDifficultyBonus = difficultyBonus;
        isOpposed = false;
    }

    public HiddenCheckNotification(int viewerDifficultyBonus, Skill viewerSkillToRoll, int sourceDifficultyBonus, Skill sourceSkillToRoll, Entity source, ClientRegistry registry) {
        super(registry);
        this.viewerDifficultyBonus = viewerDifficultyBonus;
        this.viewerSkillToRoll = viewerSkillToRoll;
        this.sourceDifficultyBonus = sourceDifficultyBonus;
        this.sourceSkillToRoll = sourceSkillToRoll;
        this.source = source;
        isOpposed = true;
    }

    @Override
    public final String getAsMessage(Entity viewer) {
        String message;
        int relativeSuccess;
        StatValueContainer viewerStats = viewer.getStats().getAugmentedValues();
        StatValueContainer sourceStats = source.getStats().getAugmentedValues();

        if (isOpposed) {
            if (viewer.equals(source)){
                message = "";
                relativeSuccess = 0;
            }
            else {
                int sourceRoll = source.getSkills().performSkillCheck(
                        sourceSkillToRoll, sourceDifficultyBonus,
                        sourceStats.getStat(sourceSkillToRoll.getAssociatedStat()),
                        source);
                int viewerRoll = viewer.getSkills().performSkillCheck(
                        viewerSkillToRoll, viewerDifficultyBonus,
                        viewerStats.getStat(viewerSkillToRoll.getAssociatedStat()),
                        source);
                relativeSuccess = viewerRoll - sourceRoll;
                if(relativeSuccess < 0)
                    message = "";
                else{
                    message = getMessageInColor(String.format(Locale.US, "You have passed a hidden (%s->%s) check(+%d)\n",
                            viewerSkillToRoll.getDisplayName(), sourceSkillToRoll.getDisplayName(), relativeSuccess), SUCCESS);
                }
            }
        } else {
            relativeSuccess = viewer.getSkills().performSkillCheck(
                    viewerSkillToRoll, viewerDifficultyBonus,
                    viewerStats.getStat(viewerSkillToRoll.getAssociatedStat()),
                    viewer);
            if(relativeSuccess < 0)
                message = "";
            else{
                message = getMessageInColor(String.format(Locale.US, "You have passed a hidden (%s) check(+%d)\n",
                        viewerSkillToRoll.getDisplayName(), relativeSuccess), SUCCESS);
            }

        }

        return message + getAsMessage(relativeSuccess,viewer);
    }

    protected abstract String getAsMessage(int relativeSuccess, Entity viewer);
}
