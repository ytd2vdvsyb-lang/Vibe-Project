#!/usr/bin/env bash
# Печатает сигнатуры классов Minecraft, которые использует клиент.
# Помогает чинить сборку после смены версии игры.
set -u
jar=$(find ~/.gradle/caches -name 'minecraft-*client*.jar' -o -name 'minecraft-merged*.jar' 2>/dev/null | grep -v sources | head -1)
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
  net.minecraft.client.input.KeyEvent \
  net.minecraft.client.input.MouseButtonEvent \
  net.minecraft.SharedConstants \
  net.minecraft.WorldVersion; do
  echo "=== $cls"
  javap -cp "$jar" "$cls" 2>&1 | grep -vE 'lambda\$' | head -150
done
