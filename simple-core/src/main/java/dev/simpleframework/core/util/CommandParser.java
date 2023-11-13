package dev.simpleframework.core.util;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import lombok.Data;

import java.util.List;

/**
 * 命令行解析器
 * *
 * * <dependency>
 * *     <groupId>com.beust</groupId>
 * *     <artifactId>jcommander</artifactId>
 * * </dependency>
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class CommandParser<T> {

    /**
     * 命令行参数值对象
     */
    private T object;

    /**
     * 命令行原参数
     */
    private String[] originalArgs;

    /**
     * 命令行程序名
     */
    private String programName;

    /**
     * obj 中未定义的参数值
     */
    private List<String> unknownOptions;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Show the usage message")
    private boolean help = false;

    public static <T> CommandParser<T> of(T obj, String[] args) {
        return of(obj, args, null);
    }

    public static <T> CommandParser<T> of(T obj, String[] args, String programName) {
        CommandParser<T> result = new CommandParser<>();
        result.object = obj;
        result.originalArgs = args;
        result.programName = programName;
        return result;
    }

    public boolean parse() {
        return parse(false, 1);
    }

    public boolean parse(boolean exitWhenHelpOrError) {
        return parse(exitWhenHelpOrError, 1);
    }

    public boolean parse(boolean exitWhenHelpOrError, int exitCode) {
        JCommander commander = JCommander.newBuilder()
                .programName(this.programName)
                .addObject(this)
                .addObject(this.object)
                .acceptUnknownOptions(true)
                .build();
        try {
            commander.parse(this.originalArgs);
            this.unknownOptions = commander.getUnknownOptions();
        } catch (ParameterException e) {
            if (exitWhenHelpOrError) {
                System.err.println(e.getMessage());
                usage(commander, true, exitCode);
            } else {
                usage(commander, false, exitCode);
                throw e;
            }
        }
        if (this.help) {
            usage(commander, exitWhenHelpOrError, exitCode);
            return false;
        }
        return true;
    }

    private void usage(JCommander commander, boolean exit, int exitCode) {
        commander.usage();
        if (exit) {
            System.exit(exitCode);
        }
    }

}
