package com.scalpelred.chatcircuit;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.IClientCommand;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ChatProcessorCommand implements IClientCommand {

    private final ChatCircuit chatCircuit;

    public ChatProcessorCommand(ChatCircuit chatCircuit) {
        this.chatCircuit = chatCircuit;
    }

    @Override
    public String getName() {
        return "chatproc";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/chatproc active <NAME>\n/charproc active <NAME> <VALUE>\n" +
                "/chatproc defactive <NAME>\n/charproc defactive <NAME> <VALUE>\n" +
                "/chatproc reload <NAME>\n/chatproc index <NAME> <INDEX>\n/chatproc list\n/chatproc list<PAGE>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {

        EntityPlayerSP player;
        if (sender instanceof EntityPlayerSP) player = (EntityPlayerSP)sender;
        else throw new PlayerNotFoundException("commands.generic.player.unspecified");

        if (args.length == 0)
        {
            player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.noargs"));
            return;
        }

        switch (args[0]) {
            case "active": {

                if (args.length == 1) {
                    player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.noname", args[1]));
                }
                else if (args.length == 2) { // get
                    ChatProcessor cp = chatCircuit.getProcByName(args[1]);
                    if (cp == null) {
                        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.procnotfound", args[1]));
                    } else {
                        player.sendMessage(ChatCircuit.translateFormat(
                                "commands.chatproc.active.get.success", args[1], chatCircuit.isActive(cp)));
                    }
                }
                else { // set
                    ChatProcessor cp = chatCircuit.getProcByName(args[1]);
                    if (cp == null) {
                        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.procnotfound", args[1]));
                    } else {
                        boolean value = Boolean.parseBoolean(args[2]);
                        if (value) chatCircuit.setActive(cp);
                        else chatCircuit.setInactive(cp);
                        player.sendMessage(ChatCircuit.translateFormat(
                                "commands.chatproc.active.set.success", args[1], value));
                    }
                }
            }
            break;

            case "defactive": {

                if (args.length == 1) {
                    player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.noname"));
                }
                else if (args.length == 2) { // get
                    ChatProcessor cp = chatCircuit.getProcByName(args[1]);
                    if (cp == null) {
                        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.procnotfound", args[1]));
                    } else {
                        player.sendMessage(ChatCircuit.translateFormat(
                                "commands.chatproc.defactive.get.success", args[1], chatCircuit.isDefactive(cp)));
                    }
                }
                else { // set
                    ChatProcessor cp = chatCircuit.getProcByName(args[1]);
                    if (cp == null) {
                        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.procnotfound", args[1]));
                    } else {
                        boolean value = Boolean.parseBoolean(args[2]);
                        if (value) chatCircuit.setDefactive(cp);
                        else chatCircuit.setDefinactive(cp);
                        player.sendMessage(ChatCircuit.translateFormat(
                                "commands.chatproc.defactive.set.success", args[1], value));
                    }
                }
            }
            break;

            case "load":
            case "reload": {
                if (args.length == 1) {
                    player.sendMessage(ChatCircuit.translateFormat("commands.reload.noname"));
                }
                else {
                    try {
                        ChatProcessor res = chatCircuit.loadProcessor(args[1]);
                        if (res == null) {
                            player.sendMessage(ChatCircuit.translateFormat(
                                    "commands.chatproc.reload.nofile", args[1]));
                        }
                        else {
                            player.sendMessage(ChatCircuit.translateFormat(
                                    "commands.chatproc.reload.success", args[1]));
                        }
                    }
                    catch (Throwable e) {
                        player.sendMessage(ChatCircuit.translateFormat(
                                "commands.chatproc.reload.error", args[1], e.toString()));
                    }

                }
            }
            break;

            case "index": {
                if (args.length == 1) {
                    player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.noname"));
                }
                else if (args.length == 2) {
                    player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.index.noindex"));
                }
                else {
                    ChatProcessor cp = chatCircuit.getProcByName(args[1]);
                    if (cp == null) {
                        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.procnotfound", args[1]));
                    }
                    else {
                        try {
                            int index = Integer.parseInt(args[2]);
                            chatCircuit.setIndex(cp, index);
                            player.sendMessage(ChatCircuit.translateFormat(
                                    "commands.chatproc.index.success", args[1], index));
                        }
                        catch (NumberFormatException e) {
                            player.sendMessage(ChatCircuit.translateFormat(
                                    "commands.chatproc.index.notnumber", args[2]));
                        }
                    }
                }
            }
            break;

            case "list": {

                if (args.length == 1) {
                    printProcPage(1, player);
                }
                else {
                    try {
                        int page = Integer.parseInt(args[1]);
                        printProcPage(page, player);
                    }
                    catch (NumberFormatException e) {
                        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.list.notnumber", args[1]));
                    }
                }

            }
            break;

            default: {
                player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.unknownaction", args[0]));
            }
        }
    }

    private static final int PROCS_PER_PAGE = 5;
    private void printProcPage(int page, EntityPlayerSP player) {

        if (page < 1) {
            player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.list.negativepage", page));
            return;
        }

        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.list.header", page));

        ChatProcessor[] procs = chatCircuit.getProcArray();

        int i0 = (page - 1) * PROCS_PER_PAGE;

        for (int i = 0; i < PROCS_PER_PAGE; i++) {
            StringBuilder msg = new StringBuilder();
            int index = i0 + i;
            if (index >= procs.length) break;
            msg.append(index);
            msg.append(". ");

            ChatProcessor cp = procs[index];

            msg.append(cp.getName());
            msg.append(" (");

            if (chatCircuit.isActive(cp))
                msg.append(ChatCircuit.translateFormat("commands.chatproc.list.activenow"));
            else msg.append(ChatCircuit.translateFormat("commands.chatproc.list.inactivenow"));
            msg.append(", ");

            if (chatCircuit.isDefactive(cp))
                msg.append(ChatCircuit.translateFormat("commands.chatproc.list.activedefault"));
            else msg.append(ChatCircuit.translateFormat("commands.chatproc.list.inactivedefault"));

            msg.append(")");

            player.sendMessage(new TextComponentString(msg.toString()));
        }

        player.sendMessage(ChatCircuit.translateFormat("commands.chatproc.list.bottom"));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos blockPos){
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender iCommandSender, String s) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
