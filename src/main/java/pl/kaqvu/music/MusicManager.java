package pl.kaqvu.music;

import javazoom.jl.decoder.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.*;

public class MusicManager {
    private static final Path music_folder = FabricLoader.getInstance().getGameDir().resolve("config")
            .resolve("kaqvuMusic");
    private static final Path config_file = music_folder.resolve("config.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static String current_file;
    private static int play_count;
    private static int max_play_count;
    private static boolean is_infinity;
    private static volatile boolean is_paused;
    private static volatile boolean is_playing;
    private static volatile boolean stop_requested;
    private static Thread playback_thread;
    private static Thread display_thread;
    private static SourceDataLine audio_line;
    private static float volume = 1.0f;
    private static volatile long current_time_ms;
    private static volatile long total_time_ms;
    private static List<HistoryEntry> history = new ArrayList<>();
    private static String current_start_time;

    public static void init() {
        try {
            Files.createDirectories(music_folder);
        } catch (IOException ignored) {
        }
        loadconfig();
        Runtime.getRuntime().addShutdownHook(new Thread(MusicManager::stop));
    }

    private static void loadconfig() {
        try {
            if (Files.exists(config_file)) {
                String json = Files.readString(config_file);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("volume")) {
                    volume = obj.get("volume").getAsFloat();
                }
                if (obj.has("history")) {
                    JsonArray arr = obj.getAsJsonArray("history");
                    history.clear();
                    for (JsonElement el : arr) {
                        JsonObject entry = el.getAsJsonObject();
                        HistoryEntry h = new HistoryEntry();
                        h.filename = entry.get("filename").getAsString();
                        h.count = entry.get("count").getAsString();
                        h.date = entry.get("date").getAsString();
                        history.add(h);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void saveconfig() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("volume", volume);
            JsonArray arr = new JsonArray();
            for (HistoryEntry h : history) {
                JsonObject entry = new JsonObject();
                entry.addProperty("filename", h.filename);
                entry.addProperty("count", h.count);
                entry.addProperty("date", h.date);
                arr.add(entry);
            }
            obj.add("history", arr);
            Files.writeString(config_file, gson.toJson(obj));
        } catch (Exception ignored) {
        }
    }

    private static void addtohistory(String filename, String count, String date) {
        HistoryEntry entry = new HistoryEntry();
        entry.filename = filename;
        entry.count = count;
        entry.date = date;
        history.add(0, entry);
        while (history.size() > 3) {
            history.remove(history.size() - 1);
        }
        saveconfig();
    }

    public static List<HistoryEntry> gethistory() {
        List<HistoryEntry> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i < history.size()) {
                HistoryEntry h = history.get(i);
                HistoryEntry copy = new HistoryEntry();
                copy.filename = h.filename;
                copy.count = h.count;
                if (i == 0 && is_playing && current_file != null && current_file.equals(h.filename)) {
                    copy.date = "aktualnie";
                } else {
                    copy.date = h.date;
                }
                result.add(copy);
            } else {
                HistoryEntry empty = new HistoryEntry();
                empty.filename = "Puste";
                empty.count = "-";
                empty.date = "-";
                result.add(empty);
            }
        }
        return result;
    }

    public static HistoryEntry getlastplayed() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(0);
    }

