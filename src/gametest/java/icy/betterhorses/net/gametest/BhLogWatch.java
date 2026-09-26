package icy.betterhorses.net.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BhLogWatch implements AutoCloseable {

    private final List<String> captured = new CopyOnWriteArrayList<>();
    private final Appender appender;
    private final LoggerConfig rootConfig;
    private final LoggerContext ctx;

    private BhLogWatch(Appender appender, LoggerConfig rootConfig, LoggerContext ctx) {
        this.appender = appender;
        this.rootConfig = rootConfig;
        this.ctx = ctx;
    }

    public static BhLogWatch start() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig rootConfig = config.getRootLogger();

        BhLogWatch[] holder = new BhLogWatch[1];
        AbstractAppender appender = new AbstractAppender(
                "bh-log-watch-" + System.nanoTime(), null, null, true, null) {
            @Override
            public void append(LogEvent event) {
                String logger = event.getLoggerName();
                if (logger != null && (logger.startsWith("net.fabricmc.fabric.impl.gametest")
                        || logger.startsWith("net.minecraft.gametest.framework"))) {
                    return;
                }
                boolean severe = event.getLevel().isMoreSpecificThan(Level.ERROR);
                if (severe || event.getThrown() != null) {
                    holder[0].captured.add(event.getLevel() + " " + event.getLoggerName() + ": "
                            + event.getMessage().getFormattedMessage()
                            + (event.getThrown() != null ? " (" + event.getThrown() + ")" : ""));
                }
            }
        };
        appender.start();
        rootConfig.addAppender(appender, null, null);
        ctx.updateLoggers();

        BhLogWatch watch = new BhLogWatch(appender, rootConfig, ctx);
        holder[0] = watch;
        return watch;
    }

    public List<String> captured() {
        return List.copyOf(captured);
    }

    public boolean isClean() {
        return captured.isEmpty();
    }

    public void assertClean(GameTestHelper helper, String context) {
        helper.assertTrue(captured.isEmpty(),
                context + ": expected no ERROR/exception log events, got " + captured.size() + ": "
                        + String.join(" || ", captured));
    }

    @Override
    public void close() {
        rootConfig.removeAppender(appender.getName());
        ctx.updateLoggers();
        appender.stop();
    }
}
