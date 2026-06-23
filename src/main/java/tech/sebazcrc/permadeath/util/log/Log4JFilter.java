package tech.sebazcrc.permadeath.util.log;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;

public class Log4JFilter extends AbstractFilter {

    private static final long serialVersionUID = -5594073755007974254L;

    private static Result validateMessage(Message message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        return validateMessage(message.getFormattedMessage());
    }

    private static Result validateMessage(String message) {
        if (message == null) return Result.NEUTRAL;

        if (message.contains("Ignoring unknown attribute")) {
            return Result.DENY;
        }

        if (message.contains("Summoned new Wither")) {
            return Result.DENY;
        }

        return Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) return Result.NEUTRAL;
        return validateMessage(event.getMessage());
    }

    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg);
    }

    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }

    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        if (msg == null) return Result.NEUTRAL;
        return validateMessage(msg.toString());
    }
}
   candidate = msg.toString();
        }
        return validateMessage(candidate);
    }
}