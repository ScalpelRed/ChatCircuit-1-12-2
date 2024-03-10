package com.scalpelred.chatcircuit;

import com.mojang.realmsclient.client.RealmsClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandHandler;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mod(modid = ChatCircuit.MODID, name = ChatCircuit.NAME, version = ChatCircuit.VERSION)
public class ChatCircuit
{
    public static final String MODID = "chatcircuit";
    public static final String NAME = "Chat circuit";
    public static final String VERSION = "1.0";

    private static Logger logger;

    private static final String CHAR_PROC_DIR = "\\chatprocessors";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        ClientCommandHandler.instance.registerCommand(new ChatProcessorCommand(this));
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        //event.registerServerCommand(new ChatProcessorCommand(this));
    }

    private HashArrayList<ChatProcessor> chatProcessors = new HashArrayList<>();
    private HashArrayList<ChatProcessor> activeProcessors = new HashArrayList<>();
    private HashArrayList<ChatProcessor> defactiveProcessors = new HashArrayList<>();

    public ChatProcessor getProcByName(String name) {
        for (ChatProcessor c : chatProcessors) if (c.getName().equals(name)) return c;
        return null;
    }

    public ChatProcessor getProcByIndex(int index) {
        return chatProcessors.getAt(index);
    }

    public void setActive(ChatProcessor chatProcessor) {

        if (activeProcessors.contains(chatProcessor)) return;

        ChatProcessor prev = null;
        for (ChatProcessor other : chatProcessors) {
            if (other.equals(chatProcessor)) break;
            if (activeProcessors.contains(chatProcessor)) prev = other;
        }

        if (prev != null) activeProcessors.insert(chatProcessor, activeProcessors.indexOf(prev) + 1);
        else activeProcessors.insert(chatProcessor, 0);
    }

    public void setInactive(ChatProcessor chatProcessor) {
        activeProcessors.remove(chatProcessor);
    }

    public boolean isActive(ChatProcessor chatProcessor) {
        return activeProcessors.contains(chatProcessor);
    }

    public void setDefactive(ChatProcessor chatProcessor) {

        if (defactiveProcessors.contains(chatProcessor)) return;

        ChatProcessor prev = null;
        for (ChatProcessor other : chatProcessors) {
            if (other.equals(chatProcessor)) break;
            if (defactiveProcessors.contains(chatProcessor)) prev = other;
        }

        if (prev != null) defactiveProcessors.insert(chatProcessor, defactiveProcessors.indexOf(prev) + 1);
        else defactiveProcessors.insert(chatProcessor, 0);
    }

    public void setDefinactive(ChatProcessor chatProcessor) {
        defactiveProcessors.remove(chatProcessor);
    }

    public boolean isDefactive(ChatProcessor chatProcessor) {
        return defactiveProcessors.contains(chatProcessor);
    }

    public void setIndex(ChatProcessor chatProcessor, int index) {
        chatProcessors.remove(chatProcessor);
        chatProcessors.insert(chatProcessor, index);

        if (activeProcessors.remove(chatProcessor)) {
            setActive(chatProcessor);
        }

        if (defactiveProcessors.remove(chatProcessor)) {
            setDefactive(chatProcessor);
        }
    }

    public ChatProcessor[] getProcArray() {
        return chatProcessors.toArray(new ChatProcessor[0]);
    }

    public ChatProcessor loadProcessor(String name) throws Exception {
        ChatProcessor res = loadProc(name, getProcFolder());
        if (res != null) {
            res.setName(name);
            if (chatProcessors.replaceEqual(res)) {
                activeProcessors.replaceEqual(res);
                defactiveProcessors.replaceEqual(res);
            }
            else {
                chatProcessors.add(res);
            }
        }
        return res;
    }

    private ChatProcessor loadProc(String name, File folder) throws Exception {

        File cpfile = Paths.get(folder.getAbsolutePath(), name + ".jar").toFile();
        if (!cpfile.exists()) return null;

        String mainClassName;
        {
            JarFile jarFile = new JarFile(cpfile);
            JarEntry entry = jarFile.getJarEntry("mainclass.ini");
            InputStream inputStream = jarFile.getInputStream(entry);
            Properties properties = new Properties();
            properties.load(inputStream);
            mainClassName = properties.getProperty("mainclass");
            inputStream.close();
        }

        //URLClassLoader classLoader = new URLClassLoader(new URL[] {cpfile.toURI().toURL() });
        ModClassLoader classLoader = Loader.instance().getModClassLoader();
        classLoader.addFile(cpfile);
        Class<?> cpclass = classLoader.loadClass(mainClassName);
        ChatProcessor chatProcessor = (ChatProcessor) cpclass.getDeclaredConstructor().newInstance();

        return chatProcessor;
    }

    private File getProcFolder() {
        File res = new File(System.getProperty("user.dir"), CHAR_PROC_DIR);
        if (!res.exists()) {
            try {
                Files.createDirectories(Paths.get(System.getProperty("user.dir"), CHAR_PROC_DIR));
                logger.info("Created chatprocessors directory.");
            }
            catch (IOException e) {
                logger.error("Error creating chatprocessors directory: " + e.getMessage());
                return null;
            }
        }
        return res;
    }

    public static TextComponentString translateFormat(String str, Object... args) {
        return new TextComponentString(I18n.format(str, args));
    }
}
