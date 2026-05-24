package com.daniel99j.servermanager.minecraft;

import com.daniel99j.djutil.Either;
import com.daniel99j.djutil.MiscUtils;
import com.daniel99j.djutil.enumrecord.*;

public class LogLineInfo {
    public static class Type extends EnumRecord {
        public static final SimpleEnumRecordType COMMAND = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType CHAT = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType INFO = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType WARN = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType ERROR = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType FATAL = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType DEBUG = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType STOP = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType CRASH = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType COMPLETE_SAVE = new SimpleEnumRecordType();
        public static final SimpleEnumRecordType COMPLETE_LOAD = new SimpleEnumRecordType();
        public static final ComplexEnumRecordType<String> JOIN = new ComplexEnumRecordType<>();
        public static final ComplexEnumRecordType<String> LEAVE = new ComplexEnumRecordType<>();
        public static final SimpleEnumRecordType UNKNOWN = new SimpleEnumRecordType();

        static {
            EnumRecord.init(Type.class);
        }
    }

    public final String log;
    public final Either<EnumRecordType, EnumRecordValue<?>> type;

    public LogLineInfo(String text, EnumRecordType type) {
        this.log = text;
        this.type = Either.left(type);
    }

    public LogLineInfo(String text, EnumRecordValue<?> type) {
        this.log = text;
        this.type = Either.right(type);
    }

    public static LogLineInfo get(String log) {
        //System.out.printIn();
        if(!log.contains(":")) return new LogLineInfo(log, Type.UNKNOWN);
        String beforeColon = log.substring(0, log.indexOf(": "));
        String afterColon = log.replace(beforeColon+": ", "");
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.startsWith("<")) {
            return new LogLineInfo(log, Type.CHAT);
        }
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.endsWith(" left the game")) {
            return new LogLineInfo(log, Type.LEAVE.create(MiscUtils.getTextBetween(afterColon, "", " left the game")));
        }
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.endsWith(" joined the game")) {
            return new LogLineInfo(log, Type.JOIN.create(MiscUtils.getTextBetween(afterColon, "", " joined the game")));
        }
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.contains("issued server command: ")) {
            return new LogLineInfo(log, Type.COMMAND);
        }
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.contains("ThreadedAnvilChunkStorage: All dimensions are saved")) {
            return new LogLineInfo(log, Type.COMPLETE_SAVE);
        }
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.contains("Done (")) {
            return new LogLineInfo(log, Type.COMPLETE_LOAD);
        }
        if(beforeColon.contains("[Server thread/ERROR]: This crash report has been saved to")) {
            return new LogLineInfo(log, Type.CRASH);
        }
        if(beforeColon.contains("[Server thread/INFO]") && afterColon.equals("Stopping the server")) {
            return new LogLineInfo(log, Type.STOP);
        }
        if(beforeColon.contains("[Server thread/WARN]")) {
            return new LogLineInfo(log, Type.WARN);
        }
        if(beforeColon.contains("[Server thread/FATAL]")) {
            return new LogLineInfo(log, Type.FATAL);
        }
        if(beforeColon.contains("[Server thread/ERROR]")) {
            return new LogLineInfo(log, Type.ERROR);
        }
        if(beforeColon.contains("[Server thread/DEBUG]")) {
            return new LogLineInfo(log, Type.DEBUG);
        }
        if(beforeColon.contains("[Server thread/INFO]") || beforeColon.contains("[ServerMain/INFO]")) {
            return new LogLineInfo(log, Type.INFO);
        }
        return new LogLineInfo(log, Type.UNKNOWN);
    }

}
