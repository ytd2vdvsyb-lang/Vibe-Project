#!/usr/bin/env bash
# Ищет в jar Minecraft нужные классы и методы: печатает только совпадения, чтобы лог был коротким.
set -u
mc=$(grep -E '^minecraft_version=' "$(dirname "$0")/../gradle.properties" | cut -d= -f2)
jar=$(find ~/.gradle/caches/fabric-loom/minecraftMaven -name "minecraft-merged-deobf-$mc.jar" 2>/dev/null | head -1)
[ -n "$jar" ] || jar=$(find ~/.gradle/caches/fabric-loom/"$mc" -name 'minecraft-merged.jar' 2>/dev/null | head -1)
[ -n "$jar" ] || exit 0
echo "### all minecraft jars"
for j in $(find ~/.gradle/caches -name 'minecraft*.jar' 2>/dev/null | grep -v sources); do
  echo "$j"
  javap -p -cp "$j" net.minecraft.world.entity.LivingEntity 2>/dev/null | grep -E ' swing\(' | sed 's/^/    /'
done
echo "using: $jar"
echo "### classes"
unzip -Z1 "$jar" | grep -E '\.class$' | grep -v '\$' | sed 's/\.class$//; s#/#.#g' \
  | grep -E 'SwingAnimation|SetEntityMotion|Explode|MovePlayer|PlayerCommand|PlayerAbilities|ClientPacketListener|LevelRenderer|ShapeRenderer|Gizmo|DataComponents$|MultiPlayerGameMode|ClientCommonPacketListener|EntityRenderDispatcher|OutlineBufferSource' 
p() { # класс, регулярка
  echo "### $1 ~ $2"
  javap -p -cp "$jar" "$1" 2>&1 | grep -vE 'lambda\$' | grep -E "$2" | head -60
}
p net.minecraft.world.entity.LivingEntity 'swing|hurtTime|FallFlying|jumpFromGround|getSpeed|setSpeed'
p net.minecraft.world.entity.Entity 'DeltaMovement|onGround|OnGround|fallDistance|Glowing|setPos\(|getX\(|horizontalCollision|isInWater|setSprinting|noPhysics|maxUpStep|getBoundingBox'
p net.minecraft.client.player.LocalPlayer 'Packet|connection|input|aiStep|sendPosition|tick\('
p net.minecraft.world.entity.player.Player 'Abilities|getAbilities|attackStrength|AttackStrength|isCreative|crit'
p net.minecraft.world.entity.player.Abilities '.'
p net.minecraft.client.multiplayer.ClientPacketListener 'handleSetEntityMotion|handleExplosion|handleExplode|send\(|getConnection'
p net.minecraft.client.multiplayer.MultiPlayerGameMode 'attack|useItem|handleContainerInput|ensureHasSentCarriedItem'
p net.minecraft.client.Minecraft 'shouldEntityAppearGlowing|startAttack|continueAttack|startUseItem|rightClickDelay|missTime|hitResult|getConnection'
p net.minecraft.client.renderer.LevelRenderer 'render|extract'
p net.minecraft.client.renderer.GameRenderer 'Fov|fov|zoom'
p net.minecraft.client.Camera 'Fov|fov|position|getPosition'
p net.minecraft.gizmos.Gizmos '.'
p net.minecraft.gizmos.GizmoStyle '.'
p net.minecraft.gizmos.GizmoProperties '.'
p net.minecraft.client.renderer.LevelRenderer 'Gizmo|gizmo|collect'
p net.minecraft.client.MouseHandler 'turn|sensitivity|accumulated|onMove|smooth'
p net.minecraft.client.KeyMapping 'setDown|isDown|consumeClick|clickCount|getKey|matches'
echo "### Minecraft.startAttack swing call"
javap -c -p -cp "$jar" net.minecraft.client.Minecraft 2>/dev/null | grep -B6 'swing' | head -40
for c in $(unzip -Z1 "$jar" | grep -E 'SwingAnimation[^$]*\.class$|SetEntityMotionPacket\.class$|ServerboundMovePlayerPacket\.class$|ClientboundExplodePacket\.class$' | sed 's/\.class$//; s#/#.#g'); do
  echo "### $c"
  javap -p -cp "$jar" "$c" 2>&1 | grep -vE 'lambda\$' | head -40
done
echo "### fabric render events"
fapi=$(find ~/.gradle/caches -name 'fabric-rendering-v1*.jar' 2>/dev/null | grep -v sources | head -1)
[ -n "$fapi" ] && unzip -Z1 "$fapi" | grep -E 'event/.*\.class$|Context\.class$' | grep -v '\$' | head -30
for c in $(unzip -Z1 "${fapi:-/dev/null}" 2>/dev/null | grep -E 'WorldRenderEvents\.class$|WorldRenderContext\.class$|LevelRenderEvents\.class$|LevelRenderContext\.class$' | sed 's/\.class$//; s#/#.#g'); do
  echo "### $c"; javap -p -cp "$fapi" "$c" 2>&1 | head -40
done
exit 0
