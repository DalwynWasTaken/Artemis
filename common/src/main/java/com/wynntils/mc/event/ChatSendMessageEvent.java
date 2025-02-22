/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatSendMessageEvent extends Event {
    private final String message;

    public ChatSendMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
