// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.Commands.CommandSelection;

public class TimeLock {
    private static final String[] BLOCKED = {"set", "add", "rate", "paused"};

    public static void register() {
        CommandRegistrationCallback.EVENT.register(TimeLock::onRegister);
    }

    private static void onRegister(CommandDispatcher<CommandSourceStack> dispatcher,
                                   CommandBuildContext buildContext, CommandSelection selection) {
        CommandNode<CommandSourceStack> time = dispatcher.getRoot().getChild("time");
        if (time == null) {
            return;
        }
        for (String sub : BLOCKED) {
            CommandNode<CommandSourceStack> node = time.getChild(sub);
            if (node != null) {
                Map<String, CommandNode<CommandSourceStack>> children =
                        (Map<String, CommandNode<CommandSourceStack>>) (Object) getChildren(time);
                children.remove(sub);
            }
        }
    }

    private static int denied(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("message.pvzkongdao.time_locked")
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
        return 0;
    }

    private static Object getChildren(CommandNode<CommandSourceStack> node) {
        try {
            java.lang.reflect.Field f = CommandNode.class.getDeclaredField("children");
            f.setAccessible(true);
            return f.get(node);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}