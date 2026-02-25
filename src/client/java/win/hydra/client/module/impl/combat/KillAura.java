package win.hydra.client.module.impl.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.ModeSetting;
import win.hydra.client.module.setting.SliderSetting;
import win.hydra.client.util.time.StopWatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Simplified KillAura port for Hydra base.
 * Focuses nearest valid target in range and automatically attacks with basic FOV check.
 */
public class KillAura extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    private final ModeSetting type = new ModeSetting("Тип", "Плавная", "Резкая");
    private final SliderSetting attackRange = new SliderSetting("Дистанция атаки", 2.5, 6.0, 3.0, 0.1);
    private final SliderSetting fov = new SliderSetting("FOV", 10.0, 180.0, 90.0, 1.0);
    private final SliderSetting attackDelay = new SliderSetting("Задержка", 50.0, 500.0, 200.0, 5.0);

    private final BooleanSetting players = new BooleanSetting("Игроки", true);
    private final BooleanSetting mobs = new BooleanSetting("Мобы", false);
    private final BooleanSetting animals = new BooleanSetting("Животные", false);

    private final StopWatch stopWatch = new StopWatch();

    private LivingEntity target;
    private float currentYaw;
    private float currentPitch;
    private boolean hasRotations = false;
    private boolean reverting = false;
    private float revertYaw;
    private float revertPitch;

    private final SliderSetting yawSpeed = new SliderSetting("Скорость Yaw", 1.0, 30.0, 10.0, 0.5);
    private final SliderSetting pitchSpeed = new SliderSetting("Скорость Pitch", 1.0, 30.0, 8.0, 0.5);

    public KillAura() {
        super("KillAura", Category.COMBAT);
        addSetting(type);
        addSetting(attackRange);
        addSetting(fov);
        addSetting(attackDelay);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(yawSpeed);
        addSetting(pitchSpeed);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (!hasRotations) {
            currentYaw = mc.player.getYRot();
            currentPitch = mc.player.getXRot();
            hasRotations = true;
        }

        updateTarget();

        if (target == null) {
            if (reverting) {
                smoothRevert();
            }
            return;
        }

        smoothAimToTarget();

        reverting = true;

        if (!stopWatch.hasTimeElapsed((long) Math.round(attackDelay.get()))) return;

        float strength = mc.player.getAttackStrengthScale(0.0F);
        if (strength < 0.9F) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        stopWatch.reset();
    }

    private void smoothAimToTarget() {
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetVec = target.getEyePosition().subtract(eyePos);

        double yawToTarget = Math.toDegrees(Math.atan2(targetVec.z, targetVec.x)) - 90.0;
        double pitchToTarget = -Math.toDegrees(Math.atan2(targetVec.y,
                Math.sqrt(targetVec.x * targetVec.x + targetVec.z * targetVec.z)));

        float yawDelta = Mth.wrapDegrees((float) yawToTarget - currentYaw);
        float pitchDelta = (float) pitchToTarget - currentPitch;

        float maxYaw = yawSpeed.get().floatValue();
        float maxPitch = pitchSpeed.get().floatValue();

        float clampedYaw = Mth.clamp(yawDelta, -maxYaw, maxYaw);
        float clampedPitch = Mth.clamp(pitchDelta, -maxPitch, maxPitch);

        currentYaw = currentYaw + clampedYaw;
        currentPitch = Mth.clamp(currentPitch + clampedPitch, -89.0F, 89.0F);

        mc.player.setYRot(currentYaw);
        mc.player.setXRot(currentPitch);
        mc.player.yHeadRot = currentYaw;
        mc.player.yBodyRot = currentYaw;
    }

    private void smoothRevert() {
        float yawDelta = Mth.wrapDegrees(revertYaw - currentYaw);
        float pitchDelta = revertPitch - currentPitch;

        float maxYaw = yawSpeed.get().floatValue();
        float maxPitch = pitchSpeed.get().floatValue();

        float clampedYaw = Mth.clamp(yawDelta, -maxYaw, maxYaw);
        float clampedPitch = Mth.clamp(pitchDelta, -maxPitch, maxPitch);

        currentYaw += clampedYaw;
        currentPitch = Mth.clamp(currentPitch + clampedPitch, -89.0F, 89.0F);

        mc.player.setYRot(currentYaw);
        mc.player.setXRot(currentPitch);
        mc.player.yHeadRot = currentYaw;
        mc.player.yBodyRot = currentYaw;

        if (Math.abs(yawDelta) < 1.0F && Math.abs(pitchDelta) < 1.0F) {
            reverting = false;
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            currentYaw = mc.player.getYRot();
            currentPitch = mc.player.getXRot();
            revertYaw = currentYaw;
            revertPitch = currentPitch;
        }
        hasRotations = true;
        reverting = false;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        hasRotations = false;
        reverting = false;
        target = null;
    }

    private void updateTarget() {
        List<LivingEntity> list = new ArrayList<>();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (!isValid(living)) continue;
            if (!isWithinFov(living, fov.get())) continue;
            list.add(living);
        }

        if (list.isEmpty()) {
            target = null;
            return;
        }

        list.sort(Comparator.comparingDouble(a -> mc.player.distanceToSqr(a)));
        target = list.get(0);
    }

    private boolean isValid(LivingEntity entity) {
        if (entity == mc.player) return false;
        if (!entity.isAlive() || entity.isRemoved()) return false;
        if (entity instanceof ArmorStand) return false;

        double dist = Math.sqrt(mc.player.distanceToSqr(entity));
        if (dist > attackRange.get()) return false;

        if (entity instanceof Player && !players.get()) return false;
        if (entity instanceof Monster && !mobs.get()) return false;
        if (entity instanceof Animal && !animals.get()) return false;

        return true;
    }

    private boolean isWithinFov(LivingEntity entity, double maxFov) {
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetVec = entity.getEyePosition().subtract(eyePos);

        double yawToTarget = Math.toDegrees(Math.atan2(targetVec.z, targetVec.x)) - 90.0;
        double pitchToTarget = -Math.toDegrees(Math.atan2(targetVec.y, Math.sqrt(targetVec.x * targetVec.x + targetVec.z * targetVec.z)));

        float yawDelta = Mth.wrapDegrees((float) yawToTarget - mc.player.getYRot());
        float pitchDelta = Mth.wrapDegrees((float) pitchToTarget - mc.player.getXRot());

        double delta = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        return delta <= maxFov;
    }
}


