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
import win.hydra.client.event.Render3DEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.ModeSetting;
import win.hydra.client.module.setting.SliderSetting;
import win.hydra.client.module.setting.ColorSetting;
import win.hydra.client.util.time.StopWatch;
import win.hydra.client.util.render.draw.Render3DUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Продвинутая KillAura с русскими настройками и плавными ротациями.
 */
public class KillAura extends Module {

    private static KillAura instance;

    public static KillAura getInstance() {
        return instance;
    }

    private final Minecraft mc = Minecraft.getInstance();

    // === Основные настройки ===
    private final ModeSetting aimMode = new ModeSetting("Наводка", "FunTime", "Legit Snap", "ReallyWorld", "HolyWorld", "SpookyTime");
    private final ModeSetting targetType = new ModeSetting("Тип цели", "Players", "Mobs", "Animals", "All");
    
    private final SliderSetting attackRange = new SliderSetting("Дистанция удара", 2.0, 6.0, 3.5, 0.1);
    private final SliderSetting lookRange = new SliderSetting("Доп. дистанция поиска", 0.0, 3.0, 1.5, 0.1);
    private final SliderSetting fov = new SliderSetting("FOV", 10.0, 180.0, 90.0, 1.0);
    
    private final SliderSetting attackDelay = new SliderSetting("Задержка атаки", 50.0, 500.0, 150.0, 5.0);
    private final SliderSetting hitChance = new SliderSetting("Шанс удара %", 1.0, 100.0, 100.0, 1.0);
    
    // === Настройки ротаций ===
    private final SliderSetting yawSpeed = new SliderSetting("Скорость Yaw", 1.0, 50.0, 20.0, 0.5);
    private final SliderSetting pitchSpeed = new SliderSetting("Скорость Pitch", 1.0, 50.0, 15.0, 0.5);
    private final ModeSetting rotationMode = new ModeSetting("Режим ротации", "Smooth", "Instant", "Legit");
    
    // === Настройки атаки ===
    private final BooleanSetting onlyCrits = new BooleanSetting("Только криты", true);
    private final BooleanSetting smartCrits = new BooleanSetting("Умные криты", true);
    private final BooleanSetting breakShield = new BooleanSetting("Ломать щит", true);
    private final BooleanSetting ignoreWalls = new BooleanSetting("Сквозь стены", false);
    private final BooleanSetting fakeLag = new BooleanSetting("Fake Lag", false);
    private final BooleanSetting resetSprint = new BooleanSetting("Сброс спринта", false);
    
    // === Визуальные настройки ===
    private final BooleanSetting renderTarget = new BooleanSetting("Рендер цели", true);
    private final ColorSetting targetColor = new ColorSetting("Цвет цели", 0xFFFF0000);

    private final StopWatch attackTimer = new StopWatch();
    private final StopWatch switchTimer = new StopWatch();

    private LivingEntity target;
    private LivingEntity lastTarget;
    
    private float currentYaw;
    private float currentPitch;
    private float revertYaw;
    private float revertPitch;
    
    private boolean hasRotations = false;
    private boolean reverting = false;
    private boolean shouldRotate = false;
    
    private int ticksSinceAttack = 0;
    private int consecutiveMisses = 0;

    public KillAura() {
        super("KillAura", Category.COMBAT);
        instance = this;
        addSetting(aimMode);
        addSetting(targetType);
        addSetting(attackRange);
        addSetting(lookRange);
        addSetting(fov);
        addSetting(attackDelay);
        addSetting(hitChance);
        addSetting(rotationMode);
        addSetting(yawSpeed);
        addSetting(pitchSpeed);
        addSetting(onlyCrits);
        addSetting(smartCrits);
        addSetting(breakShield);
        addSetting(ignoreWalls);
        addSetting(fakeLag);
        addSetting(resetSprint);
        addSetting(renderTarget);
        addSetting(targetColor);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        ticksSinceAttack++;

        // Инициализация текущих ротаций
        if (!hasRotations) {
            currentYaw = mc.player.getYRot();
            currentPitch = mc.player.getXRot();
            revertYaw = currentYaw;
            revertPitch = currentPitch;
            hasRotations = true;
        }

        // Обновление цели
        updateTarget();

        if (target == null) {
            // Возврат к исходному углу поворота
            if (reverting && rotationMode.get().equals("Smooth")) {
                smoothRevert();
            } else if (reverting && rotationMode.get().equals("Instant")) {
                instantRevert();
            }
            return;
        }

        // Ротация к цели
        shouldRotate = true;
        rotateToTarget();

        // Проверка возможности атаки
        if (!canAttack()) return;

        // Атака
        performAttack();
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !renderTarget.get() || target == null) return;
        