    public static List<String> getmusicfiles() {
        List<String> files = new ArrayList<>();
        try {
            if (Files.exists(music_folder)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(music_folder, "*.mp3")) {
                    for (Path path : stream) {
                        files.add(path.getFileName().toString());
                    }
                }
            }
        } catch (IOException ignored) {
        }
        Collections.sort(files);
        return files;
    }

    public static boolean fileexists(String filename) {
        return Files.exists(music_folder.resolve(filename));
    }

    public static boolean play(String filename, int count, boolean infinity) {
        stop();
        Path filepath = music_folder.resolve(filename);
        if (!Files.exists(filepath)) {
            return false;
        }
        current_file = filename;
        play_count = 0;
        max_play_count = count;
        is_infinity = infinity;
        is_paused = false;
        stop_requested = false;
        current_time_ms = 0;
        total_time_ms = calculateDuration(filepath);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        current_start_time = LocalDateTime.now().format(formatter);
        String countstr = infinity ? "∞" : String.valueOf(count);
        addtohistory(filename, countstr, current_start_time);

        startplayback(filepath);
        startdisplaythread();
        return true;
    }

    private static long calculateDuration(Path filepath) {
        try {
            FileInputStream fis = new FileInputStream(filepath.toFile());
            Bitstream bitstream = new Bitstream(fis);
            long totalms = 0;
            Header header;
            while ((header = bitstream.readFrame()) != null) {
                totalms += (long) (header.ms_per_frame());
                bitstream.closeFrame();
            }
            bitstream.close();
            fis.close();
            return totalms;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void startdisplaythread() {
        display_thread = new Thread(() -> {
            while (is_playing && !stop_requested) {
                if (!is_paused) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {
                        String currenttime = formattime(current_time_ms);
                        String totaltime = formattime(total_time_ms);
                        int currentqueue = play_count + 1;
                        String queuestr = is_infinity ? (currentqueue + "/∞") : (currentqueue + "/" + max_play_count);
                        String message = "§b§l♪ §f" + current_file + " §8| §a" + currenttime + "§7/§2" + totaltime
                                + " §8| §ekolejka §6" + queuestr;
                        client.execute(() -> {
                            if (client.player != null) {
                                client.player.sendMessage(Text.literal(message), true);
                            }
                        });
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        display_thread.setDaemon(true);
        display_thread.start();
    }

    private static String formattime(long ms) {
        long totalseconds = ms / 1000;
        long minutes = totalseconds / 60;
        long seconds = totalseconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static void startplayback(Path filepath) {
        is_playing = true;
        stop_requested = false;
        playback_thread = new Thread(() -> {
            try {
                playfile(filepath);
                while (!stop_requested && is_playing) {
                    play_count++;
                    current_time_ms = 0;
                    if (is_infinity || play_count < max_play_count) {
                        playfile(filepath);
                    } else {
                        is_playing = false;
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client != null && client.player != null) {
                            client.execute(() -> {
                                client.player.sendMessage(
                                        Text.literal("§emuzyka §f" + current_file + " §ezostala wylaczona"), true);
                            });
                        }
                    }
                }
            } catch (Exception ignored) {
                is_playing = false;
            }
        });
        playback_thread.setDaemon(true);
        playback_thread.start();
    }

    private static void playfile(Path filepath) throws Exception {
        FileInputStream fis = new FileInputStream(filepath.toFile());
        Bitstream bitstream = new Bitstream(fis);
        Decoder decoder = new Decoder();

        Header header = bitstream.readFrame();
        if (header == null) {
            fis.close();
            return;
        }

        int samplerate = header.frequency();
        int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
        float mspf = header.ms_per_frame();

        AudioFormat format = new AudioFormat(samplerate, 16, channels, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        audio_line = (SourceDataLine) AudioSystem.getLine(info);
        audio_line.open(format);
        audio_line.start();
        applyvolume();

        bitstream.closeFrame();
        bitstream.close();
        fis.close();

        fis = new FileInputStream(filepath.toFile());
        bitstream = new Bitstream(fis);

        byte[] buffer = new byte[4096];
        current_time_ms = 0;

        while (!stop_requested && is_playing) {
            while (is_paused && !stop_requested) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }

            if (stop_requested)
                break;

            header = bitstream.readFrame();
            if (header == null)
                break;

            mspf = header.ms_per_frame();

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
            short[] samples = output.getBuffer();
            int len = output.getBufferLength();

            int bytes = len * 2;
            if (buffer.length < bytes) {
                buffer = new byte[bytes];
            }

            for (int i = 0; i < len; i++) {
                buffer[i * 2] = (byte) (samples[i] & 0xff);
                buffer[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xff);
            }

            audio_line.write(buffer, 0, bytes);
            current_time_ms += (long) mspf;
            bitstream.closeFrame();
        }

        audio_line.drain();
        audio_line.stop();
        audio_line.close();
        audio_line = null;
        bitstream.close();
        fis.close();
    }

    public static boolean pause() {
        if (is_playing && !is_paused) {
            is_paused = true;
            if (audio_line != null) {
                audio_line.stop();
            }
            return true;
        }
        return false;
    }

    public static boolean resume() {
        if (is_paused) {
            is_paused = false;
            if (audio_line != null) {
                audio_line.start();
            }
            return true;
        }
        return false;
    }

    public static boolean stop() {
        boolean was_playing = is_playing || is_paused;
        stop_requested = true;
        is_playing = false;
        is_paused = false;
        if (display_thread != null) {
            display_thread.interrupt();
            display_thread = null;
        }
        if (audio_line != null) {
            try {
                audio_line.stop();
                audio_line.close();
            } catch (Exception ignored) {
            }
            audio_line = null;
        }
        if (playback_thread != null) {
            playback_thread.interrupt();
            playback_thread = null;
        }
        current_file = null;
        play_count = 0;
        max_play_count = 0;
        is_infinity = false;
        current_time_ms = 0;
        total_time_ms = 0;
        return was_playing;
    }

    public static void setvolume(int percent) {
        volume = Math.max(0, Math.min(100, percent)) / 100.0f;
        applyvolume();
        saveconfig();
    }

    private static void applyvolume() {
        if (audio_line != null && audio_line.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) audio_line.getControl(FloatControl.Type.MASTER_GAIN);
                float db;
                if (volume <= 0.0f) {
                    db = gainControl.getMinimum();
                } else {
                    db = (float) (20.0 * Math.log10(volume));
                    db = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db));
                }
                gainControl.setValue(db);
            } catch (Exception ignored) {
            }
        }
    }

    public static int getvolume() {
        return (int) (volume * 100);
    }

    public static boolean isplaying() {
        return is_playing && !is_paused;
    }

    public static boolean ispaused() {
        return is_paused;
    }

    public static boolean hasmusic() {
        return is_playing || is_paused;
    }

    public static String getcurrentfile() {
        return current_file;
    }

    public static class HistoryEntry {
        public String filename;
        public String count;
        public String date;
    }
}
