package com.civicscience.entity;

import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JotLog {
    private Integer sequence;
    private String id;
    private String ts;
    private Boolean consent_accepted;
    private String meta_target_id;
    private String target_id;
    private String state;
    private String space;
    private Integer age;
    private Integer gender;
    private String locale;
    private List<String> questions;
    private String context;
    private String user_alias;
    private Boolean isContainerSeen;
    private String usage;
    private Integer position;
    private String askable;
    private String user_session;
    private List<String> natures;
    private String session_template;
    private String session_template_group;
    private String ad_id;
    private String platform;
    private String ip_v4;
    private String ua_browser_family;
    private String ua_browser_version;
    private String ua_os_family;
    private String ua_os_version;
    private String ua_device_family;
    private String ua_device_class;
    private boolean ua_is_mobile;
    private boolean ua_is_bot;
    private String cs_id;
    private Integer hashCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JotLog jotLog = (JotLog) o;
        return Objects.equals(id, jotLog.id) && Objects.equals(context, jotLog.context) && Objects.equals(user_alias,
                jotLog.user_alias) && Objects.equals(user_session, jotLog.user_session) && Objects.equals(ip_v4,
                jotLog.ip_v4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, context, user_alias, user_session, ip_v4);
    }
}
