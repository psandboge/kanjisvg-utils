package se.sandboge.kanjisvg;

import java.util.List;

public record SvgElement(String width, String height, String viewBox, List<KanjiGroup> groups, List<KanjiPart> parts) {}

