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

Пока поддерживается только офлайн-аккаунт (одиночная игра и сервера без лицензии). Вход через Microsoft добавим отдельно.

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

В разработке. Готовый jar клиента вшивается в лаунчер как ресурс `client/vibe-client.jar`
и при каждом запуске копируется в папку `mods`.
