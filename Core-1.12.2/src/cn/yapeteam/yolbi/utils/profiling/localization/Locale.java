package cn.yapeteam.yolbi.utils.profiling.localization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * @author Hazsi
 * @since 10/31/2022
 */
@Getter
@RequiredArgsConstructor
public enum Locale {
    EN_US("en_US"),
    FR_FR("fr_FR"),
    ES_ES("es_ES"),

    // Partial
    SV_SE("sv_SE"),
    PL_PL("pl_PL"),
    RU_RU("ru_RU"),
    ZH_ZH("zh_ZH");

    private final String file;
    private final HashMap<String, String> strings = new HashMap<>();
}