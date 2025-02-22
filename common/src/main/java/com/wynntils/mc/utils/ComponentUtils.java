/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.wynntils.wc.utils.WynnUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

public class ComponentUtils {
    public static String getFormatted(Component component) {
        StringBuilder result = new StringBuilder();

        component.visit(
                new FormattedText.StyledContentConsumer<>() {
                    Style oldStyle = Style.EMPTY;

                    @Override
                    public Optional<Object> accept(Style style, String string) {
                        handleStyleDifference(oldStyle, style, result);
                        result.append(string);

                        oldStyle = style;

                        return Optional.empty();
                    }
                },
                Style.EMPTY);

        return result.toString();
    }

    public static String getUnformatted(Component msg) {
        return msg.getString();
    }

    @Nullable
    public static String getFormatted(String loreString) {
        MutableComponent component = Component.Serializer.fromJson(loreString);
        if (component == null) return null;

        return ComponentUtils.getFormatted(component);
    }

    @Nullable
    public static String getUnformatted(String loreString) {
        MutableComponent component = Component.Serializer.fromJson(loreString);
        if (component == null) return null;

        return component.getString();
    }

    /**
     * This method handles the fact that the style likely has changed between 2 components
     *
     * <p>It tries to first generate a constructive way of adding color codes to get from the old
     * style to the new style. If that does not succeed, it instead resets the format and adds the
     * color codes of the new style
     */
    private static void handleStyleDifference(Style oldStyle, Style newStyle, StringBuilder result) {
        if (oldStyle.equals(newStyle)) return;

        if (!oldStyle.isEmpty()) {
            StringBuilder different = tryConstructDifference(oldStyle, newStyle);

            if (different != null) {
                result.append(different);
                return;
            }
        }

        result.append(ChatFormatting.RESET);

        if (newStyle.getColor() != null) {
            getChatFormatting(newStyle.getColor()).ifPresent(result::append);
        }

        if (newStyle.isBold()) result.append(ChatFormatting.BOLD);
        if (newStyle.isItalic()) result.append(ChatFormatting.ITALIC);
        if (newStyle.isUnderlined()) result.append(ChatFormatting.UNDERLINE);
        if (newStyle.isStrikethrough()) result.append(ChatFormatting.STRIKETHROUGH);
        if (newStyle.isObfuscated()) result.append(ChatFormatting.OBFUSCATED);
    }

    private static StringBuilder tryConstructDifference(Style oldStyle, Style newStyle) {
        StringBuilder add = new StringBuilder();

        int oldColorInt = Optional.ofNullable(oldStyle.getColor())
                .map(TextColor::getValue)
                .orElse(-1);
        int newColorInt = Optional.ofNullable(newStyle.getColor())
                .map(TextColor::getValue)
                .orElse(-1);

        if (oldColorInt == -1) {
            if (newColorInt != -1) {
                getChatFormatting(newColorInt).ifPresent(add::append);
            }
        } else if (oldColorInt != newColorInt) {
            return null;
        }

        if (oldStyle.isBold() && !newStyle.isBold()) return null;
        if (!oldStyle.isBold() && newStyle.isBold()) add.append(ChatFormatting.BOLD);

        if (oldStyle.isItalic() && !newStyle.isItalic()) return null;
        if (!oldStyle.isItalic() && newStyle.isItalic()) add.append(ChatFormatting.ITALIC);

        if (oldStyle.isUnderlined() && !newStyle.isUnderlined()) return null;
        if (!oldStyle.isUnderlined() && newStyle.isUnderlined()) add.append(ChatFormatting.UNDERLINE);

        if (oldStyle.isStrikethrough() && !newStyle.isStrikethrough()) return null;
        if (!oldStyle.isStrikethrough() && newStyle.isStrikethrough()) add.append(ChatFormatting.STRIKETHROUGH);

        if (oldStyle.isObfuscated() && !newStyle.isObfuscated()) return null;
        if (!oldStyle.isObfuscated() && newStyle.isObfuscated()) add.append(ChatFormatting.OBFUSCATED);

        return add;
    }

    public static Optional<ChatFormatting> getChatFormatting(TextColor textColor) {
        return getChatFormatting(textColor.getValue());
    }

    public static Optional<ChatFormatting> getChatFormatting(int textColor) {
        return Arrays.stream(ChatFormatting.values())
                .filter(c -> c.isColor() && textColor == c.getColor())
                .findFirst();
    }

    public static List<Component> stripDuplicateBlank(List<Component> lore) {
        List<Component> newLore = new ArrayList<>(); // Used to remove duplicate blank lines

        boolean oldBlank = false;
        int index = 0;

        for (; index < lore.size(); index++) { // find first blank
            Component loreLine = lore.get(index);

            String line = WynnUtils.normalizeBadString(loreLine.getString());

            newLore.add(loreLine);

            if (line.isEmpty()) {
                oldBlank = true;
                break;
            }
        }

        if (!oldBlank) {
            return newLore;
        }

        for (; index < lore.size(); index++) {
            Component loreLine = lore.get(index);

            String line = WynnUtils.normalizeBadString(loreLine.getString());

            if (oldBlank && line.isEmpty()) {
                continue; // both blank - do not add; oldBlank still true
            }

            oldBlank = line.isEmpty();

            newLore.add(loreLine);
        }

        return newLore;
    }
}
