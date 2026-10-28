#!/usr/bin/env bash
# Печатает сигнатуры классов Minecraft, которые использует клиент.
# Помогает чинить сборку после смены версии игры.
set -u
mc=$(grep -E '^minecraft_version=' "$(dirname "$0")/../gradle.properties" | cut -d= -f2)
jar=$(find ~/.gradle/caches/fabric-loom/minecraftMaven -name "minecraft-merged-deobf-$mc.jar" 2>/dev/null | head -1)
[ -n "$jar" ] || jar=$(find ~/.gradle/caches/fabric-loom/"$mc" -name 'minecraft-merged.jar' 2>/dev/null | head -1)
echo "Minecraft jar: $jar"
[ -n "$jar" ] || exit 0
for cls in \
  net.minecraft.client.Minecraft \
  net.minecraft.client.gui.Gui \
  net.minecraft.client.gui.Hud \
  net.minecraft.client.gui.GuiGraphicsExtractor \
  net.minecraft.client.gui.screens.Screen \
  net.minecraft.client.gui.screens.TitleScreen \
  net.minecraft.client.gui.components.Button \
  net.minecraft.client.KeyboardHandler \
  com.mojang.blaze3d.platform.InputConstants \
  net.minecraft.client.renderer.GameRenderer \
  net.minecraft.client.OptionInstance \
  net.minecraft.client.gui.screens.DeathScreen \
  net.minecraft.world.effect.MobEffectInstance \
  net.minecraft.world.entity.player.Player \
  net.minecraft.client.gui.components.ChatComponent \
  net.minecraft.client.renderer.LightmapRenderStateExtractor \
  net.minecraft.client.renderer.ScreenEffectRenderer \
  net.minecraft.client.Camera \
  net.minecraft.client.Options \
  net.minecraft.client.player.LocalPlayer \
  net.minecraft.client.input.KeyEvent \
  net.minecraft.client.input.MouseButtonEvent \
  net.minecraft.SharedConstants \
  net.minecraft.WorldVersion; do
  echo "=== $cls"
  javap -p -cp "$jar" "$cls" 2>&1 | grep -vE 'lambda\$' | head -250
done