        Render3DUtil.drawEntityESP(target, targetColor.get(), 2.0F);
    }

    private boolean canAttack() {
        // Проверка задержки атаки
        if (!attackTimer.hasTimeElapsed((long) Math.round(attackDelay.get()))) {
            return false;
        }

        // Проверка силы атаки
        float strength = mc.player.getAttackStrengthScale(0.0F);
        if (strength < 0.9F && onlyCrits.get()) {
            return false;
        }

        // Проверка шанса удара
        if (Math.random() * 100 > hitChance.get()) {
            return false;
        }

        // Проверка критов (умные криты)
        if (onlyCrits.get() && smartCrits.get()) {
            if (!mc.player.onGround() && mc.player.getDeltaMovement().y <= 0) {
                // Падаем - будет крит
            } else if (mc.player.onGround()) {
                // На земле - не будет крита без прыжка
                if (smartCrits.get()) {
                    return false;
                }
            }
        }

        // Проверка дистанции
        if (mc.player.distanceTo(target) > attackRange.get()) {
            return false;
        }

        // Проверка видимости (если не ignoreWalls)
        if (!ignoreWalls.get() && !canSeeEntity(target)) {
            return false;
        }

        return true;
    }

    private void performAttack() {
        // Сброс спринта если нужно
        if (resetSprint.get() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }

        // Атака
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        // Сброс таймеров
        attackTimer.reset();
        ticksSinceAttack = 0;
        consecutiveMisses = 0;

        lastTarget = target;
    }

    private void rotateToTarget() {
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetVec = getRotationVector(target, eyePos);

        if (targetVec == null) return;

        double yawToTarget = Math.toDegrees(Math.atan2(targetVec.z, targetVec.x)) - 90.0;
        double pitchToTarget = -Math.toDegrees(Math.atan2(targetVec.y,
                Math.sqrt(targetVec.x * targetVec.x + targetVec.z * targetVec.z)));

        float targetYaw = Mth.wrapDegrees((float) yawToTarget);
        float targetPitch = Mth.wrapDegrees((float) pitchToTarget);

        switch (rotationMode.get()) {
            case "Smooth" -> {
                smoothRotation(targetYaw, targetPitch);
            }
            case "Instant" -> {
                instantRotation(targetYaw, targetPitch);
            }
            case "Legit" -> {
                legitRotation(targetYaw, targetPitch);
            }
        }

        // Применение ротаций
        applyRotations();
    }

    private void smoothRotation(float targetYaw, float targetPitch) {
        float yawDelta = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = targetPitch - currentPitch;

        float maxYaw = yawSpeed.get().floatValue();
        float maxPitch = pitchSpeed.get().floatValue();

        // Модификаторы для разных режимов наводки
        float speedMod = getAimModeSpeedModifier();
        maxYaw *= speedMod;
        maxPitch *= speedMod;

        float clampedYaw = Mth.clamp(yawDelta, -maxYaw, maxYaw);
        float clampedPitch = Mth.clamp(pitchDelta, -maxPitch, maxPitch);

        currentYaw = currentYaw + clampedYaw;
        currentPitch = Mth.clamp(currentPitch + clampedPitch, -89.0F, 89.0F);

        reverting = false;
    }

    private void instantRotation(float targetYaw, float targetPitch) {
        currentYaw = targetYaw;
        currentPitch = Mth.clamp(targetPitch, -89.0F, 89.0F);
        reverting = false;
    }

    private void legitRotation(float targetYaw, float targetPitch) {
        // Более медленная и естественная ротация
        float yawDelta = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = targetPitch - currentPitch;

        float maxYaw = yawSpeed.get().floatValue() * 0.5F;
        float maxPitch = pitchSpeed.get().floatValue() * 0.5F;

        // Случайные вариации для легитности
        float randomYaw = (float) (Math.random() * 2 - 1) * 3;
        float randomPitch = (float) (Math.random() * 2 - 1) * 2;

        float clampedYaw = Mth.clamp(yawDelta + randomYaw, -maxYaw, maxYaw);
        float clampedPitch = Mth.clamp(pitchDelta + randomPitch, -maxPitch, maxPitch);

        currentYaw = currentYaw + clampedYaw;
        currentPitch = Mth.clamp(currentPitch + clampedPitch, -89.0F, 89.0F);

        reverting = false;
    }

    private float getAimModeSpeedModifier() {
        switch (aimMode.get()) {
            case "FunTime": return 2.0F;
            case "HolyWorld": return 0.8F;
            case "Legit Snap": return 0.5F;
            case "ReallyWorld": return 1.5F;
            case "SpookyTime": return 1.2F;
            default: return 1.0F;
        }
    }

    private Vec3 getRotationVector(LivingEntity entity, Vec3 eyePos) {
        // Получение точки для ротации (голова, тело, ноги)
        Vec3 entityPos = entity.getEyePosition();
        return entityPos.subtract(eyePos);
    }

    private void applyRotations() {
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

        applyRotations();

        if (Math.abs(yawDelta) < 1.0F && Math.abs(pitchDelta) < 1.0F) {
            reverting = false;
        }
    }

    private void instantRevert() {
        currentYaw = revertYaw;
        currentPitch = revertPitch;
        applyRotations();
        reverting = false;
    }

    private void updateTarget() {
        List<LivingEntity> candidates = new ArrayList<>();
        double searchRange = attackRange.get() + lookRange.get();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (!isValidTarget(living, searchRange)) continue;
            
            if (!ignoreWalls.get() && !canSeeEntity(living)) continue;
            if (!isWithinFov(living)) continue;
            
            candidates.add(living);
        }

        if (candidates.isEmpty()) {
            target = null;
            return;
        }

        // Сортировка по дистанции и здоровью
        candidates.sort(Comparator
            .comparingDouble((LivingEntity e) -> mc.player.distanceToSqr(e))
            .thenComparingDouble(e -> e.getHealth()));

        LivingEntity newTarget = candidates.get(0);
        
        // Проверка на смену цели (anti-switch)
        if (target != null && target != newTarget) {
            if (switchTimer.hasTimeElapsed(500)) {
                target = newTarget;
                switchTimer.reset();
            }
        } else {
            target = newTarget;
        }
    }

    private boolean isValidTarget(LivingEntity entity, double range) {
        if (entity == mc.player) return false;
        if (!entity.isAlive() || entity.isRemoved()) return false;
        if (entity instanceof ArmorStand) return false;

        double dist = mc.player.distanceToSqr(entity);
        if (dist > range * range) return false;

        // Проверка типа цели
        String type = targetType.get();
        switch (type) {
            case "Players":
                return entity instanceof Player;
            case "Mobs":
                return entity instanceof Monster;
            case "Animals":
                return entity instanceof Animal;
            case "All":
                return true;
        }

        return false;
    }

    private boolean canSeeEntity(LivingEntity entity) {
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetPos = entity.getEyePosition();
        
        var clipResult = mc.level.clip(new net.minecraft.world.level.ClipContext(
            eyePos, targetPos,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE,
            mc.player
        ));
        
        return clipResult.getBlockPos().equals(entity.blockPosition()) || 
               mc.player.hasLineOfSight(entity);
    }

    private boolean isWithinFov(LivingEntity entity) {
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetVec = entity.getEyePosition().subtract(eyePos);

        double yawToTarget = Math.toDegrees(Math.atan2(targetVec.z, targetVec.x)) - 90.0;
        double pitchToTarget = -Math.toDegrees(Math.atan2(targetVec.y, 
                Math.sqrt(targetVec.x * targetVec.x + targetVec.z * targetVec.z)));

        float yawDelta = Mth.wrapDegrees((float) yawToTarget - mc.player.getYRot());
        float pitchDelta = Mth.wrapDegrees((float) pitchToTarget - mc.player.getXRot());

        double delta = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        return delta <= fov.get();
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
        target = null;
        attackTimer.reset();
        switchTimer.reset();
        ticksSinceAttack = 0;
        consecutiveMisses = 0;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        hasRotations = false;
        reverting = false;
        shouldRotate = false;
        target = null;
        lastTarget = null;
    }

    // Геттеры для визуализации
    public LivingEntity getTarget() {
        return target;
    }

    public LivingEntity getLastTarget() {
        return lastTarget;
    }

    public boolean isTargetColorEnabled() {
        return renderTarget.get();
    }

    public int getTargetColor() {
        return targetColor.get();
    }
}
