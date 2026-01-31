package pl.kaqvu.music;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class MusicCommand {
        private static final SuggestionProvider<FabricClientCommandSource> file_suggestions = (context, builder) -> {
                List<String> files = MusicManager.getmusicfiles();
                for (String file : files) {
                        if (file.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                builder.suggest(file);
                        }
                }
                if ("last".startsWith(builder.getRemainingLowerCase())) {
                        builder.suggest("last");
                }
                return builder.buildFuture();
        };

        private static final SuggestionProvider<FabricClientCommandSource> count_suggestions = (context, builder) -> {
                builder.suggest("infinity");
                return builder.buildFuture();
        };

        public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
                dispatcher.register(ClientCommandManager.literal("kaqvumusic")
                                .executes(context -> {
                                        showhelp(context.getSource());
                                        context.getSource().getPlayer().sendMessage(Text.literal("§awyswietlono pomoc"),
                                                        true);
                                        return 1;
                                })
                                .then(ClientCommandManager.literal("help")
                                                .executes(context -> {
                                                        showhelp(context.getSource());
                                                        context.getSource().getPlayer().sendMessage(
                                                                        Text.literal("§awyswietlono pomoc"), true);
                                                        return 1;
                                                }))
                                .then(ClientCommandManager.literal("list")
                                                .executes(context -> {
                                                        List<String> files = MusicManager.getmusicfiles();
                                                        if (files.isEmpty()) {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§c§l✗ §7brak plikow mp3 w folderze"));
                                                        } else {
                                                                context.getSource().sendFeedback(Text
                                                                                .literal("§6§l♪ §elista muzyki §8(§7"
                                                                                                + files.size()
                                                                                                + "§8)"));
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§8§m                              "));
                                                                for (int i = 0; i < files.size(); i++) {
                                                                        String color = (i % 2 == 0) ? "§b" : "§3";
                                                                        context.getSource().sendFeedback(Text.literal(
                                                                                        "  " + color + "• §f" + files
                                                                                                        .get(i)));
                                                                }
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§8§m                              "));
                                                        }
                                                        context.getSource().getPlayer().sendMessage(
                                                                        Text.literal("§awyswietlono liste muzyki"),
                                                                        true);
                                                        return 1;
                                                }))
                                .then(ClientCommandManager.literal("history")
                                                .executes(context -> {
                                                        List<MusicManager.HistoryEntry> history = MusicManager
                                                                        .gethistory();
                                                        context.getSource().sendFeedback(
                                                                        Text.literal("§6§l♪ §ehistoria muzyki"));
                                                        context.getSource().sendFeedback(Text
                                                                        .literal("§8§m                              "));
                                                        for (int i = 0; i < history.size(); i++) {
                                                                MusicManager.HistoryEntry h = history.get(i);
                                                                String num = String.valueOf(i + 1);
                                                                if (h.filename.equals("Puste")) {
                                                                        context.getSource().sendFeedback(Text.literal(
                                                                                        "§7" + num + ". §8Puste"));
                                                                } else {
                                                                        String countdisplay = h.count.equals("∞") ? "∞"
                                                                                        : "x" + h.count;
                                                                        String datestr = h.date.equals("aktualnie")
                                                                                        ? "§a(aktualnie)"
                                                                                        : "§7(" + h.date + ")";
                                                                        context.getSource().sendFeedback(Text.literal(
                                                                                        "§b" + num + ". §f" + h.filename
                                                                                                        + " §8| §e"
                                                                                                        + countdisplay
                                                                                                        + " §8| "
                                                                                                        + datestr));
                                                                }
                                                        }
                                                        context.getSource().sendFeedback(Text
                                                                        .literal("§8§m                              "));
                                                        context.getSource().getPlayer().sendMessage(
                                                                        Text.literal("§awyswietlono historie muzyki"),
                                                                        true);
                                                        return 1;
                                                }))
                                .then(ClientCommandManager.literal("play")
                                                .executes(context -> {
                                                        context.getSource().sendFeedback(Text.literal(
                                                                        "§6§l♪ §euzycie: §f/kaqvumusic play <plik.mp3|last> [ilosc|infinity]"));
                                                        context.getSource().sendFeedback(Text.literal(
                                                                        "§7  odtwarza plik mp3 z folderu config/kaqvuMusic"));
                                                        context.getSource().sendFeedback(Text.literal(
                                                                        "§7  last - odtwarza ostatnio grana muzyke"));
                                                        context.getSource().getPlayer().sendMessage(
                                                                        Text.literal("§ewyswietlono pomoc dla play"),
                                                                        true);
                                                        return 1;
                                                })
                                                .then(ClientCommandManager.argument("file", StringArgumentType.string())
                                                                .suggests(file_suggestions)
                                                                .executes(context -> {
                                                                        String file = StringArgumentType
                                                                                        .getString(context, "file");
                                                                        return playmusic(context.getSource(), file, 1,
                                                                                        false);
                                                                })
                                                                .then(ClientCommandManager
                                                                                .argument("count",
                                                                                                StringArgumentType
                                                                                                                .word())
                                                                                .suggests(count_suggestions)
                                                                                .executes(context -> {
                                                                                        String file = StringArgumentType
                                                                                                        .getString(context,
                                                                                                                        "file");
                                                                                        String countstr = StringArgumentType
                                                                                                        .getString(context,
                                                                                                                        "count");
                                                                                        boolean infinity = countstr
                                                                                                        .equalsIgnoreCase(
                                                                                                                        "infinity");
                                                                                        int count = 1;
                                                                                        if (!infinity) {
                                                                                                try {
                                                                                                        count = Integer.parseInt(
                                                                                                                        countstr);
                                                                                                        if (count < 1)
                                                                                                                count = 1;
                                                                                                } catch (NumberFormatException e) {
                                                                                                        context.getSource()
                                                                                                                        .sendFeedback(Text
                                                                                                                                        .literal("§c§l✗ §7nieprawidlowa wartosc: §f"
                                                                                                                                                        + countstr));
                                                                                                        context.getSource()
                                                                                                                        .getPlayer()
                                                                                                                        .sendMessage(Text
                                                                                                                                        .literal("§cuzyj liczby lub infinity"),
                                                                                                                                        true);
                                                                                                        return 1;
                                                                                                }
                                                                                        }
                                                                                        return playmusic(context
                                                                                                        .getSource(),
                                                                                                        file, count,
                                                                                                        infinity);
                                                                                }))))
                                .then(ClientCommandManager.literal("pause")
                                                .executes(context -> {
                                                        if (MusicManager.pause()) {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§e§l⏸ §fzapauzowano: §b" + MusicManager
                                                                                                .getcurrentfile()));
                                                                context.getSource().getPlayer().sendMessage(
                                                                                Text.literal("§ezapauzowano muzyke"),
                                                                                true);
                                                        } else if (MusicManager.ispaused()) {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§c§l✗ §7muzyka jest juz zapauzowana"));
                                                                context.getSource().getPlayer().sendMessage(Text
                                                                                .literal("§cmuzyka juz zapauzowana"),
                                                                                true);
                                                        } else {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§c§l✗ §7zadna muzyka nie jest wlaczona"));
                                                                context.getSource().getPlayer().sendMessage(Text
                                                                                .literal("§cmuzyka nie jest wlaczona"),
                                                                                true);
                                                        }
                                                        return 1;
                                                }))
                                .then(ClientCommandManager.literal("resume")
                                                .executes(context -> {
                                                        if (MusicManager.resume()) {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§a§l▶ §fwznowiono: §b" + MusicManager
                                                                                                .getcurrentfile()));
                                                                context.getSource().getPlayer().sendMessage(
                                                                                Text.literal("§awznowiono muzyke"),
                                                                                true);
                                                        } else if (MusicManager.isplaying()) {
                                                                context.getSource().sendFeedback(
                                                                                Text.literal("§c§l✗ §7muzyka juz gra"));
                                                                context.getSource().getPlayer().sendMessage(
                                                                                Text.literal("§cmuzyka juz gra"), true);
                                                        } else {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§c§l✗ §7zadna muzyka nie jest zapauzowana"));
                                                                context.getSource().getPlayer().sendMessage(Text
                                                                                .literal("§cmuzyka nie jest zapauzowana"),
                                                                                true);
                                                        }
                                                        return 1;
                                                }))
                                .then(ClientCommandManager.literal("stop")
                                                .executes(context -> {
                                                        String file = MusicManager.getcurrentfile();
                                                        if (MusicManager.stop()) {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§c§l⏹ §fzatrzymano: §b" + file));
                                                                context.getSource().getPlayer().sendMessage(
                                                                                Text.literal("§czatrzymano muzyke"),
                                                                                true);
                                                        } else {
                                                                context.getSource().sendFeedback(Text.literal(
                                                                                "§c§l✗ §7zadna muzyka nie jest wlaczona"));
                                                                context.getSource().getPlayer().sendMessage(Text
                                                                                .literal("§cmuzyka nie jest wlaczona"),
                                                                                true);
                                                        }
                                                        return 1;
                                                }))
                                .then(ClientCommandManager.literal("volume")
                                                .executes(context -> {
                                                        context.getSource().sendFeedback(Text.literal(
                                                                        "§6§l♪ §euzycie: §f/kaqvumusic volume <0-100>"));
                                                        context.getSource().sendFeedback(
                                                                        Text.literal("§7  aktualna glosnosc: §f"
                                                                                        + MusicManager.getvolume()
                                                                                        + "%"));
                                                        context.getSource().getPlayer().sendMessage(
                                                                        Text.literal("§ewyswietlono pomoc dla volume"),
                                                                        true);
                                                        return 1;
                                                })
                                                .then(ClientCommandManager
                                                                .argument("percent",
                                                                                IntegerArgumentType.integer(0, 100))
                                                                .executes(context -> {
                                                                        int percent = IntegerArgumentType
                                                                                        .getInteger(context, "percent");
                                                                        MusicManager.setvolume(percent);
                                                                        context.getSource().sendFeedback(Text.literal(
                                                                                        "§a§l🔊 §fglosnosc ustawiona na: §b"
                                                                                                        + percent
                                                                                                        + "%"));
                                                                        context.getSource().getPlayer().sendMessage(
                                                                                        Text.literal("§aglosnosc: §f"
                                                                                                        + percent
                                                                                                        + "%"),
                                                                                        true);
                                                                        return 1;
                                                                }))));
        }

        private static int playmusic(FabricClientCommandSource source, String file, int count, boolean infinity) {
                if (file.equalsIgnoreCase("last")) {
                        MusicManager.HistoryEntry last = MusicManager.getlastplayed();
                        if (last == null) {
                                source.sendFeedback(Text.literal("§c§l✗ §7brak informacji o ostatnio granej muzyce"));
                                source.getPlayer().sendMessage(Text.literal("§cbrak historii"), true);
                                return 1;
                        }
                        file = last.filename;
                        if (!MusicManager.fileexists(file)) {
                                source.sendFeedback(Text.literal("§c§l✗ §7plik nie istnieje: §f" + file));
                                source.getPlayer().sendMessage(Text.literal("§cplik zostal usuniety"), true);
                                return 1;
                        }
                }

                if (MusicManager.play(file, count, infinity)) {
                        String countdisplay = infinity ? "∞" : "x" + count;
                        source.sendFeedback(
                                        Text.literal("§a§l▶ §fgranie: §b" + file + " §8(§7" + countdisplay + "§8)"));
                        source.getPlayer().sendMessage(Text.literal("§aodtwarzanie: §f" + file + " §7" + countdisplay),
                                        true);
                } else {
                        source.sendFeedback(Text.literal("§c§l✗ §7nie znaleziono pliku: §f" + file));
                        source.getPlayer().sendMessage(Text.literal("§cnie znaleziono pliku"), true);
                }
                return 1;
        }

        private static void showhelp(FabricClientCommandSource source) {
                source.sendFeedback(Text.literal("§6§l♪ §ekaqvumusic §8- §7lista komend"));
                source.sendFeedback(Text.literal("§8§m                              "));
                source.sendFeedback(Text.literal("§b/kaqvumusic help §8- §7wyswietla pomoc"));
                source.sendFeedback(Text.literal("§b/kaqvumusic list §8- §7lista plikow mp3"));
                source.sendFeedback(Text.literal("§b/kaqvumusic history §8- §7historia odtwarzania"));
                source.sendFeedback(
                                Text.literal("§b/kaqvumusic play <plik|last> [ilosc|infinity] §8- §7odtwarza muzyke"));
                source.sendFeedback(Text.literal("§b/kaqvumusic pause §8- §7pauzuje muzyke"));
                source.sendFeedback(Text.literal("§b/kaqvumusic resume §8- §7wznawia muzyke"));
                source.sendFeedback(Text.literal("§b/kaqvumusic stop §8- §7zatrzymuje muzyke"));
                source.sendFeedback(Text.literal("§b/kaqvumusic volume <0-100> §8- §7ustawia glosnosc"));
                source.sendFeedback(Text.literal("§8§m                              "));
        }
}
