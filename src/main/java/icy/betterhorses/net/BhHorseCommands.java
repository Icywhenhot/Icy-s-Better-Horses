package icy.betterhorses.net;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedCoatSet;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.registry.GenderType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class BhHorseCommands {

    private static final String MSG = "message.icys-better-horses.horse_cmd.";
    private static final double SPEED_FACTOR = 43.2D;
    private static final double REACH = 8.0D;
    private static final double LOOK_REACH = 6.0D;
    private static final int BOND_MAX = 100;
    private static final String RANDOM = "random";
    private static final List<String> OPTION_KEYS =
            List.of("coat", "gender", "speed", "jump", "health", "bond", "baby", "owner");

    private static final SimpleCommandExceptionType NO_HORSE =
            new SimpleCommandExceptionType(Component.translatable(MSG + "no_horse"));
    private static final SimpleCommandExceptionType CANT_SPAWN =
            new SimpleCommandExceptionType(Component.translatable(MSG + "cant_spawn"));
    private static final DynamicCommandExceptionType UNKNOWN_BREED =
            new DynamicCommandExceptionType(id -> Component.translatable(MSG + "unknown_breed", id));
    private static final DynamicCommandExceptionType UNKNOWN_GENDER =
            new DynamicCommandExceptionType(id -> Component.translatable(MSG + "unknown_gender", id));
    private static final DynamicCommandExceptionType BAD_OPTION =
            new DynamicCommandExceptionType(token -> Component.translatable(MSG + "bad_option", token));

    private BhHorseCommands() {}

    private enum Stat {
        SPEED(Attributes.MOVEMENT_SPEED, "speed"),
        JUMP(Attributes.JUMP_STRENGTH, "jump"),
        HEALTH(Attributes.MAX_HEALTH, "health");

        final Holder<Attribute> attribute;
        final String key;

        Stat(Holder<Attribute> attribute, String key) {
            this.attribute = attribute;
            this.key = key;
        }

        double shown(double base) {
            return switch (this) {
                case SPEED -> base * SPEED_FACTOR;
                case JUMP -> Math.max(0.0D, base * 6.0D - 1.0D);
                case HEALTH -> base;
            };
        }

        double base(double shown) {
            return switch (this) {
                case SPEED -> shown / SPEED_FACTOR;
                case JUMP -> (shown + 1.0D) / 6.0D;
                case HEALTH -> shown;
            };
        }

        double low(ArchetypeType arch) {
            return switch (this) {
                case SPEED -> arch.lowSpeed();
                case JUMP -> arch.lowJump();
                case HEALTH -> arch.lowHealth();
            };
        }

        double high(ArchetypeType arch) {
            return switch (this) {
                case SPEED -> arch.highSpeed();
                case JUMP -> arch.highJump();
                case HEALTH -> arch.highHealth();
            };
        }

        double clamp(ArchetypeType arch, double base) {
            return switch (this) {
                case SPEED -> arch.clampSpeed(base);
                case JUMP -> arch.clampJump(base);
                case HEALTH -> arch.clampHealth(base);
            };
        }

        Component label() {
            return Component.translatable(MSG + "stat." + key);
        }

        Component amount(double shown) {
            return Component.translatable(MSG + "unit." + key, fmt(shown));
        }
    }

    public static void build(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("horse")
                .then(properties(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)), false)
                        .then(properties(Commands.argument("targets", EntityArgument.entities()), true)))
                .then(Commands.literal("spawn")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("breed", IdentifierArgument.id())
                                .suggests(BhHorseCommands::suggestBreeds)
                                .executes(context -> spawn(context, ""))
                                .then(Commands.argument("options", StringArgumentType.greedyString())
                                        .suggests(BhHorseCommands::suggestOptions)
                                        .executes(context -> spawn(context,
                                                StringArgumentType.getString(context, "options"))))))
                .then(Commands.literal("coats")
                        .executes(context -> listCoats(context, implicitHorse(context.getSource())))
                        .then(Commands.argument("breed", IdentifierArgument.id())
                                .suggests(BhHorseCommands::suggestBreeds)
                                .executes(context -> listCoats(context, breed(context))))));
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T properties(T node, boolean targeted) {
        node.then(Commands.literal("coat")
                .then(Commands.argument("coat", StringArgumentType.word())
                        .suggests((context, builder) -> suggestTargetCoats(context, builder, targeted))
                        .executes(context -> setCoat(context, targeted))));
        node.then(Commands.literal("gender")
                .then(Commands.argument("gender", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(genderIds(), builder))
                        .executes(context -> setGender(context, targeted))));
        for (Stat stat : Stat.values()) {
            node.then(Commands.literal(stat.key)
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D))
                            .suggests((context, builder) -> suggestTargetRange(context, builder, targeted, stat))
                            .executes(context -> setStat(context, targeted, stat))));
        }
        node.then(Commands.literal("bond")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, BOND_MAX))
                        .executes(context -> setBond(context, targeted))));
        return node;
    }

    private static List<BhBreedHorse> targets(CommandContext<CommandSourceStack> context, boolean targeted)
            throws CommandSyntaxException {
        if (!targeted) {
            BhBreedHorse horse = implicitHorse(context.getSource());
            return List.of(horse);
        }
        List<BhBreedHorse> found = new ArrayList<>();
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof BhBreedHorse horse) {
                found.add(horse);
            }
        }
        if (found.isEmpty()) {
            throw NO_HORSE.create();
        }
        return found;
    }

    private static BhBreedHorse implicitHorse(CommandSourceStack source) throws CommandSyntaxException {
        Entity self = source.getEntity();
        if (self != null && self.getVehicle() instanceof BhBreedHorse ridden) {
            return ridden;
        }
        if (self instanceof ServerPlayer player) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getViewVector(1.0F);
            AABB box = player.getBoundingBox().expandTowards(look.scale(LOOK_REACH)).inflate(1.0D);
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, eye.add(look.scale(LOOK_REACH)), box,
                    entity -> entity instanceof BhBreedHorse && entity.isPickable(), LOOK_REACH * LOOK_REACH);
            if (hit != null && hit.getEntity() instanceof BhBreedHorse seen) {
                return seen;
            }
        }
        Vec3 origin = source.getPosition();
        BhBreedHorse best = null;
        double bestDist = Double.MAX_VALUE;
        for (BhBreedHorse horse : source.getLevel().getEntitiesOfClass(BhBreedHorse.class,
                new AABB(origin, origin).inflate(REACH))) {
            double d = horse.distanceToSqr(origin);
            if (d < bestDist) {
                bestDist = d;
                best = horse;
            }
        }
        if (best == null) {
            throw NO_HORSE.create();
        }
        return best;
    }

    private static ArchetypeType archetype(BhBreedHorse horse) {
        return BhBreedData.of(horse.bhFixedBreed()).archetype();
    }

    private static Component name(BhBreedHorse horse) {
        return horse.getDisplayName();
    }

    private static int setCoat(CommandContext<CommandSourceStack> context, boolean targeted)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String coat = StringArgumentType.getString(context, "coat");
        int done = 0;
        for (BhBreedHorse horse : targets(context, targeted)) {
            BreedCoatSet coats = horse.bhCoatSet();
            int index = coat.equals(RANDOM) ? coats.roll(horse.getRandom()) : coats.coatIds().indexOf(coat);
            if (index < 0) {
                source.sendFailure(Component.translatable(MSG + "unknown_coat",
                        name(horse), coat, String.join(", ", coats.coatIds())));
                continue;
            }
            horse.bhSetCoat(index);
            refresh(horse);
            done++;
            source.sendSuccess(() -> Component.translatable(MSG + "coat_set", name(horse),
                    coats.displayName(index)).withStyle(ChatFormatting.GREEN), true);
        }
        return done;
    }

    private static int setGender(CommandContext<CommandSourceStack> context, boolean targeted)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String raw = StringArgumentType.getString(context, "gender");
        int done = 0;
        for (BhBreedHorse horse : targets(context, targeted)) {
            ResourceKey<GenderType> gender = gender(raw, horse);
            IHorseData.of(horse).bh_setGender(gender);
            refresh(horse);
            done++;
            source.sendSuccess(() -> Component.translatable(MSG + "gender_set", name(horse),
                    GenderType.displayName(gender)).withStyle(ChatFormatting.GREEN), true);
        }
        return done;
    }

    private static int setStat(CommandContext<CommandSourceStack> context, boolean targeted, Stat stat)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        double wanted = DoubleArgumentType.getDouble(context, "value");
        int done = 0;
        for (BhBreedHorse horse : targets(context, targeted)) {
            report(source, horse, stat, wanted, applyStat(horse, stat, wanted));
            if (stat == Stat.HEALTH) {
                horse.setHealth(horse.getMaxHealth());
            }
            refresh(horse);
            done++;
        }
        return done;
    }

    private static double applyStat(BhBreedHorse horse, Stat stat, double wanted) {
        double base = stat.clamp(archetype(horse), stat.base(wanted));
        AttributeInstance instance = horse.getAttribute(stat.attribute);
        if (instance != null) {
            instance.setBaseValue(base);
        }
        return stat.shown(base);
    }

    private static void report(CommandSourceStack source, BhBreedHorse horse, Stat stat, double wanted, double actual) {
        if (Math.abs(actual - wanted) > 0.05D) {
            ArchetypeType arch = archetype(horse);
            source.sendSuccess(() -> Component.translatable(MSG + "stat_clamped", name(horse), stat.label(),
                    stat.amount(actual), stat.amount(stat.shown(stat.low(arch))), stat.amount(stat.shown(stat.high(arch))))
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        source.sendSuccess(() -> Component.translatable(MSG + "stat_set", name(horse), stat.label(), stat.amount(actual))
                .withStyle(ChatFormatting.GREEN), true);
    }

    private static int setBond(CommandContext<CommandSourceStack> context, boolean targeted)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        int level = IntegerArgumentType.getInteger(context, "level");
        int done = 0;
        for (BhBreedHorse horse : targets(context, targeted)) {
            IHorseData.of(horse).bh_setBond(level);
            refresh(horse);
            done++;
            int tier = BhHorseTraits.bondTier(level);
            source.sendSuccess(() -> Component.translatable("message.icys-better-horses.bond.set",
                    name(horse), level, tier + 1).withStyle(ChatFormatting.GREEN), true);
        }
        return done;
    }

    private static void refresh(BhBreedHorse horse) {
        if (IHorseData.of(horse).bh_isOwned()) {
            HorseTracker.register(horse);
        }
    }

    private static int spawn(CommandContext<CommandSourceStack> context, String options)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ResourceKey<BreedType> breedKey = breed(context);
        Map<String, String> picked = parseOptions(options);

        EntityType<? extends BhBreedHorse> type = ModEntities.forBreed(breedKey);
        ServerLevel level = source.getLevel();
        BhBreedHorse horse = type == null ? null : type.create(level, EntitySpawnReason.COMMAND);
        if (horse == null) {
            throw CANT_SPAWN.create();
        }
        Vec3 pos = source.getPosition();
        horse.snapTo(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);
        horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()),
                EntitySpawnReason.COMMAND, null);

        BreedCoatSet coats = horse.bhCoatSet();
        String coat = picked.getOrDefault("coat", RANDOM);
        if (!coat.equals(RANDOM)) {
            int index = coats.coatIds().indexOf(coat);
            if (index < 0) {
                source.sendFailure(Component.translatable(MSG + "unknown_coat",
                        BreedType.displayName(breedKey, false), coat, String.join(", ", coats.coatIds())));
                return 0;
            }
            horse.bhSetCoat(index);
        }

        String gender = picked.getOrDefault("gender", RANDOM);
        if (!gender.equals(RANDOM)) {
            IHorseData.of(horse).bh_setGender(gender(gender, horse));
        }

        Map<Stat, Double> wanted = new HashMap<>();
        for (Stat stat : Stat.values()) {
            String raw = picked.get(stat.key);
            if (raw != null) {
                wanted.put(stat, number(stat.key, raw));
            }
        }
        Map<Stat, Double> actual = new HashMap<>();
        for (Map.Entry<Stat, Double> entry : wanted.entrySet()) {
            actual.put(entry.getKey(), applyStat(horse, entry.getKey(), entry.getValue()));
        }
        horse.setHealth(horse.getMaxHealth());

        if (Boolean.parseBoolean(picked.getOrDefault("baby", "false"))) {
            horse.setAge(-24000);
        }

        ServerPlayer owner = null;
        String ownerName = picked.get("owner");
        if (ownerName != null) {
            owner = source.getServer().getPlayerList().getPlayerByName(ownerName);
            if (owner == null) {
                source.sendFailure(Component.translatable(MSG + "unknown_owner", ownerName));
                return 0;
            }
        }

        Integer bond = null;
        String rawBond = picked.get("bond");
        if (rawBond != null) {
            bond = (int) Math.max(0, Math.min(BOND_MAX, Math.round(number("bond", rawBond))));
        }

        if (!level.addFreshEntity(horse)) {
            throw CANT_SPAWN.create();
        }
        if (owner != null) {
            horse.setTamed(true);
            horse.setOwner(owner);
            IHorseData.of(horse).bh_setOwner(owner.getUUID());
        }
        if (bond != null) {
            IHorseData.of(horse).bh_setBond(bond);
        }
        refresh(horse);

        for (Map.Entry<Stat, Double> entry : wanted.entrySet()) {
            double got = actual.get(entry.getKey());
            if (Math.abs(got - entry.getValue()) > 0.05D) {
                report(source, horse, entry.getKey(), entry.getValue(), got);
            }
        }
        source.sendSuccess(() -> Component.translatable(MSG + "spawned",
                BreedType.displayName(breedKey, false),
                coats.displayName(horse.bhCoat()),
                GenderType.displayName(IHorseData.of(horse).bh_getGender()),
                fmt(Stat.SPEED.shown(horse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED))),
                fmt(Stat.JUMP.shown(horse.getAttributeBaseValue(Attributes.JUMP_STRENGTH))),
                fmt(horse.getAttributeBaseValue(Attributes.MAX_HEALTH))).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static Map<String, String> parseOptions(String options) throws CommandSyntaxException {
        Map<String, String> picked = new HashMap<>();
        for (String token : options.trim().split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            int eq = token.indexOf('=');
            String key = eq < 0 ? token : token.substring(0, eq).toLowerCase(Locale.ROOT);
            if (eq <= 0 || eq == token.length() - 1 || !OPTION_KEYS.contains(key)) {
                throw BAD_OPTION.create(token);
            }
            picked.put(key, token.substring(eq + 1));
        }
        return picked;
    }

    private static double number(String key, String raw) throws CommandSyntaxException {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw BAD_OPTION.create(key + "=" + raw);
        }
    }

    private static int listCoats(CommandContext<CommandSourceStack> context, BhBreedHorse horse) {
        return listCoats(context, horse.bhFixedBreed());
    }

    private static int listCoats(CommandContext<CommandSourceStack> context, ResourceKey<BreedType> breedKey) {
        BreedType type = BhRegistries.breedTypeRegistry().getValue(breedKey.identifier());
        if (type == null) {
            return 0;
        }
        List<String> ids = type.coats().coatIds();
        context.getSource().sendSuccess(() -> Component.translatable(MSG + "coats",
                BreedType.displayName(breedKey, false), ids.size(), String.join(", ", ids)), false);
        return ids.size();
    }

    private static ResourceKey<BreedType> breed(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ResourceKey<BreedType> key = breedOrNull(context);
        if (key == null) {
            throw UNKNOWN_BREED.create(context.getArgument("breed", Identifier.class).toString());
        }
        return key;
    }

    private static @Nullable ResourceKey<BreedType> breedOrNull(CommandContext<CommandSourceStack> context) {
        Identifier id = context.getArgument("breed", Identifier.class);
        if (!BhRegistries.breedTypeRegistry().containsKey(id) && id.getNamespace().equals("minecraft")) {
            id = Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, id.getPath());
        }
        if (!BhRegistries.breedTypeRegistry().containsKey(id)) {
            return null;
        }
        ResourceKey<BreedType> key = ResourceKey.create(BhRegistries.BREED_TYPES, id);
        return ModEntities.forBreed(key) == null ? null : key;
    }

    private static ResourceKey<GenderType> gender(String raw, BhBreedHorse horse) throws CommandSyntaxException {
        if (raw.equals(RANDOM)) {
            List<Identifier> all = new ArrayList<>(BhRegistries.genderTypeRegistry().keySet());
            return ResourceKey.create(BhRegistries.GENDER_TYPES, all.get(horse.getRandom().nextInt(all.size())));
        }
        Identifier id = Identifier.tryParse(raw.contains(":") ? raw : IcysBetterHorses.MOD_ID + ":" + raw);
        if (id == null || !BhRegistries.genderTypeRegistry().containsKey(id)) {
            throw UNKNOWN_GENDER.create(raw);
        }
        return ResourceKey.create(BhRegistries.GENDER_TYPES, id);
    }

    private static List<String> genderIds() {
        return BhRegistries.genderTypeRegistry().keySet().stream().map(BhHorseCommands::shortId).toList();
    }

    private static String shortId(Identifier id) {
        return id.getNamespace().equals(IcysBetterHorses.MOD_ID) ? id.getPath() : id.toString();
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static CompletableFuture<Suggestions> suggestBreeds(CommandContext<CommandSourceStack> context,
                                                                SuggestionsBuilder builder) {
        List<String> ids = new ArrayList<>();
        for (Identifier id : BhRegistries.breedTypeRegistry().keySet()) {
            if (ModEntities.forBreed(ResourceKey.create(BhRegistries.BREED_TYPES, id)) != null) {
                ids.add(shortId(id));
            }
        }
        return SharedSuggestionProvider.suggest(ids, builder);
    }

    private static CompletableFuture<Suggestions> suggestTargetCoats(CommandContext<CommandSourceStack> context,
                                                                     SuggestionsBuilder builder, boolean targeted) {
        List<String> ids = new ArrayList<>();
        ids.add(RANDOM);
        try {
            for (BhBreedHorse horse : targets(context, targeted)) {
                for (String id : horse.bhCoatSet().coatIds()) {
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                }
            }
        } catch (CommandSyntaxException ignored) {
        }
        return SharedSuggestionProvider.suggest(ids, builder);
    }

    private static CompletableFuture<Suggestions> suggestTargetRange(CommandContext<CommandSourceStack> context,
                                                                     SuggestionsBuilder builder, boolean targeted,
                                                                     Stat stat) {
        try {
            List<BhBreedHorse> horses = targets(context, targeted);
            return suggestRange(builder, archetype(horses.get(0)), stat);
        } catch (CommandSyntaxException e) {
            return builder.buildFuture();
        }
    }

    private static CompletableFuture<Suggestions> suggestRange(SuggestionsBuilder builder, ArchetypeType arch, Stat stat) {
        String high = fmt(stat.shown(stat.high(arch)));
        String low = fmt(stat.shown(stat.low(arch)));
        if (high.startsWith(builder.getRemaining())) {
            builder.suggest(high, Component.translatable(MSG + "range_max"));
        }
        if (low.startsWith(builder.getRemaining())) {
            builder.suggest(low, Component.translatable(MSG + "range_min"));
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestOptions(CommandContext<CommandSourceStack> context,
                                                                 SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        int tokenStart = remaining.lastIndexOf(' ') + 1;
        String token = remaining.substring(tokenStart);
        List<String> used = new ArrayList<>();
        for (String part : remaining.substring(0, tokenStart).trim().split("\\s+")) {
            int eq = part.indexOf('=');
            if (eq > 0) {
                used.add(part.substring(0, eq).toLowerCase(Locale.ROOT));
            }
        }

        int eq = token.indexOf('=');
        if (eq < 0) {
            SuggestionsBuilder keys = builder.createOffset(builder.getStart() + tokenStart);
            for (String key : OPTION_KEYS) {
                if (!used.contains(key) && key.startsWith(token.toLowerCase(Locale.ROOT))) {
                    keys.suggest(key + "=");
                }
            }
            return keys.buildFuture();
        }

        String key = token.substring(0, eq).toLowerCase(Locale.ROOT);
        SuggestionsBuilder values = builder.createOffset(builder.getStart() + tokenStart + eq + 1);
        ResourceKey<BreedType> breedKey = breedOrNull(context);
        BreedType type = breedKey == null ? null : BhRegistries.breedTypeRegistry().getValue(breedKey.identifier());
        ArchetypeType arch = breedKey == null ? null : BhBreedData.of(breedKey).archetype();
        switch (key) {
            case "coat" -> {
                List<String> ids = new ArrayList<>();
                ids.add(RANDOM);
                if (type != null) {
                    ids.addAll(type.coats().coatIds());
                }
                return SharedSuggestionProvider.suggest(ids, values);
            }
            case "gender" -> {
                List<String> ids = new ArrayList<>(genderIds());
                ids.add(RANDOM);
                return SharedSuggestionProvider.suggest(ids, values);
            }
            case "speed", "jump", "health" -> {
                return arch == null ? values.buildFuture()
                        : suggestRange(values, arch, Stat.valueOf(key.toUpperCase(Locale.ROOT)));
            }
            case "bond" -> {
                return SharedSuggestionProvider.suggest(List.of("0", "20", "40", "60", "80", "100"), values);
            }
            case "baby" -> {
                return SharedSuggestionProvider.suggest(List.of("true", "false"), values);
            }
            case "owner" -> {
                return SharedSuggestionProvider.suggest(context.getSource().getOnlinePlayerNames(), values);
            }
            default -> {
                return values.buildFuture();
            }
        }
    }
}
