# Vibe Client

Свой клиент Minecraft и лаунчер к нему.

![Лаунчер](docs/launcher.png)

## Лаунчер

Папка `launcher/`. Java 17+, интерфейс на Swing (FlatLaf).

Что делает при нажатии «Играть»:

1. Берёт список версий у Mojang и выбирает последнюю (или выбранную).
2. Ставит Fabric Loader для этой версии (meta.fabricmc.net).
3. Скачивает jar игры, библиотеки, ресурсы, проверяя sha1.
4. Скачивает нужную Java от Mojang, ставить Java вручную не надо.
5. Кладёт в `mods/` Fabric API (с Modrinth) и сам Vibe Client.
6. Запускает игру. Лог видно по кнопке «Открыть лог».

Всё хранится отдельно от обычного Minecraft:

| ОС | Папка |
|---|---|
| Windows | `%APPDATA%\.vibeclient` |
| macOS | `~/Library/Application Support/vibeclient` |
| Linux | `~/.vibeclient` |

### Аккаунты

- **По нику (офлайн)**: одиночная игра и сервера без лицензии.
- **Microsoft (лицензия)**: кнопка «Войти через Microsoft». Нужен свой client id приложения Azure:
  1. Зарегистрируй приложение на portal.azure.com (App registrations), тип аккаунтов «Personal Microsoft accounts only»,
     включи «Allow public client flows».
  2. Подай заявку Mojang на доступ к Minecraft API для этого приложения (форма по ссылке в
     [Minecraft Help](https://help.minecraft.net/hc/en-us/articles/16254801392141)). Без одобрения вход
     дойдёт до последнего шага и получит отказ.
  3. Впиши client id в `settings.json` лаунчера (`"msClientId": "..."`) или передай `-Dvibe.msClientId=...`.

### Сборка

```
./gradlew build
```

Готовый файл: `launcher/build/libs/VibeLauncher.jar`, запуск двойным кликом или `java -jar VibeLauncher.jar`.

Без окна (удобно для отладки):

```
java -jar VibeLauncher.jar --cli --name Steve [--version 1.21.10] [--memory 4096] [--client путь/к/vibe-client.jar]
```

## Клиент

Папка `client/`. Minecraft 26.3, Fabric. Собственный код, без чужих исходников.

- HUD: FPS, координаты с направлением, CPS, нажатые клавиши, часы.
- Авто-бег.
- Меню на **Right Shift**: включение модулей и перетаскивание HUD мышью. Настройки в `config/vibe-client.json`.
- «Vibe Client» в заголовке окна и на главном экране.

Собранный клиент вшивается в лаунчер (`client/vibe-client.jar`) и при каждом запуске копируется в `mods`.

Версии игры, Fabric и Fabric API лежат в `gradle.properties`.

## Сборка в GitHub Actions

Каждый push собирает лаунчер и клиент, запускает игру в виртуальном экране для проверки
и выкладывает `VibeLauncher.jar` в артефакты сборки (вкладка Actions).
