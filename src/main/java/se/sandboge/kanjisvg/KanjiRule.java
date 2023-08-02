package se.sandboge.kanjisvg;

import java.util.List;

public record KanjiRule(String id, String strokeId, String type, List<String> params) {
}
