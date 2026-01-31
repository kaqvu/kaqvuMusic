<p align="center">
  <img src="https://i.imgur.com/NXNtkEC.png" alt="kaqvuMusic Logo" width="128">
</p>

# kaqvuMusic

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1+-brightgreen?style=flat-square" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Mod%20Loader-Fabric-blue?style=flat-square" alt="Fabric">
  <img src="https://img.shields.io/badge/Java-21+-red?style=flat-square" alt="Java">
</p>

<p align="center">
  <b>A professional and lightweight MP3 player mod for Minecraft Fabric 1.21.1+.</b><br>
  Manage and enjoy your music library directly in-game with real-time feedback.
</p>

---

## 🎵 Features

<p align="center">
  <b>Music Control</b> | <b>Action Bar Progress</b> | <b>History Tracking</b> | <b>Looping</b>
</p>

- **High-Quality Playback**: Smooth MP3 streaming with minimal performance impact.
- **Dynamic Action Bar**: Displays song name, progress time, and queue position in real-time.
- **Customizable Looping**: Play songs once, multiple times, or set it to `infinity` for endless loops.
- **Playback History**: Saves your last 3 played songs with timestamps and settings.
- **Independent Volume**: Adjust mod volume from 0% to 100% without affecting game master volume.
- **Easy Management**: Simple file system — just drop your MP3s into the config folder.

---

## 🚀 Getting Started

1.  **Install Requirements**: Ensure you have **Fabric Loader** and **Fabric API** installed.
2.  **Mod Placement**: Drop the `kaqvumusic.jar` into your `.minecraft/mods` directory.
3.  **Add Your Music**:
    - Launch the game once.
    - Go to `.minecraft/config/kaqvuMusic/`.
    - Drop your favorite `.mp3` files there.
4.  **Enjoy**: Use `/kaqvumusic list` and start playing!

---

## 💻 Commands

<p align="center">
  <i>All commands are available via <b>/kaqvumusic</b></i>
</p>

| Command | Argument | Description |
| :--- | :--- | :--- |
| `help` | - | Displays the command list and usage. |
| `list` | - | Shows all available MP3 files in your library. |
| `history` | - | Displays the 3 most recently played songs. |
| `play` | `<file|last> [count|infinity]` | Starts playing. Use `last` for the previous song. |
| `pause` | - | Pauses the current track. |
| `resume` | - | Resumes from where you left off. |
| `stop` | - | Completely stops playback. |
| `volume` | `<0-100>` | Adjusts the output volume. |

---

## 💬 Interface & Messages

### 📺 Action Bar (Real-time Feedback)
The status bar appears above your hotbar while music is playing:
> `§b§l♪ §fSong_Name.mp3 §8| §a1:24§7/§23:15 §8| §ekolejka §61/∞`

- **♪ Icon**: Indicates active playback.
- **Progress**: Current time vs. total duration.
- **Queue**: Displays how many repetitions are left.

### 💬 Chat Messages
The mod provides clean and colorful chat feedback for all actions:
- **Library List**: Categorized list of all your songs with bullet points.
- **History View**: Shows song name, playback mode, and the exact date/time it was started.
- **Alerts**: Instant feedback in the center of the screen (above action bar) for volume changes or status updates.

---

<p align="center">
  Developed by <b>kaqvu</b>
</p>
