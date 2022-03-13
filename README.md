# WordWolves2021
2021年度関東信越地区 冬季交流会アイスブレイクのために開発したワードウルフ管理Discord Bot

# Build & Use

## Requirements
- Java 17

## Build
```bash
$ git clone git@github.com:5hrz/WordWolves2021.git
$ ./gradlew shadowJar
```

## Run
```bash
$ java -jar <Path to Jar File>.jar
```

## Configuration

config.yml
```json
{
  "tokens": [],
  "status": "BEFORE_SETUP",
  "staffRoleId": -1,
  "staffCatId": -1,
  "teams": [],
  "guild": -1,
  "mainVC": -1,
  "gameAdmin": -1,
  "gameParticipant": -1,
  "teamCat": -1,
  "trueRole": -1,
  "falseRole": -1,
  "trueChannelId": -1,
  "falseChannelId": -1,
  "dummyChannelId": -1,
  "panelCh": -1,
  "panelId": -1,
  "channels": {},
  "announceChannel": -1,
  "currentTrue": "",
  "currentFalse": ""
}
```

dataset.json
```json
{
  "words": [
    {
      "trueWord": "抹茶",
      "falseWord": "緑茶",
      "dummy": "ほうじ茶"
    }
  ]
}
```

# Dependencies
- [Kotlin](https://github.com/JetBrains/kotlin) by JetBrains
- [JDA](https://github.com/DV8FromTheWorld/JDA) by DV8FromTheWorld
- [Gson](https://github.com/google/gson) by Google
- [okhttp3](https://github.com/square/okhttp) by square

# License
WordWolves2021 is released under the [MIT License](https://opensource.org/licenses/MIT).