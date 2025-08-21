package com.shardclay.shardplexmotd.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradientUtil {

    private static final Map<String, TextDecoration> FORMATTING_TAGS = new HashMap<>();

    static {
        FORMATTING_TAGS.put("bold", TextDecoration.BOLD);
        FORMATTING_TAGS.put("italic", TextDecoration.ITALIC);
        FORMATTING_TAGS.put("underlined", TextDecoration.UNDERLINED);
        FORMATTING_TAGS.put("strikethrough", TextDecoration.STRIKETHROUGH);
        FORMATTING_TAGS.put("obfuscated", TextDecoration.OBFUSCATED);
    }

    public static Component parse(String input) {
        Component component = Component.empty();
        Pattern pattern = Pattern.compile("<(#[0-9a-fA-F]{6})>(.*?)<(#[0-9a-fA-F]{6})>");
        Matcher matcher = pattern.matcher(input);

        int lastEnd = 0;
        while (matcher.find()) {
            component = component.append(parseFormatting(input.substring(lastEnd, matcher.start())));
            lastEnd = matcher.end();

            String startColor = matcher.group(1);
            String text = matcher.group(2);
            String endColor = matcher.group(3);

            component = component.append(applyGradient(text, startColor, endColor));
        }
        component = component.append(parseFormatting(input.substring(lastEnd)));

        return component;
    }

    public static List<Component> parse(List<String> lines) {
        return lines.stream().map(GradientUtil::parse).collect(Collectors.toList());
    }

    public static Component parseLegacy(String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }

    private static Component parseFormatting(String input) {
        Component component = Component.empty();
        Set<TextDecoration> decorations = new HashSet<>();
        Pattern formattingPattern = Pattern.compile("<(/?)(bold|italic|underlined|strikethrough|obfuscated)>");
        Matcher matcher = formattingPattern.matcher(input);
        int lastEnd = 0;

        while (matcher.find()) {
            String plainText = input.substring(lastEnd, matcher.start());
            if (!plainText.isEmpty()) {
                component = component.append(Component.text(plainText).decorations(toDecorationMap(decorations)));
            }
            lastEnd = matcher.end();

            boolean isClosing = !matcher.group(1).isEmpty();
            String tag = matcher.group(2);
            TextDecoration decoration = FORMATTING_TAGS.get(tag);
            if (isClosing) {
                decorations.remove(decoration);
            } else {
                decorations.add(decoration);
            }
        }
        String plainText = input.substring(lastEnd);
        if (!plainText.isEmpty()) {
            component = component.append(Component.text(plainText).decorations(toDecorationMap(decorations)));
        }

        return component;
    }

    private static Component applyGradient(String textWithTags, String startColor, String endColor) {
        String plainText = textWithTags.replaceAll("<.*?>", "");
        int plainTextLength = plainText.length();
        int plainTextIndex = 0;

        Component component = Component.empty();
        Set<TextDecoration> decorations = new HashSet<>();
        Pattern formattingPattern = Pattern.compile("<(/?)(bold|italic|underlined|strikethrough|obfuscated)>");
        Matcher matcher = formattingPattern.matcher(textWithTags);
        int lastEnd = 0;

        while (matcher.find()) {
            String textSegment = textWithTags.substring(lastEnd, matcher.start());
            if (!textSegment.isEmpty()) {
                component = component.append(applyGradientToSegment(textSegment, startColor, endColor, plainTextLength, plainTextIndex, decorations));
                plainTextIndex += textSegment.length();
            }
            lastEnd = matcher.end();

            boolean isClosing = !matcher.group(1).isEmpty();
            String tag = matcher.group(2);
            TextDecoration decoration = FORMATTING_TAGS.get(tag);
            if (isClosing) {
                decorations.remove(decoration);
            } else {
                decorations.add(decoration);
            }
        }
        String textSegment = textWithTags.substring(lastEnd);
        if (!textSegment.isEmpty()) {
            component = component.append(applyGradientToSegment(textSegment, startColor, endColor, plainTextLength, plainTextIndex, decorations));
        }

        return component;
    }

    private static Component applyGradientToSegment(String text, String startColor, String endColor, int totalLength, int startIndex, Set<TextDecoration> decorations) {
        Component component = Component.empty();
        int[] startRGB = hexToRgb(startColor);
        int[] endRGB = hexToRgb(endColor);

        for (int i = 0; i < text.length(); i++) {
            double progress = totalLength > 1 ? (double) (startIndex + i) / (double) (totalLength - 1) : 0.0;
            int r = interpolate(startRGB[0], endRGB[0], progress);
            int g = interpolate(startRGB[1], endRGB[1], progress);
            int b = interpolate(startRGB[2], endRGB[2], progress);

            component = component.append(Component.text(text.charAt(i)).color(TextColor.color(r, g, b)).decorations(toDecorationMap(decorations)));
        }
        return component;
    }

    private static Map<TextDecoration, TextDecoration.State> toDecorationMap(Set<TextDecoration> decorations) {
        Map<TextDecoration, TextDecoration.State> map = new HashMap<>();
        for (TextDecoration decoration : decorations) {
            map.put(decoration, TextDecoration.State.TRUE);
        }
        return map;
    }

    private static int interpolate(int start, int end, double progress) {
        return (int) (start + (end - start) * progress);
    }

    private static int[] hexToRgb(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return new int[]{Integer.parseInt(hex.substring(0, 2), 16), Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)};
    }
}