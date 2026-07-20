package com.lathaeps.lathabulk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure-Java parser for short, free-form handwritten business material notes. */
final class MaterialNoteParser {
    private static final String UNIT_WORDS =
            "coils?|coi+l|nos?|pcs?|pieces?|mtrs?|meters?|metres?|boxes?|rolls?|sets?|kgs?|pairs?|pkts?|packets?";
    private static final Pattern QTY_UNIT = Pattern.compile(
            "(?i)(\\d+(?:[.,]\\d+)?)\\s*(" + UNIT_WORDS + ")\\b");
    private static final Pattern DISCOUNT = Pattern.compile(
            "(?i)(?:disc(?:ount)?|less)\\s*[:=\\-]?\\s*(\\d+(?:[.,]\\d+)?)\\s*%?");
    private static final Pattern PERCENT = Pattern.compile("(?i)(\\d+(?:[.,]\\d+)?)\\s*(?:%|/\\.?|percent)");
    private static final Pattern HEADING_NOISE = Pattern.compile(
            "(?i)\\b(quotation|estimate|material\\s*list|description|particulars|qty|quantity|total|subtotal|amount|invoice|customer|address|phone|gstin)\\b");

    static final class ParsedItem {
        final String description;
        final double quantity;
        final String unit;

        ParsedItem(String description, double quantity, String unit) {
            this.description = description;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    static final class ParseResult {
        final List<ParsedItem> items = new ArrayList<>();
        String heading = "";
        Double overallDiscount;
    }

    static ParseResult parse(String raw) {
        ParseResult result = new ParseResult();
        if (raw == null) return result;
        String[] lines = raw.replace('\r', '\n').split("\\n+");
        boolean waitingForDiscountValue = false;

        for (String source : lines) {
            String line = cleanLine(source);
            if (line.length() < 1) continue;
            String lower = line.toLowerCase(Locale.ROOT);

            Matcher discount = DISCOUNT.matcher(line);
            if (discount.find()) {
                result.overallDiscount = clampPercent(number(discount.group(1), 0));
                waitingForDiscountValue = false;
                continue;
            }
            if (lower.matches(".*\\b(?:disc(?:ount)?|less)\\b.*")) {
                Matcher percent = PERCENT.matcher(line);
                if (percent.find()) result.overallDiscount = clampPercent(number(percent.group(1), 0));
                else waitingForDiscountValue = true;
                continue;
            }
            if (waitingForDiscountValue) {
                Matcher percent = PERCENT.matcher(line);
                if (percent.find()) {
                    result.overallDiscount = clampPercent(number(percent.group(1), 0));
                    waitingForDiscountValue = false;
                    continue;
                }
            }

            ParsedItem item = parseItem(line);
            if (item != null) {
                result.items.add(item);
                continue;
            }

            if (result.heading.isEmpty() && looksLikeHeading(line)) result.heading = cleanHeading(line);
        }
        return result;
    }

    static String toReviewText(ParseResult result) {
        StringBuilder out = new StringBuilder();
        for (ParsedItem item : result.items) {
            if (out.length() > 0) out.append('\n');
            out.append(item.description).append(" | ")
                    .append(format(item.quantity)).append(" | ").append(item.unit);
        }
        return out.toString();
    }

    private static ParsedItem parseItem(String line) {
        String withoutSerial = line.replaceFirst("^\\s*\\d{1,3}[.)]\\s+", "").trim();
        Matcher matcher = QTY_UNIT.matcher(withoutSerial);
        int start = -1, end = -1;
        String qtyText = null, unitText = null;
        while (matcher.find()) {
            start = matcher.start(); end = matcher.end(); qtyText = matcher.group(1); unitText = matcher.group(2);
        }
        if (start < 0) return null;

        String before = trimSeparators(withoutSerial.substring(0, start));
        String after = trimSeparators(withoutSerial.substring(end));
        String description = before;
        if (description.isEmpty() && !after.isEmpty() && !after.matches(".*(?:₹|(?i:rs\\.?|inr))\\s*\\d+.*")) description = after;
        if (description.isEmpty()) return null;
        description = description.replaceAll("(?i)\\b(?:qty|quantity)\\b", " ")
                .replaceAll("\\s+", " ").trim();
        if (description.matches("\\d+(?:[.,]\\d+)?")) description = description.replace(',', '.') + " sq";
        if (description.length() > 80) description = description.substring(0, 80).trim();
        return new ParsedItem(description, Math.max(0.0001, number(qtyText, 1)), normalizeUnit(unitText));
    }

    private static boolean looksLikeHeading(String line) {
        if (line.length() < 2 || line.length() > 60 || line.matches(".*\\d{5,}.*")) return false;
        String candidate = cleanHeading(line);
        return candidate.matches(".*[A-Za-z].*") && !HEADING_NOISE.matcher(candidate).find();
    }

    private static String cleanHeading(String line) {
        return line.replaceAll("(?i)\\b(?:quotation|estimate|material\\s*list)\\b", " ")
                .replaceAll("[-:=]+$", "").replaceAll("\\s+", " ").trim();
    }

    private static String cleanLine(String line) {
        return line == null ? "" : line.replace('|', ' ').replace('—', '-')
                .replace('–', '-').replaceAll("\\s+", " ").trim();
    }

    private static String trimSeparators(String value) {
        return value.replaceAll("^[\\s:=xX-]+|[\\s:=xX-]+$", "").trim();
    }

    private static String normalizeUnit(String raw) {
        String unit = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
        if (unit.startsWith("coi")) return "Coils";
        if (unit.startsWith("mtr") || unit.startsWith("meter") || unit.startsWith("metre")) return "Mtr";
        if (unit.startsWith("box")) return "Box";
        if (unit.startsWith("roll")) return "Roll";
        if (unit.startsWith("set")) return "Set";
        if (unit.startsWith("kg")) return "Kg";
        if (unit.startsWith("pair")) return "Pair";
        if (unit.startsWith("pkt") || unit.startsWith("packet")) return "Pkt";
        return "Nos";
    }

    private static double number(String value, double fallback) {
        try { return Double.parseDouble(value.replace(',', '.')); }
        catch (Exception ignored) { return fallback; }
    }

    private static double clampPercent(double value) { return Math.max(0, Math.min(100, value)); }

    private static String format(double value) {
        return Math.abs(value - Math.rint(value)) < 0.00001
                ? String.valueOf((long) Math.rint(value))
                : String.format(Locale.US, "%.2f", value);
    }

    private MaterialNoteParser() {}
}
